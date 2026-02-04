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

if [ "$ENVIRONMENT" = "production" ]; then
    export COMPOSE_FILE="docker-compose.yml:docker-compose.prod.yml"
    ENV_FILE=".env.production"
else
    export COMPOSE_FILE="docker-compose.yml"
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
docker-compose pull

# Build services
print_status "Building services..."
docker-compose build

# Stop existing services
print_status "Stopping existing services..."
docker-compose down

# Start services
print_status "Starting services..."
if [ "$ENVIRONMENT" = "production" ]; then
    docker-compose --profile production up -d
else
    docker-compose up -d
fi

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check service health
print_status "Checking service health..."

services=("mysql" "redis" "backend" "frontend" "admin")
for service in "${services[@]}"; do
    if docker-compose ps $service | grep -q "Up (healthy)"; then
        print_status "✅ $service is healthy"
    else
        print_warning "⚠️  $service may not be fully ready yet"
    fi
done

# Show running services
print_status "Running services:"
docker-compose ps

# Show logs for any failed services
failed_services=$(docker-compose ps --services --filter "status=exited")
if [ -n "$failed_services" ]; then
    print_error "Some services failed to start:"
    for service in $failed_services; do
        print_error "❌ $service failed"
        echo "Logs for $service:"
        docker-compose logs --tail=20 $service
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
print_status "  View logs:     docker-compose logs -f [service]"
print_status "  Stop all:      docker-compose down"
print_status "  Restart:       docker-compose restart [service]"
print_status "  Shell access:  docker-compose exec [service] sh"
