# GitHub Actions Hardening

Este documento registra as decisoes de hardening aplicadas aos workflows.

## O que foi aplicado

- Acoes core e Docker foram fixadas por commit SHA (immutable pinning).
- Mantivemos comentario com a major/tag correspondente para facilitar manutencao.
- Dependabot continua ativo para detectar updates em `github-actions`.

## SHAs pinados atualmente

- `actions/checkout@v5` -> `93cb6efe18208431cddfb8368fd83d5badbf9bfd`
- `actions/setup-java@v5` -> `0f481fcb613427c0f801b606911222b5b6f3083a`
- `actions/upload-artifact@v5` -> `330a01c490aca151604b8cf639adc76d48f6c5d4`
- `docker/setup-buildx-action@v3` -> `8d2750c68a42422c14e847fe6c8ac0403b4cbd6f`
- `docker/login-action@v3` -> `c94ce9fb468520275223c153574b00df6fe4bcc9`
- `docker/metadata-action@v5` -> `c299e40c65443455700f0fdfc63efafe5b349051`
- `docker/build-push-action@v6` -> `10e90e3645eae34f1e60eeb005ba3a3d33f178e8`

## Como atualizar com seguranca

1. Atualize a tag/major da action (ex.: `v5`, `v6`).
2. Busque o SHA oficial da tag na API do GitHub:

```bash
curl -s https://api.github.com/repos/<owner>/<repo>/git/ref/tags/<tag>
```

3. Troque `uses: owner/repo@<sha>` e mantenha comentario da tag.
4. Rode workflow manual para validacao.

## Beneficios

- Reduz risco de supply chain por mutacao de tags.
- Aumenta reprodutibilidade e auditabilidade dos pipelines.
