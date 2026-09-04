script:pre-request {
  // Log da requisição que será executada
  console.log(`🚀 Executando: ${req.getMethod()} ${req.getUrl()}`);

  // Adicionar timestamp para todas as requisições
  req.setHeader("X-Request-Timestamp", new Date().toISOString());

  // Adicionar User-Agent customizado
  req.setHeader("User-Agent", "Bruno-Auth-Service-Collection/1.0.0");
}

script:post-response {
  // Log da resposta
  console.log(`📨 Resposta: ${res.getStatus()} - ${res.getStatusText()}`);

  // Log de erro para status de erro
  if (res.getStatus() >= 400) {
    console.error(`❌ Erro ${res.getStatus()}: ${JSON.stringify(res.getBody(), null, 2)}`);
  } else {
    console.log(`✅ Sucesso: ${res.getBody()?.message || 'OK'}`);
  }
}