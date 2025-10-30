#!/bin/bash

# Build and Deploy Script for iFood Clone

set -e

echo "🚀 Starting iFood Clone build and deployment..."

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
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Clean and build all services
print_status "Building all microservices..."
mvn clean package -DskipTests

# Build Docker images
print_status "Building Docker images..."

services=("config-server" "service-discovery" "api-gateway" "auth-service")

for service in "${services[@]}"; do
    print_status "Building Docker image for $service..."
    cd $service
    docker build -t ifood-clone/$service:latest .
    cd ..
done

# Start infrastructure services first
print_status "Starting infrastructure services..."
docker-compose up -d zookeeper kafka postgres mongodb redis

# Wait for infrastructure to be ready
print_status "Waiting for infrastructure services to be ready..."
sleep 30

# Start config server first
print_status "Starting Config Server..."
docker-compose up -d config-server

# Wait for config server
print_status "Waiting for Config Server to be ready..."
sleep 20

# Start service discovery
print_status "Starting Service Discovery..."
docker-compose up -d service-discovery

# Wait for service discovery
print_status "Waiting for Service Discovery to be ready..."
sleep 20

# Start other services
print_status "Starting other microservices..."
docker-compose up -d

print_status "✅ Deployment completed!"
print_status "🌐 Services are starting up. Please wait a few minutes for all services to be ready."
print_status ""
print_status "📊 Access points:"
print_status "• Eureka Dashboard: http://localhost:8761"
print_status "• API Gateway: http://localhost:8080"
print_status "• Config Server: http://localhost:8888"
print_status ""
print_status "To check logs: docker-compose logs -f [service-name]"
print_status "To stop all services: docker-compose down"