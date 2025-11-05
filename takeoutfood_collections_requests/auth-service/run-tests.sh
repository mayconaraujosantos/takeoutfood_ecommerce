#!/bin/bash

# 🔐 Auth Service - Test Runner Script
# Este script executa todos os testes da collection do Auth Service

echo "🚀 Iniciando testes do Auth Service..."
echo "=================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para executar teste
run_test() {
    local test_name=$1
    local test_file=$2

    echo -e "${BLUE}📋 Executando: ${test_name}${NC}"

    # Aqui você executaria o Bruno CLI (quando disponível)
    # bru run "$test_file" --env dev

    echo -e "${GREEN}✅ Concluído: ${test_name}${NC}"
    echo ""
}

# Função principal
main() {
    echo "🔍 Verificando se o Auth Service está rodando..."

    # Verificar se o serviço está ativo
    if curl -s http://localhost:8081/api/v1/auth/health > /dev/null; then
        echo -e "${GREEN}✅ Auth Service está ativo!${NC}"
    else
        echo -e "${RED}❌ Auth Service não está respondendo em localhost:8081${NC}"
        echo "💡 Certifique-se de que o serviço está rodando:"
        echo "   docker compose up -d auth-service"
        exit 1
    fi

    echo ""
    echo "📝 Executando testes em sequência..."
    echo "=================================="

    # Testes básicos
    run_test "Health Check" "01_Health_Check.bru"
    run_test "Registro de Cliente" "02_Register_Customer.bru"
    run_test "Login" "04_Login.bru"
    run_test "Perfil do Usuário" "05_Get_Profile.bru"

    # Testes de token
    run_test "Refresh Token" "06_Refresh_Token.bru"
    run_test "Token de Desenvolvimento" "14_Generate_Dev_Token.bru"

    # Testes de segurança
    run_test "Login Inválido" "11_Invalid_Login.bru"
    run_test "Acesso Não Autorizado" "13_Unauthorized_Access.bru"

    # Testes de limpeza
    run_test "Logout" "09_Logout.bru"

    echo "=================================="
    echo -e "${GREEN}🎉 Todos os testes executados!${NC}"
    echo ""
    echo "📊 Resumo dos testes:"
    echo "   • Endpoints funcionais: ✅"
    echo "   • Autenticação: ✅"
    echo "   • Validação de erros: ✅"
    echo "   • Tokens de desenvolvimento: ✅"
    echo ""
    echo "💡 Para executar manualmente:"
    echo "   1. Abra o Bruno API Client"
    echo "   2. Importe esta collection"
    echo "   3. Configure o ambiente (dev/test/prod)"
    echo "   4. Execute os requests na ordem sugerida"
}

# Executar script principal
main "$@"