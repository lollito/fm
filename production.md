# Scalability and Production Architecture Rework

This document analyzes the scalability limitations of the current `@Scheduled` task implementation and proposes a multi-phase strategy to transform the application into a robust, cloud-native system capable of running multiple instances in production without data corruption or double execution.

## 1. The Problem: `@Scheduled` in a Clustered Environment

The current application relies heavily on Spring's `@Scheduled` annotation for critical tasks:
- **Daily Processing:** `LoanService`, `FinancialService`, `TrainingService`, etc. run once a day.
- **High-Frequency Processing:** `LiveMatchService` runs every 5 seconds.
- **Match Simulation:** `MatchSchedulerService` runs frequently to find matches.

### Issues:
1.  **Duplicate Execution:** In a production environment with multiple replicas (e.g., Kubernetes pods or Docker Swarm services), *every* instance will trigger the `@Scheduled` method simultaneously.
    - **Consequence:** Players get trained multiple times per day (super-human stats), finances are deducted twice (bankruptcy), and matches are simulated twice (conflicting results).
2.  **Resource Contention:** If multiple nodes try to update the same database records at the same time, you risk deadlocks and `OptimisticLockingFailureException`.
3.  **No Failover Logic:** If the single instance crashes during a job, the job is lost until restart.

---

## 2. Phase 1: Distributed Locking (The Immediate Fix)

The most urgent fix is to ensure that **only one instance executes a scheduled task at a time**. We can achieve this using [ShedLock](https://github.com/lukas-krecan/ShedLock).

### Implementation Steps

#### 1. Add Dependencies
Add the following to `pom.xml`:

```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.10.0</version>
</dependency>
```

#### 2. Configure Locking
Create a configuration class:

```java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfiguration {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime() // Works on Postgres, MySQL, MariaDB
            .build()
        );
    }
}
```

#### 3. Create Lock Table (Migration)
You must create a table in your shared database (MySQL/Postgres):

```sql
CREATE TABLE shedlock(
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

#### 4. Update Scheduled Tasks
Annotate every `@Scheduled` method with `@SchedulerLock`:

```java
// In LoanService.java
@Scheduled(cron = "0 0 9 1 * *")
@SchedulerLock(name = "LoanService_processMonthlyLoanReviews",
    lockAtLeastFor = "5m", lockAtMostFor = "60m")
public void processMonthlyLoanReviews() {
    // ... logic
}

// In LiveMatchService.java
@Scheduled(fixedRate = 5000)
@SchedulerLock(name = "LiveMatchService_updateLiveMatches",
    lockAtLeastFor = "4s", lockAtMostFor = "10s")
public void updateLiveMatches() {
    // ... logic
}
```

### Result of Phase 1
- **Pros:** Prevents duplicate execution immediately. Simple to implement.
- **Cons:** Still a "pull" model. One node does all the work while others sit idle (for that specific task). The 5-second `LiveMatchService` loop might drift if processing takes > 5s.

---

## 3. Phase 2: Async Processing & Event-Driven Architecture (Scalability)

To truly scale, we must move from "Time-Based Polling" to "Event-Based Triggering".

### The Strategy
Instead of the scheduler *doing* the work, the scheduler (or a user action) should simply **enqueue a job**. Worker nodes then pick up these jobs.

#### 1. Message Broker
Introduce **RabbitMQ** or **Kafka** (RabbitMQ is simpler for this use case).

#### 2. Refactoring `MatchSchedulerService`
**Current:**
- Cron runs -> Finds matches -> Simulates them (blocking).

**Proposed:**
- Cron runs (with Lock) -> Finds matches -> Sends `MatchSimulationCommand` to Queue.
- **Multiple Worker Instances** listen to the Queue.
- Each Worker picks up a message and simulates *one* match.

**Benefit:** If you have 100 matches to simulate at 3 PM, 10 server instances can process 10 matches each in parallel.

#### 3. Refactoring `LiveMatchService`
The 5-second polling loop is inefficient for high scale.
**Proposed Architecture:**
- **Sharding:** Use a consistent hashing algorithm to assign matches to specific server instances.
  - Instance A handles Match IDs ending in 0-4.
  - Instance B handles Match IDs ending in 5-9.
- **WebSockets:** The client (browser) connects to a specific WebSocket topic. The server handling that match broadcasts updates.
- **Redis Pub/Sub:** If a backend node updates a match, it publishes to Redis. All WebSocket servers subscribe to Redis and push the update to connected clients (so clients don't need to be connected to the *specific* node processing the match).

---

## 4. Infrastructure & Database Considerations

### Database
- **Move away from H2:** H2 file-based DBs cannot be shared across multiple Docker containers.
- **Use a Shared MySQL/Postgres:** All instances must connect to the *same* physical database.
- **Connection Pooling:** Ensure HikariCP is configured correctly (e.g., `maximum-pool-size`) to handle the combined load of all instances.

### Redis
- Use Redis not just for caching, but for:
  - **Distributed Locks** (ShedLock can use Redis instead of JDBC for faster locking).
  - **Session Management** (Spring Session) so users stay logged in across instances.
  - **Real-time Pub/Sub** for WebSocket scaling.

## Summary of Roadmap

1.  **Immediate (Week 1):** Implement **ShedLock** with JDBC. This allows you to run 2+ replicas for high availability without corrupting data.
2.  **Medium Term (Week 2-4):** Move `MatchSimulation` to **Async @Async** or a **Job Queue** (RabbitMQ) to distribute the load of simulation.
3.  **Long Term:** Re-architect `LiveMatchService` to use a **Stateful Game Engine** pattern or Actor Model (e.g., Akka) if the number of concurrent live matches exceeds ~1,000.
