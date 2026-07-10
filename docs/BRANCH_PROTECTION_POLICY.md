# Branch Protection Policy

Este documento define uma politica recomendada de branch protection para este repositorio.

## Objetivos

- Impedir merge de codigo sem validacao automatica.
- Garantir revisao minima por pares.
- Reduzir regressao em `main` e `develop`.

## Status Checks Necessarios

Com base nos workflows atuais:

- Workflow: `CI Quality`
- Jobs/checks:
  - `Build and Test`
  - `Sonar Analysis` (quando os secrets de Sonar estiverem configurados)

## Regra Recomendada para `main`

Aplicar branch protection com:

1. Require a pull request before merging: **enabled**
2. Required approvals: **1** (ideal: 2 para time maior)
3. Dismiss stale pull request approvals when new commits are pushed: **enabled**
4. Require review from CODEOWNERS: **enabled** (quando existir CODEOWNERS)
5. Require conversation resolution before merging: **enabled**
6. Require status checks to pass before merging: **enabled**
7. Require branches to be up to date before merging: **enabled**
8. Required checks:
   - `Build and Test`
   - `Sonar Analysis`
9. Require signed commits: **optional/recommended**
10. Require linear history: **optional/recommended**
11. Restrict who can push to matching branches: **enabled** (admins e bot de release)
12. Include administrators: **enabled**
13. Allow force pushes: **disabled**
14. Allow deletions: **disabled**

## Regra Recomendada para `develop`

Aplicar branch protection com:

1. Require a pull request before merging: **enabled**
2. Required approvals: **1**
3. Dismiss stale pull request approvals when new commits are pushed: **enabled**
4. Require conversation resolution before merging: **enabled**
5. Require status checks to pass before merging: **enabled**
6. Require branches to be up to date before merging: **enabled**
7. Required checks:
   - `Build and Test`
8. Include administrators: **enabled**
9. Allow force pushes: **disabled**
10. Allow deletions: **disabled**

Observacao: em `develop`, manter `Sonar Analysis` opcional pode acelerar fluxo em times pequenos. Em times maduros, tambem pode ser obrigatorio.

## Sequencia de Aplicacao

1. Garantir que os workflows estejam na branch default e executem ao menos uma vez.
2. Configurar secrets do Sonar no repositorio:
   - `SONAR_TOKEN`
   - `SONAR_HOST_URL`
   - `SONAR_PROJECT_KEY`
3. Em GitHub: `Settings` -> `Branches` -> `Add branch protection rule`.
4. Criar regra para `main` com os checks listados.
5. Criar regra para `develop` com os checks listados.
6. Validar em um PR de teste.

## Troubleshooting Rapido

- Check nao aparece na lista:
  - Execute o workflow pelo menos uma vez na branch alvo.
- Sonar nao executa:
  - Verifique os 3 secrets e se o servidor Sonar esta acessivel.
- PR bloqueado sem motivo:
  - Confira se o nome do check requerido corresponde exatamente ao nome do job.
