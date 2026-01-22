# WallPanel Vity - Product Context & Architecture

## 1. Product Vision
**WallPanel Vity** es un dashboard nativo para Home Assistant en Android, diseñado para reemplazar a "Fully Kiosk Browser" superando sus capacidades de integración.
El objetivo es una aplicación **"Device Owner" (Kiosk)** que controle el hardware (pantalla, energía, cámara) para servir como interfaz domótica 24/7.

## 2. Technical Stack & Constraints
- **Language:** Migración activa de Java a **Kotlin** (Target: 100% Kotlin).
- **Min SDK:** Android 10 (API 29). Target SDK: Latest stable.
- **Architecture:** MVVM.
- **DI (Dependency Injection):** Dagger (Legacy) -> Migrando a Hilt/Koin bajo demanda.
- **Async:** RxJava (Legacy) -> Migrar a **Kotlin Coroutines/Flow**.

## 3. Core Features (The "Fully Kiosk" Killer)
### A. True Kiosk Mode (Prioridad Alta)
- Implementar `startLockTask()` para bloqueo total.
- Capacidad de actuar como Launcher (Home App).
- Protección por PIN para salir del modo kiosco.

### B. Smart Energy & Wake-up (Updated)
- **Goal:** Comportamiento similar a Fully Kiosk. La pantalla debe poder **apagarse/bloquearse** (Screen Off) para ahorro máximo, pero despertar instantáneamente ante eventos.
- **Wake-up Triggers:**
    1. **Motion Detection:** Usar la cámara frontal en segundo plano (si el hardware lo permite con pantalla apagada) para despertar el dispositivo.
    2. **Face Detection:** Identificar si hay una cara mirando para encender (evitar falsos positivos de luces/sombras).
    3. **MQTT/REST:** Comando remoto `screen_on`.
- **Fallback Strategy:** Si Android mata la cámara al apagar la pantalla (hardware limitation), implementar "Zero Brightness Overlay" (Fake Sleep) como alternativa transparente al usuario.

### C. Home Assistant Integration
- **MQTT Autodiscovery:** Enviar payload a `homeassistant/device/.../config` al iniciar. Entidades:
    - *Binary Sensor:* Motion, Face Detected.
    - *Sensor:* Battery Level, Plugged Status, Lux (Luz), Temperature, etc.
    - *Light:* Screen Brightness/State.
- **Remote Control (REST API):** Endpoints locales para control total (ej: `POST /api/screen/on`, `POST /api/speak`).

### D. Sensors & Computer Vision
- **CameraX:** Migrar detección de movimiento a CameraX Analysis.
- **Face Detection:** Implementar Google ML Kit Face Detection para distinguir "Persona" de "Movimiento genérico".

## 4. Coding Rules for Agents
1. **Modernize on Touch:** Si tocas un archivo Java antiguo, pásalo a Kotlin.
2. **Permissions:** Gestionar permisos de Runtime (Cámara/Overlay) para Android 10+.
3. **Performance:** El procesamiento de imagen (Motion/Face) debe ser eficiente para no drenar batería si la tablet no está cargando.
