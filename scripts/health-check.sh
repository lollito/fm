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
