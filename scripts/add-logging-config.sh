#!/bin/bash

# Script para adicionar configurações de logging para todos os microsserviços

COMPOSE_FILE="/home/mayconaraujo/Documents/ifood_clone/docker-compose.yml"

# Lista dos serviços para atualizar
services=(
    "user-service"
    "restaurant-service" 
    "menu-service"
    "order-service"
    "payment-service"
    "notification-service"
    "delivery-service"
    "review-service"
)

# Backup do arquivo original
cp "$COMPOSE_FILE" "$COMPOSE_FILE.backup"

# Para cada serviço, adicionar labels e volumes de logging
for service in "${services[@]}"; do
    echo "Atualizando $service..."
    
    # Usar sed para adicionar as configurações após a seção environment de cada serviço
    sed -i "/$service:/,/^  [a-zA-Z]/ {
        /KAFKA_BOOTSTRAP_SERVERS: kafka:9092/a\\
    labels:\\
      - \"logging=promtail\"\\
    volumes:\\
      - ./logs:/app/logs
    }" "$COMPOSE_FILE"
    
    # Para notification-service e review-service que usam MongoDB
    sed -i "/$service:/,/^  [a-zA-Z]/ {
        /MONGODB_URI:/a\\
    labels:\\
      - \"logging=promtail\"\\
    volumes:\\
      - ./logs:/app/logs
    }" "$COMPOSE_FILE"
done

echo "Configurações de logging adicionadas para todos os microsserviços!"