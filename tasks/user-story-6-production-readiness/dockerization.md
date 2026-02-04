# Dockerization Implementation

## Overview
Create a complete Docker containerization setup for the Football Manager application, including Docker Compose configuration for the entire stack (Backend, Frontend, Database, Redis).

## Technical Requirements

### Docker Configuration Files

#### Backend Dockerfile
```dockerfile
# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Install curl for health checks
RUN apk add --no-cache curl

# Copy JAR from build stage
COPY --from=build /app/target/fm-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Frontend Dockerfile (fm-web)
```dockerfile
# Multi-stage build for React application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code and build
COPY . .
RUN npm run build

# Production stage with Nginx
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built React app
COPY --from=build /app/build /usr/share/nginx/html

# Create non-root user
RUN addgroup -g 1001 -S nginx && \
    adduser -u 1001 -S nginx -G nginx

# Change ownership
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d

# Switch to non-root user
USER nginx

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3000 || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

#### Admin Frontend Dockerfile (fm-admin)
```dockerfile
# Multi-stage build for React admin application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code and build
COPY . .
RUN npm run build

# Production stage with Nginx
FROM nginx:alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built React app
COPY --from=build /app/build /usr/share/nginx/html

# Create non-root user
RUN addgroup -g 1001 -S nginx && \
    adduser -u 1001 -S nginx -G nginx

# Change ownership
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d

# Switch to non-root user
USER nginx

# Expose port
EXPOSE 3001

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3001 || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

#### Nginx Configuration (nginx.conf)
```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Logging
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

    # Basic settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;

    server {
        listen 3000;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html index.htm;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Referrer-Policy "no-referrer-when-downgrade" always;
        add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

        # Handle React Router
        location / {
            try_files $uri $uri/ /index.html;
        }

        # API proxy
        location /api/ {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # WebSocket proxy for live match
        location /ws/ {
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Static assets caching
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # Health check endpoint
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
```

#### Docker Compose Configuration
```yaml
version: '3.8'

services:
  # Database
  mysql:
    image: mysql:8.0
    container_name: fm-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootpassword}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-footballmanager}
      MYSQL_USER: ${MYSQL_USER:-fmuser}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-fmpassword}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    networks:
      - fm-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
      interval: 10s
      start_period: 40s

  # Redis for caching
  redis:
    image: redis:7-alpine
    container_name: fm-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./docker/redis/redis.conf:/usr/local/etc/redis/redis.conf
    networks:
      - fm-network
    command: redis-server /usr/local/etc/redis/redis.conf
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      timeout: 3s
      retries: 5
      interval: 10s

  # Backend Spring Boot Application
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: fm-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-docker}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE:-footballmanager}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER:-fmuser}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-fmpassword}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_JPA_HIBERNATE_DDL_AUTO: ${DDL_AUTO:-update}
      JAVA_OPTS: ${JAVA_OPTS:--Xmx1g -Xms512m}
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
    networks:
      - fm-network
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      timeout: 10s
      retries: 5
      interval: 30s
      start_period: 60s

  # Frontend Web Application
  frontend:
    build:
      context: ./fm-web
      dockerfile: Dockerfile
    container_name: fm-frontend
    restart: unless-stopped
    environment:
      REACT_APP_API_URL: ${REACT_APP_API_URL:-http://localhost:8080}
      REACT_APP_WS_URL: ${REACT_APP_WS_URL:-ws://localhost:8080}
    ports:
      - "3000:3000"
    networks:
      - fm-network
    depends_on:
      backend:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      timeout: 10s
      retries: 3
      interval: 30s

  # Admin Frontend Application
  admin:
    build:
      context: ./fm-admin
      dockerfile: Dockerfile
    container_name: fm-admin
    restart: unless-stopped
    environment:
      REACT_APP_API_URL: ${REACT_APP_API_URL:-http://localhost:8080}
    ports:
      - "3001:3001"
    networks:
      - fm-network
    depends_on:
      backend:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3001/health"]
      timeout: 10s
      retries: 3
      interval: 30s

  # Reverse Proxy (Optional - for production)
  nginx:
    image: nginx:alpine
    container_name: fm-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./docker/nginx/ssl:/etc/nginx/ssl
    networks:
      - fm-network
    depends_on:
      - frontend
      - admin
      - backend
    profiles:
      - production

  # Monitoring with Prometheus (Optional)
  prometheus:
    image: prom/prometheus:latest
    container_name: fm-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - fm-network
    profiles:
      - monitoring

  # Grafana for visualization (Optional)
  grafana:
    image: grafana/grafana:latest
    container_name: fm-grafana
    restart: unless-stopped
    ports:
      - "3002:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD:-admin}
    volumes:
      - grafana_data:/var/lib/grafana
      - ./docker/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./docker/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - fm-network
    profiles:
      - monitoring

networks:
  fm-network:
    driver: bridge

volumes:
  mysql_data:
    driver: local
  redis_data:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local
```

#### Environment Configuration (.env)
```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=rootpassword123
MYSQL_DATABASE=footballmanager
MYSQL_USER=fmuser
MYSQL_PASSWORD=fmpassword123

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=docker
DDL_AUTO=update
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC

# Frontend Configuration
REACT_APP_API_URL=http://localhost:8080
REACT_APP_WS_URL=ws://localhost:8080

# Monitoring
GRAFANA_PASSWORD=admin123

# Security
JWT_SECRET=your-super-secret-jwt-key-here-make-it-long-and-random
```

#### Production Environment (.env.production)
```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=super-secure-root-password
MYSQL_DATABASE=footballmanager_prod
MYSQL_USER=fmuser_prod
MYSQL_PASSWORD=super-secure-user-password

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=production
DDL_AUTO=validate
JAVA_OPTS=-Xmx4g -Xms2g -XX:+UseG1GC -XX:+UseStringDeduplication

# Frontend Configuration
REACT_APP_API_URL=https://api.footballmanager.com
REACT_APP_WS_URL=wss://api.footballmanager.com

# Security
JWT_SECRET=your-production-jwt-secret-key-should-be-very-long-and-random
```

### Docker Configuration Files

#### Redis Configuration (docker/redis/redis.conf)
```conf
# Redis configuration for Football Manager

# Network
bind 0.0.0.0
port 6379
timeout 300
tcp-keepalive 60

# General
daemonize no
supervised no
pidfile /var/run/redis_6379.pid
loglevel notice
logfile ""

# Snapshotting
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir /data

# Replication
replica-serve-stale-data yes
replica-read-only yes

# Security
requirepass footballmanager_redis_password

# Memory management
maxmemory 256mb
maxmemory-policy allkeys-lru

# Append only file
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

#### MySQL Initialization (docker/mysql/init/01-init.sql)
```sql
-- Football Manager Database Initialization

-- Create additional databases if needed
CREATE DATABASE IF NOT EXISTS footballmanager_test;

-- Grant permissions
GRANT ALL PRIVILEGES ON footballmanager.* TO 'fmuser'@'%';
GRANT ALL PRIVILEGES ON footballmanager_test.* TO 'fmuser'@'%';

-- Create indexes for better performance
USE footballmanager;

-- Player indexes
CREATE INDEX IF NOT EXISTS idx_player_team ON player(team_id);
CREATE INDEX IF NOT EXISTS idx_player_role ON player(role);
CREATE INDEX IF NOT EXISTS idx_player_age ON player(birth);

-- Match indexes
CREATE INDEX IF NOT EXISTS idx_match_date ON match(match_date);
CREATE INDEX IF NOT EXISTS idx_match_status ON match(status);
CREATE INDEX IF NOT EXISTS idx_match_teams ON match(home_team_id, away_team_id);

-- User indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_club ON user(club_id);

-- Performance optimizations
SET GLOBAL innodb_buffer_pool_size = 268435456; -- 256MB
SET GLOBAL query_cache_size = 67108864; -- 64MB
SET GLOBAL query_cache_type = 1;

FLUSH PRIVILEGES;
```

#### Prometheus Configuration (docker/prometheus/prometheus.yml)
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'football-manager-backend'
    static_configs:
      - targets: ['backend:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql:3306']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']
```

### Build and Deployment Scripts

#### Build Script (scripts/build.sh)
```bash
#!/bin/bash

set -e

echo "🏗️  Building Football Manager Application..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose > /dev/null 2>&1; then
    print_error "Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

# Build backend
print_status "Building backend application..."
docker-compose build backend

# Build frontend
print_status "Building frontend application..."
docker-compose build frontend

# Build admin frontend
print_status "Building admin frontend..."
docker-compose build admin

print_status "✅ All services built successfully!"

# Optional: Run tests
if [ "$1" = "--with-tests" ]; then
    print_status "Running tests..."
    
    # Backend tests
    print_status "Running backend tests..."
    docker run --rm -v "$(pwd)":/app -w /app maven:3.9.6-eclipse-temurin-21 mvn test
    
    # Frontend tests
    print_status "Running frontend tests..."
    docker run --rm -v "$(pwd)/fm-web":/app -w /app node:20-alpine npm test -- --coverage --watchAll=false
    
    # Admin tests
    print_status "Running admin tests..."
    docker run --rm -v "$(pwd)/fm-admin":/app -w /app node:20-alpine npm test -- --coverage --watchAll=false
    
    print_status "✅ All tests passed!"
fi

echo ""
print_status "🚀 Ready to deploy! Run 'docker-compose up -d' to start all services."
```

#### Deployment Script (scripts/deploy.sh)
```bash
#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[DEPLOY]${NC} $1"
}

# Configuration
ENVIRONMENT=${1:-development}
COMPOSE_FILE="docker-compose.yml"

if [ "$ENVIRONMENT" = "production" ]; then
    COMPOSE_FILE="docker-compose.yml:docker-compose.prod.yml"
    ENV_FILE=".env.production"
else
    ENV_FILE=".env"
fi

print_header "🚀 Deploying Football Manager - Environment: $ENVIRONMENT"

# Check prerequisites
print_status "Checking prerequisites..."

if [ ! -f "$ENV_FILE" ]; then
    print_error "Environment file $ENV_FILE not found!"
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found!"
    exit 1
fi

# Load environment variables
set -a
source "$ENV_FILE"
set +a

# Create necessary directories
print_status "Creating directories..."
mkdir -p logs
mkdir -p docker/mysql/data
mkdir -p docker/redis/data

# Pull latest images
print_status "Pulling latest images..."
docker-compose -f $COMPOSE_FILE pull

# Build services
print_status "Building services..."
docker-compose -f $COMPOSE_FILE build

# Stop existing services
print_status "Stopping existing services..."
docker-compose -f $COMPOSE_FILE down

# Start services
print_status "Starting services..."
if [ "$ENVIRONMENT" = "production" ]; then
    docker-compose -f $COMPOSE_FILE --profile production up -d
else
    docker-compose -f $COMPOSE_FILE up -d
fi

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check service health
print_status "Checking service health..."

services=("mysql" "redis" "backend" "frontend" "admin")
for service in "${services[@]}"; do
    if docker-compose -f $COMPOSE_FILE ps $service | grep -q "Up (healthy)"; then
        print_status "✅ $service is healthy"
    else
        print_warning "⚠️  $service may not be fully ready yet"
    fi
done

# Show running services
print_status "Running services:"
docker-compose -f $COMPOSE_FILE ps

# Show logs for any failed services
failed_services=$(docker-compose -f $COMPOSE_FILE ps --services --filter "status=exited")
if [ -n "$failed_services" ]; then
    print_error "Some services failed to start:"
    for service in $failed_services; do
        print_error "❌ $service failed"
        echo "Logs for $service:"
        docker-compose -f $COMPOSE_FILE logs --tail=20 $service
    done
    exit 1
fi

print_header "🎉 Deployment completed successfully!"
echo ""
print_status "Application URLs:"
print_status "  Frontend:  http://localhost:3000"
print_status "  Admin:     http://localhost:3001"
print_status "  Backend:   http://localhost:8080"
print_status "  API Docs:  http://localhost:8080/swagger-ui.html"

if [ "$ENVIRONMENT" = "production" ]; then
    print_status "  Main Site: http://localhost (via Nginx)"
fi

echo ""
print_status "Useful commands:"
print_status "  View logs:     docker-compose -f $COMPOSE_FILE logs -f [service]"
print_status "  Stop all:      docker-compose -f $COMPOSE_FILE down"
print_status "  Restart:       docker-compose -f $COMPOSE_FILE restart [service]"
print_status "  Shell access:  docker-compose -f $COMPOSE_FILE exec [service] sh"
```

#### Monitoring Script (scripts/monitor.sh)
```bash
#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[MONITOR]${NC} $1"
}

# Function to check service health
check_service_health() {
    local service=$1
    local url=$2
    
    if curl -f -s "$url" > /dev/null; then
        print_status "✅ $service is healthy"
        return 0
    else
        print_error "❌ $service is unhealthy"
        return 1
    fi
}

# Function to get container stats
get_container_stats() {
    local container=$1
    
    if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "$container"; then
        local stats=$(docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" "$container")
        echo "$stats"
    else
        print_error "Container $container is not running"
    fi
}

print_header "🔍 Football Manager System Monitor"

# Check Docker services
print_status "Checking Docker services..."
docker-compose ps

echo ""
print_status "Service Health Checks:"

# Health check URLs
check_service_health "Backend" "http://localhost:8080/actuator/health"
check_service_health "Frontend" "http://localhost:3000/health"
check_service_health "Admin" "http://localhost:3001/health"
check_service_health "MySQL" "http://localhost:8080/actuator/health/db"

echo ""
print_status "Container Resource Usage:"

# Get stats for all containers
containers=("fm-backend" "fm-frontend" "fm-admin" "fm-mysql" "fm-redis")
for container in "${containers[@]}"; do
    if docker ps --format "{{.Names}}" | grep -q "$container"; then
        echo ""
        print_status "Stats for $container:"
        get_container_stats "$container"
    fi
done

echo ""
print_status "Disk Usage:"
df -h | grep -E "(Filesystem|/dev/)"

echo ""
print_status "Memory Usage:"
free -h

echo ""
print_status "Recent Logs (last 10 lines):"
echo "Backend logs:"
docker-compose logs --tail=10 backend

echo ""
print_status "Database Status:"
docker-compose exec mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW PROCESSLIST;" 2>/dev/null || print_warning "Could not connect to database"

echo ""
print_status "Redis Status:"
docker-compose exec redis redis-cli ping 2>/dev/null || print_warning "Could not connect to Redis"

echo ""
print_header "📊 Monitoring completed"
print_status "For continuous monitoring, consider setting up Prometheus + Grafana"
print_status "Run: docker-compose --profile monitoring up -d"
```

### Production Optimizations

#### Production Docker Compose Override (docker-compose.prod.yml)
```yaml
version: '3.8'

services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_prod_data:/var/lib/mysql
    command: >
      --default-authentication-plugin=mysql_native_password
      --innodb-buffer-pool-size=1G
      --innodb-log-file-size=256M
      --max-connections=200
      --query-cache-size=64M
      --query-cache-type=1

  redis:
    command: redis-server /usr/local/etc/redis/redis.conf --maxmemory 512mb

  backend:
    environment:
      SPRING_PROFILES_ACTIVE: production
      JAVA_OPTS: -Xmx4g -Xms2g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=200
    deploy:
      resources:
        limits:
          memory: 4G
        reservations:
          memory: 2G

  frontend:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  admin:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

volumes:
  mysql_prod_data:
    driver: local
```

### Security Configurations

#### Docker Security Best Practices
```dockerfile
# Security-focused Dockerfile additions

# Use specific versions instead of 'latest'
FROM eclipse-temurin:21.0.1_12-jre-alpine

# Update packages and remove package manager
RUN apk update && apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Create non-root user with specific UID/GID
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set file permissions
COPY --chown=appuser:appgroup --chmod=755 app.jar /app/

# Use non-root user
USER appuser

# Remove unnecessary packages and files
RUN rm -rf /tmp/* /var/tmp/*

# Set security labels
LABEL security.scan="enabled"
LABEL security.non-root="true"
```

### Testing and Validation

#### Docker Health Check Script (scripts/health-check.sh)
```bash
#!/bin/bash

set -e

# Health check for all services
services=("backend:8080/actuator/health" "frontend:3000/health" "admin:3001/health")

for service in "${services[@]}"; do
    IFS=':' read -r name endpoint <<< "$service"
    
    echo "Checking $name..."
    
    if curl -f -s "http://localhost:$endpoint" > /dev/null; then
        echo "✅ $name is healthy"
    else
        echo "❌ $name is unhealthy"
        exit 1
    fi
done

echo "🎉 All services are healthy!"
```

### Documentation

#### Docker Setup README (docker/README.md)
```markdown
# Football Manager Docker Setup

This directory contains all Docker-related configuration files for the Football Manager application.

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd football-manager
   ```

2. **Copy environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Build and start services**
   ```bash
   ./scripts/build.sh
   ./scripts/deploy.sh
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Admin: http://localhost:3001
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

## Services

### Core Services
- **backend**: Spring Boot application (Port 8080)
- **frontend**: React web application (Port 3000)
- **admin**: React admin application (Port 3001)
- **mysql**: MySQL database (Port 3306)
- **redis**: Redis cache (Port 6379)

### Optional Services
- **nginx**: Reverse proxy (Port 80/443) - Production profile
- **prometheus**: Metrics collection (Port 9090) - Monitoring profile
- **grafana**: Metrics visualization (Port 3002) - Monitoring profile

## Profiles

### Development (Default)
```bash
docker-compose up -d
```

### Production
```bash
docker-compose --profile production up -d
```

### With Monitoring
```bash
docker-compose --profile monitoring up -d
```

## Useful Commands

### Service Management
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart a specific service
docker-compose restart backend

# View logs
docker-compose logs -f backend

# Execute command in container
docker-compose exec backend bash
```

### Maintenance
```bash
# Update images
docker-compose pull

# Rebuild services
docker-compose build

# Clean up
docker system prune -a
```

### Monitoring
```bash
# Check service status
docker-compose ps

# View resource usage
docker stats

# Run health checks
./scripts/health-check.sh
```

## Environment Variables

Key environment variables in `.env`:

- `MYSQL_ROOT_PASSWORD`: MySQL root password
- `MYSQL_DATABASE`: Database name
- `MYSQL_USER`: Database user
- `MYSQL_PASSWORD`: Database password
- `SPRING_PROFILES_ACTIVE`: Spring Boot profile
- `JAVA_OPTS`: JVM options
- `REACT_APP_API_URL`: Backend API URL for frontend

## Troubleshooting

### Common Issues

1. **Port conflicts**
   - Change ports in docker-compose.yml
   - Check for running services: `netstat -tulpn`

2. **Database connection issues**
   - Verify MySQL is healthy: `docker-compose logs mysql`
   - Check connection string in backend logs

3. **Frontend not loading**
   - Check nginx configuration
   - Verify API proxy settings

4. **Out of memory**
   - Adjust JAVA_OPTS in .env
   - Increase Docker memory limits

### Logs and Debugging

```bash
# View all logs
docker-compose logs

# Follow specific service logs
docker-compose logs -f backend

# Check container health
docker-compose ps
```

## Security Considerations

- All services run as non-root users
- Secrets are managed via environment variables
- Network isolation using Docker networks
- Regular security updates for base images
- Health checks for all services

## Performance Tuning

### Database Optimization
- Configured InnoDB buffer pool size
- Query cache enabled
- Connection pooling configured

### Application Optimization
- JVM tuning with G1GC
- Connection pooling
- Redis caching enabled

### Frontend Optimization
- Gzip compression enabled
- Static asset caching
- Bundle optimization
```

## Implementation Notes

1. **Security**: All containers run as non-root users with specific UIDs/GIDs
2. **Performance**: Optimized JVM settings and database configuration for production
3. **Monitoring**: Health checks for all services with proper timeouts
4. **Scalability**: Services can be scaled horizontally using Docker Swarm or Kubernetes
5. **Development**: Hot reloading supported for development environment
6. **Production**: Separate configuration with optimized settings
7. **Backup**: Volume mounts for persistent data with backup strategies

## Dependencies

- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM minimum (16GB recommended for production)
- 20GB disk space minimum
- Linux/macOS/Windows with WSL2

## Testing Strategy

- Health checks for all services
- Integration tests in containerized environment
- Load testing with containerized setup
- Security scanning of Docker images
- Automated deployment testing