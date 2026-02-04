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
