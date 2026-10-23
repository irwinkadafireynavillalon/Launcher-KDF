# Orquestador Híbrido Hyper Lux

Este plan describe la finalización de la aplicación Orquestador Híbrido Hyper Lux, incluyendo la IA seleccionable, integración de IA local, historial de chats y 10 temas vibrantes.

### User Review & Critical Decisions

> [!IMPORTANT]
> Confirmar las siguientes decisiones antes de proceder con la implementación masiva:

- **Decisión 1**: Comportamiento Híbrido. El usuario selecciona la IA (Gemini 3.1 Pro nube o Local @ 127.0.0.1:8080). Si no hay selección, se solicitará al usuario.
- **Decisión 2**: Historial de Chat. Implementaremos un `ModalBottomSheet` que se activa desde el icono de chat para gestionar historial (borrar, exportar, nuevo chat).
- **Decisión 3**: 10 Temas Vibrantes. Implementaremos un gestor de temas (`ThemeManager`) con 10 paletas predefinidas configurables desde los ajustes.

### 1. Overview & Core Concept

- **What It Does**: Una aplicación de chat con orquestación híbrida que permite cambiar dinámicamente entre Gemini 3.1 Pro (Nube) y un servidor local.
- **Target Audience**: Usuarios avanzados que requieren flexibilidad entre nube y local con alta personalización.
- **Key Value**: Orquestación inteligente, privacidad (local) y potencia (nube) en una sola interfaz limpia.

### 2. User Experience & Visual Design

- **Key User Flows**: 
    - Cambio de modelo: Menú de selección rápido en la barra superior.
    - Acceso a historial: Icono de chat -> `ModalBottomSheet`.
    - Ajustes: Pantalla de ajustes con selector de 10 temas (OLED Noir, Cyberpunk, etc.).
- **Visual Identity & Theme**:
    - *Aesthetic Direction*: Utilitario premium, limpio y vibrante.
    - *Color Palette*: 10 paletas definidas con acentos de alto contraste.
    - *Component Styling*: Material 3 con esquinas redondeadas y profundidad tonal.

### 3. Key Product Decisions & Trade-Offs

- **Decision 1**: Gestión Híbrida.
    - *Chosen Approach*: `ViewModel` central gestionando `SelectedModelFlow`.
    - *Why*: Permite conmutación atómica y manejo de estados de error independientes.
- **Decision 2**: Servidor Local.
    - *Chosen Approach*: `Ktor` cliente en `InternalAiManager` apuntando a `127.0.0.1:8080`.
    - *Why*: Estándar para comunicación local rápida y eficiente.

### 4. Technical Architecture

```text
┌──────────────────────┐
│   UI: Orchestrator   │
│   (Screen/Compose)   │
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│   ViewModel: Orchest │
│  (State Management)  │
└────┬────────────┬────┘
     │            │
┌────▼────┐ ┌─────▼─────┐ ┌──────────────┐
│ Gemini  │ │ LocalAi   │ │ HistoryStore │
│ Repo    │ │ Manager   │ │ (Firestore)  │
└────┬────┘ └─────┬─────┘ └──────┬───────┘
     │            │              │
┌────▼────┐ ┌─────▼─────┐ ┌──────▼───────┐
│Cloud API│ │Local Ktor │ │Cloud DB      │
└─────────┘ └───────────┘ └──────────────┘
```
