# Sistema de Monitoreo de Sueño - Galaxy Watch 5 y Android

Este proyecto contiene dos aplicaciones Android separadas que trabajan en conjunto para monitorear el sueño:

1. **Aplicación Móvil** (`movile-app/`) - Para teléfonos Android
2. **Aplicación para Galaxy Watch 5** (`watch-app/`) - Para el reloj inteligente

## Características Principales

### Aplicación Móvil
- **Interfaz moderna** con Material Design
- **Conexión Bluetooth** con el Galaxy Watch 5
- **Almacenamiento local** de datos de sueño usando Room Database
- **Visualización de datos** de ritmo cardíaco y pasos
- **Cálculo de calidad del sueño** basado en múltiples parámetros
- **Historial de sueño** con análisis de tendencias

### Aplicación para Galaxy Watch 5
- **Monitoreo de salud** usando Health Services API
- **Recolección de datos** de ritmo cardíaco y pasos
- **Transmisión Bluetooth** de datos al teléfono
- **Interfaz optimizada** para pantalla pequeña
- **Monitoreo en segundo plano**

## Funcionalidades

### Monitoreo de Salud
- **Ritmo cardíaco** en tiempo real
- **Conteo de pasos** durante el sueño
- **Detección de movimiento** para análisis de calidad del sueño

### Comunicación Bluetooth
- **Conexión automática** entre dispositivos
- **Transmisión de datos** en tiempo real
- **Reconexión automática** en caso de desconexión

### Análisis de Sueño
- **Calidad del sueño** calculada automáticamente
- **Duración del sueño** registrada
- **Tendencias** y patrones de sueño

## Requisitos del Sistema

### Para la Aplicación Móvil
- Android 7.0 (API 24) o superior
- Bluetooth habilitado
- Permisos de ubicación (para Bluetooth)

### Para la Aplicación del Galaxy Watch 5
- Wear OS 3.0 o superior
- Health Services API disponible
- Bluetooth habilitado

## Instalación

### Aplicación Móvil
1. Abrir el proyecto `movile-app` en Android Studio
2. Sincronizar dependencias de Gradle
3. Conectar dispositivo Android
4. Ejecutar la aplicación

### Aplicación del Galaxy Watch 5
1. Abrir el proyecto `watch-app` en Android Studio
2. Sincronizar dependencias de Gradle
3. Conectar Galaxy Watch 5
4. Ejecutar la aplicación

## Uso

### Configuración Inicial
1. **Instalar ambas aplicaciones** en sus respectivos dispositivos
2. **Conectar dispositivos** vía Bluetooth
3. **Conceder permisos** necesarios en ambas aplicaciones

### Monitoreo de Sueño
1. **Iniciar monitoreo** en el Galaxy Watch 5
2. **Los datos se transmiten** automáticamente al teléfono
3. **Ver resultados** en la aplicación móvil
4. **Analizar tendencias** de sueño

## Estructura del Proyecto

```
├── movile-app/                 # Aplicación para teléfono
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/example/sleepmonitor/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── viewmodel/
│   │   │   └── res/
│   │   └── build.gradle
│   └── build.gradle
│
└── watch-app/                  # Aplicación para Galaxy Watch 5
    ├── app/
    │   ├── src/main/
    │   │   ├── java/com/example/watchsleepmonitor/
    │   │   │   ├── MainActivity.kt
    │   │   │   ├── service/
    │   │   │   └── viewmodel/
    │   │   └── res/
    │   └── build.gradle
    └── build.gradle
```

## Tecnologías Utilizadas

### Aplicación Móvil
- **Kotlin** - Lenguaje de programación
- **AndroidX** - Bibliotecas de soporte
- **Room Database** - Almacenamiento local
- **LiveData & ViewModel** - Arquitectura MVVM
- **Coroutines** - Programación asíncrona
- **Bluetooth API** - Comunicación con el reloj

### Aplicación del Galaxy Watch 5
- **Kotlin** - Lenguaje de programación
- **Wear OS** - Plataforma para relojes
- **Health Services API** - Monitoreo de salud
- **Bluetooth API** - Comunicación con el teléfono
- **Coroutines** - Programación asíncrona

## Permisos Requeridos

### Aplicación Móvil
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### Aplicación del Galaxy Watch 5
```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.BODY_SENSORS_BACKGROUND" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
```

## Desarrollo

### Compilación
```bash
# Aplicación móvil
cd movile-app
./gradlew assembleDebug

# Aplicación del reloj
cd watch-app
./gradlew assembleDebug
```

### Pruebas
```bash
# Aplicación móvil
cd movile-app
./gradlew test

# Aplicación del reloj
cd watch-app
./gradlew test
```

## Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Contacto

Para preguntas o soporte, por favor contactar a través de los issues del proyecto. 