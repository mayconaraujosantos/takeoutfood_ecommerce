#!/bin/bash

# Script para inicializar a stack de observabilidade do iFood Clone

set -e

echo "🚀 Iniciando Stack de Observabilidade iFood Clone..."

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

# Criar diretórios necessários
echo "📁 Criando diretórios de logs..."
mkdir -p logs
chmod 755 logs

# Criar rede se não existir
echo "🌐 Configurando rede Docker..."
docker network create ifood-network 2>/dev/null || true

# Parar containers existentes se necessário
echo "⏹️ Parando containers existentes de observabilidade..."
docker-compose -f docker-compose.observability.yml down 2>/dev/null || true

# Limpar volumes órfãos
echo "🧹 Limpando volumes órfãos..."
docker volume prune -f

# Subir stack de observabilidade
echo "🔧 Iniciando stack de observabilidade..."
docker-compose -f docker-compose.observability.yml up -d

# Aguardar serviços ficarem prontos
echo "⏳ Aguardando serviços ficarem prontos..."

# Verificar se Loki está rodando
for i in {1..30}; do
    if curl -s http://localhost:3100/ready > /dev/null 2>&1; then
        echo "✅ Loki está pronto!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Loki não ficou pronto após 30 tentativas"
        exit 1
    fi
    sleep 2
done

# Verificar se Prometheus está rodando
for i in {1..30}; do
    if curl -s http://localhost:9090/-/ready > /dev/null 2>&1; then
        echo "✅ Prometheus está pronto!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Prometheus não ficou pronto após 30 tentativas"
        exit 1
    fi
    sleep 2
done

# Verificar se Grafana está rodando
for i in {1..30}; do
    if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
        echo "✅ Grafana está pronto!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Grafana não ficou pronto após 30 tentativas"
        exit 1
    fi
    sleep 2
done

# Mostrar status dos containers
echo "📊 Status dos containers:"
docker-compose -f docker-compose.observability.yml ps

echo ""
echo "🎉 Stack de Observabilidade iniciada com sucesso!"
echo ""
echo "🔗 URLs disponíveis:"
echo "   📊 Grafana:    http://localhost:3000 (admin/ifood_grafana_pass)"
echo "   📈 Prometheus: http://localhost:9090"
echo "   📜 Loki:       http://localhost:3100"
echo "   🔍 Jaeger:     http://localhost:16686"
echo ""
echo "📋 Próximos passos:"
echo "   1. Execute os microsserviços: docker-compose up -d"
echo "   2. Acesse o Grafana em http://localhost:3000"
echo "   3. Verifique os dashboards na pasta 'IFood Clone'"
echo "   4. Monitore logs em tempo real no painel de Logs"
echo ""
echo "🔧 Para parar a stack: docker-compose -f docker-compose.observability.yml down"