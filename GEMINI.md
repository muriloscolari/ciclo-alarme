# Diretrizes e Convenções de Desenvolvimento do Ciclo

Este documento contém as regras, padrões de código e convenções de fluxo de trabalho que o **Gemini / Antigravity** e qualquer desenvolvedor devem seguir ao trabalhar neste projeto.

---

## 1. Visão Geral e Stack Tecnológica
- **Projeto:** Ciclo (`me.muriloscolari.ciclo`)
- **Plataforma:** Android 8.0+ (`minSdk 26`, `targetSdk 35`)
- **Linguagem & Framework:** Kotlin 2.x, Jetpack Compose com Material 3
- **Injeção de Dependências:** Dagger Hilt
- **Persistência de Dados:** Room Database (`CicloDatabase`) + AndroidX DataStore Preferences
- **Efeitos Visuais:** Haze (Frosted glass / blur dinâmico)
- **Otimização:** R8 / ProGuard ativado em release (`minifyEnabled = true`, `shrinkResources = true`)

---

## 2. Convenção Estrita de Versionamento e Releases (CI/CD)
> [!IMPORTANT]
> **REGRAS INEGOCIÁVEIS DE RELEASES:**
> - As releases são **100% automatizadas pelo GitHub Actions** (`.github/workflows/release.yml`) a cada push na branch `main`.
> - **Padrão de Nome da Release:** `Ciclo v1.0.<run_number>` (ex: `Ciclo v1.0.12`).
> - **Padrão de Tag:** `v1.0.<run_number>` (ex: `v1.0.12`).
> - **Padrão de Arquivo:** `Ciclo-v1.0.<run_number>.apk`.
> - **Fórmula de VersionCode:** Deve sempre usar a base `10000 + runNumber` (no CI) e `10000 + gitCommitCount` (localmente). Isso garante que o `versionCode` cresça de forma monotônica e previne o erro de downgrade do Android (*"Como o pacote parece ser inválido, o app não foi instalado"*).
> - **PROIBIDO:** Criar tags ou releases manuais com títulos descritivos (ex: `v1.2.0 - Nova Funcionalidade`). Toda release deve ser disparada por commit na `main`.

---

## 3. Padrão de Código, Interface e Internacionalização
> [!WARNING]
> - **Zero Hardcoded Text:** NUNCA escreva strings literais em arquivos Compose. Todas as strings visíveis ao usuário **DEVEM** ser cadastradas em `app/src/main/res/values/strings.xml` ou `plurals.xml`. O Lint (`HardcodedText`) está configurado para falhar a compilação se encontrar texto estático no código.
> - **Idioma Oficial:** Português do Brasil (`pt-BR`).
> - **Design System de Vidro:** Use consistentemente os componentes customizados:
>   - `GlassSurface` e `GlassNavigationBar`
>   - `AuroraBackground` para o fundo imersivo
>   - Paleta oficial: `PrimaryLavender`, `DawnWarmOrange`, `GlassFill`, `GlassBorder`, `TextPrimary`, `TextSecondary`, `TextTertiary`.

---

## 4. Arquitetura de Dados e Estado
- **Fonte Única de Verdade (Single Source of Truth):**
  - O banco Room (`CicloDatabase`) é a única fonte da verdade para o estado de alarmes (`ActiveAlarmEntity` na tabela `active_alarms`) e histórico de acionamentos (`AlarmHistoryEntity` na tabela `alarm_history`).
  - O DataStore (`SettingsRepository`) deve ser utilizado exclusivamente para configurações e preferências do usuário (duração de sono, tema, lead time do amanhecer).
- **Reatividade e Sincronização:**
  - Telas como `CalculatorScreen` e `ArmedAlarmsScreen` devem observar os Flows reativos do Room diretamente via `ViewModel`.
  - Ao desarmar um alarme, garanta a limpeza tanto do Room (`activeAlarmRepository.removeActiveAlarm()`) quanto das preferências legadas do DataStore (`settingsRepository.clearActiveAlarm()`) para que a UI responda imediatamente em todas as abas.

---

## 5. Confiabilidade e HyperOS / Xiaomi
- Alarmes do Ciclo utilizam prioritariamente `AlarmManager.setAlarmClock()`, que possui privilégio RTC máximo no Android (desperta mesmo em modo Doze profundo).
- Permissões essenciais mantidas no `AndroidManifest.xml`:
  - `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`
  - `USE_FULL_SCREEN_INTENT`, `POST_NOTIFICATIONS`, `WAKE_LOCK`
- Respeitar as diretrizes de otimização de bateria do HyperOS presentes no card de orientações em Ajustes (Início Automático, Bateria Sem Restrições, Exibir na Tela de Bloqueio).

---

## 6. Checklist Obrigatório Antes de Commitar
Antes de criar qualquer commit ou abrir PR, execute os seguintes comandos no terminal:

```bash
# 1. Executar testes unitários
./gradlew testDebugUnitTest

# 2. Executar análise estática do Lint (não pode haver nenhum erro)
./gradlew lintDebug

# 3. Validar compilação com R8 e empacotamento
./gradlew assembleRelease
```
