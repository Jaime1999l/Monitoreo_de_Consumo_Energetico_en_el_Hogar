LINK: https://github.com/Jaime1999l/Monitoreo_de_Consumo_Energetico_en_el_Hogar.git

# PARTICIPANTES

Jaime López Díaz
Alberto
Mario
Nicolas
Juan Manuel 


# Monitoreo de Consumo Energético en el Hogar

Este proyecto de Android está diseñado para monitorear el consumo energético de diferentes habitaciones y dispositivos dentro de un hogar. La aplicación utiliza Firebase Firestore para almacenar y recuperar datos, y se enfoca en la eficiencia energética mediante la recolección de datos en segundo plano, utilizando hilos para minimizar el impacto en el rendimiento y consumo de batería del dispositivo.

## Tabla de Contenidos

- [PantallaPrincipal](#pantallaprincipal)
- [DisenarHogarActivity](#disenarhogaractivity)
- [RoomMonitorActivity](#roommonitoractivity)
- [EnergyMonitorActivity](#energymonitoractivity)
- [GaussChartView](#gausschartview)
- [Clases del Dominio](#clases-del-dominio)
- [Worker](#worker)

---

## PantallaPrincipal

La clase `PantallaPrincipal` es la actividad principal que actúa como un menú central para navegar entre diferentes actividades de monitoreo y diseño del hogar.

### Funcionalidades:

- **Navegación**: Permite a los usuarios redirigirse a otras actividades:
  - `DisenarHogarActivity`: Para diseñar el layout del hogar.
  - `RoomMonitorActivity`: Para monitorear el consumo energético de las habitaciones.
  - `EnergyMonitorActivity`: Para monitorear el consumo de dispositivos.

- **Actualización en segundo plano**:
  - **Concurrencia**: Implementa un `ExecutorService` con dos hilos que se ejecutan en segundo plano:
    - Uno para monitorear el consumo de las habitaciones.
    - Otro para monitorear el uso energético de los dispositivos.
  - **Control de hilos**: Usa variables volátiles (`isUpdatingConsumo` e `isUpdatingEnergyUsage`) para manejar el control de la ejecución de los hilos de manera eficiente y segura.

### Código de interés:

executorService = Executors.newFixedThreadPool(2);
runBackgroundTasksConsumo();
runBackgroundTasksEnergy();

Estos métodos permiten la ejecución de tareas en segundo plano para la actualización periódica de los datos sin afectar el rendimiento de la interfaz de usuario.

## DisenarHogarActivity
La clase DisenarHogarActivity permite a los usuarios diseñar el layout de su hogar agregando habitaciones y pasillos y monitorear el consumo de cada habitación.

Funcionalidades:
Agregar habitaciones: Los usuarios pueden agregar nuevas habitaciones, definir su tamaño y color.
Agregar pasillos: Permite la creación de pasillos para conectar las habitaciones.
Firebase: Cada habitación o pasillo añadido se guarda automáticamente en Firebase Firestore.
Interacción táctil: Las habitaciones y los pasillos pueden moverse y redimensionarse mediante gestos táctiles.
Visualización del consumo por habitación.

## RoomMonitorActivity
Esta clase se utiliza para monitorear con una gráfica el consumo energético en tiempo real de las habitaciones del hogar.

Funcionalidades:
Consumo en tiempo real: Utiliza Firebase para obtener los datos de consumo energético de cada habitación y los actualiza periódicamente.
Gráficos: Usa GaussChartView para visualizar el consumo en gráficos.

### Código de interés:

handler.postDelayed(this::obtenerDatosDeHistorial, 2000);

Este fragmento ilustra cómo se actualizan periódicamente los datos de consumo utilizando un Handler con un retraso de 2 segundos.

## EnergyMonitorActivity
Esta actividad monitorea el consumo de energía de dispositivos específicos como luces, electrodomésticos y calefacción.

Funcionalidades:
Actualización periódica: Utiliza un Handler para actualizar el consumo de energía de los dispositivos cada 6 segundos.
Firebase: Los datos se envían a Firebase después de cada actualización.

## GaussChartView
GaussChartView es una clase personalizada que extiende View y se encarga de mostrar gráficos del consumo energético en las actividades de monitoreo.

Funcionalidades:
Dibujo del gráfico: Dibuja un gráfico de línea que representa el consumo energético a lo largo del tiempo.
Actualización dinámica: El gráfico se redibuja dinámicamente cuando se obtienen nuevos datos de consumo.

## Clases del Dominio
Habitacion
Pasillo

## Worker
EnergyDataWorker es una clase que extiende Worker y se utiliza para gestionar el envío de datos de consumo energético a Firebase en segundo plano.

Funcionalidades:
Sincronización con Firebase: Se encarga de enviar periódicamente los datos de uso energético (luz, electrodomésticos, calefacción) a Firebase sin interferir con la interfaz de usuario.
