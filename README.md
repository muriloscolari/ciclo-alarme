# Ciclo 🌙

[![CI](https://github.com/muriloscolari/ciclo-alarme/actions/workflows/ci.yml/badge.svg)](https://github.com/muriloscolari/ciclo-alarme/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/muriloscolari/ciclo-alarme?color=7C3AED&label=Release)](https://github.com/muriloscolari/ciclo-alarme/releases/latest)
[![Licença GPL-3.0](https://img.shields.io/badge/Licen%C3%A7a-GPL--3.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)

**Ciclo** é um aplicativo Android nativo, minimalista e moderno, projetado para calcular horários de despertar e de deitar com base nos ciclos naturais de sono (90 a 120 minutos).

Ao selecionar um horário ideal, o Ciclo cria o alarme diretamente no **aplicativo de Relógio nativo do aparelho** (com foco principal em aparelhos **Xiaomi / HyperOS / MIUI**, além de **Samsung One UI** e **Android padrão**).

[⬇️ **Baixar o APK mais recente**](https://github.com/muriloscolari/ciclo-alarme/releases/latest)

---

## ✨ Funcionalidades

- **Dormir agora:** calcula os melhores horários para acordar considerando o tempo para pegar no sono e ciclos completos.
- **Vou dormir às X:** planeje a hora de ir para a cama e veja os horários ideais de despertar.
- **Acordar às X:** defina que horas precisa levantar e descubra a hora exata de deitar (com opção de alarme simples se já passou do horário).
- **Opção Recomendada (5 ciclos · ~7h30):** destaque visual para o descanso ideal sem sensação de cansaço.
- **Integração nativa:** utiliza `AlarmClock.ACTION_SET_ALARM` para acionar o alarme do sistema sem precisar de serviços em segundo plano drenando bateria.
- **Histórico completo:** registre os alarmes criados com opção de recriação rápida e gerenciamento.
- **Design System Dark-first com Glassmorphism:** visual translúcido moderno com efeito de desfoque (Haze) e aurora boreal em tons índigo e lavanda.

---

## 🛠️ Stack & Arquitetura

O projeto segue as diretrizes modernas recomendadas pelo Google:

- **Linguagem:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3
- **Injeção de Dependências:** Hilt (Dagger)
- **Banco de Dados local:** Room (SQLite) com KSP
- **Configurações:** Jetpack DataStore Preferences
- **Glassmorphism / Blur:** [Haze](https://github.com/chrisbanes/haze)
- **Arquitetura:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)

---

## 📄 Licença

Este projeto é distribuído sob a licença [GNU General Public License v3.0 (GPL-3.0)](LICENSE).
