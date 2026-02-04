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
