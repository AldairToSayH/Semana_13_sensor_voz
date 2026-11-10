# Sensor de luz y control por voz

Aplicación Android desarrollada con Kotlin y Jetpack Compose.

## Funcionalidades

- Solicita permiso para utilizar el micrófono.
- Reconoce órdenes de voz en español y cambia el color de fondo.
- Lee en tiempo real el sensor de luz del dispositivo.
- Muestra una imagen de acuerdo con la iluminación:
  - Menos de 50 lux: anochecer.
  - Entre 50 y 499 lux: atardecer.
  - Desde 500 lux: amanecer.

## Ejecución

Abre el proyecto en Android Studio y ejecútalo en un dispositivo físico. El sensor de luz puede no estar disponible en algunos emuladores.
