# Git Flow — Convenção de Branches

Este projeto usa Git Flow. Siga estas regras para manter `main` e `develop` sincronizadas sem merges manuais de emergência.

## Branches permanentes

- **`main`** — código em produção. Só recebe merge de `release/*` ou `hotfix/*`. **Nunca commit direto.**
- **`develop`** — linha de integração. Recebe merge de `feature/*`. Base para novas `release/*`.

## Branches temporárias

| Branch      | Nasce de   | Volta para        | Quando usar                          |
|-------------|------------|--------------------|---------------------------------------|
| `feature/*` | `develop`  | `develop`          | Nova funcionalidade                   |
| `release/*` | `develop`  | `main` + `develop` | Preparar uma versão para lançamento   |
| `hotfix/*`  | `main`     | `main` + `develop` | Correção urgente em produção          |

## Fluxo básico

```bash
# Nova feature
git checkout develop
git checkout -b feature/nome-da-feature
# ... commits ...
git checkout develop
git merge --no-ff feature/nome-da-feature

# Release
git checkout develop
git checkout -b release/1.2.0
# ... ajustes finais, bump de versão ...
git checkout main
git merge --no-ff release/1.2.0
git tag v1.2.0
git checkout develop
git merge --no-ff release/1.2.0

# Hotfix urgente
git checkout main
git checkout -b hotfix/corrige-bug-critico
# ... commit do fix ...
git checkout main
git merge --no-ff hotfix/corrige-bug-critico
git tag v1.2.1
git checkout develop
git merge --no-ff hotfix/corrige-bug-critico
```

## Regra de ouro

Nunca faça `git commit` estando em `main`. Se precisar de correção rápida em produção, abra `hotfix/*` a partir de `main` e sempre traga de volta para `develop` ao final.

## Exceção automatizada: bump de tags de imagem

O job `update-gitops` (`.github/workflows/ci-cd.yml`) roda após todo merge em `main` e atualiza as tags de imagem em `gitops/apps/services/values/*.yaml` para o SHA recém-buildado — é assim que o Argo CD pega o novo deploy. Como `main` é protegida, esse job não pode mais dar `git push` direto: ele abre uma PR a partir de uma branch `chore/gitops-bump-<sha>` e a mergeia sozinho. O Git Flow Guard libera especificamente esse prefixo, só para esse fim.
