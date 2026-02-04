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
