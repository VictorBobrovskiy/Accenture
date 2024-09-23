### Documentación Completa del Servicio de Procesamiento de Pedidos

Este servicio está diseñado para manejar el procesamiento de pedidos de manera eficiente y escalable. Utiliza **Spring Boot**, **Kafka** para la mensajería asíncrona, **Redis** para almacenamiento en caché, y **PostgreSQL** como base de datos para almacenar los pedidos. El servicio procesa los pedidos de manera asíncrona y los resultados se almacenan en Redis para una recuperación rápida.

---

## Índice
1. [Descripción General](#descripción-general)
2. [Arquitectura](#arquitectura)
3. [Componentes Principales](#componentes-principales)
   1. [OrderService](#orderservice)
   2. [OrderController](#ordercontroller)
   3. [OrderProcessingService](#orderprocessingservice)
   4. [KafkaOrderProducerService](#kafkaorderproducerservice)
   5. [KafkaOrderConsumerService](#kafkaorderconsumerservice)
4. [Endpoints](#endpoints)
   1. [Procesar Pedido](#procesar-pedido)
   2. [Obtener Pedido por ID](#obtener-pedido-por-id)
   3. [Obtener Todos los Pedidos](#obtener-todos-los-pedidos)
5. [Manejo de Errores](#manejo-de-errores)
6. [Manejo de Logs](#manejo-de-logs)
7. [Configuración](#configuración)
   1. [Kafka](#kafka)
   2. [Redis](#redis)
   3. [PostgreSQL](#postgresql)
   4. [Configuración de Spring Boot](#configuración-de-spring-boot)
8. [Consideraciones de Concurrencia](#consideraciones-de-concurrencia)
9. [Conclusión](#conclusión)

---

## Descripción General

Este servicio está diseñado para gestionar el ciclo de vida de un pedido, desde su recepción, validación, procesamiento, hasta el almacenamiento en caché y la persistencia en la base de datos. 
Utiliza una arquitectura basada en microservicios con **Kafka** para la mensajería y **Redis** como sistema de caché para mejorar el rendimiento en la recuperación de datos.

### Características Principales:
- Procesamiento de pedidos de forma asíncrona.
- Uso de Kafka para la comunicación entre servicios.
- Almacenamiento temporal de pedidos procesados en Redis.
- Persistencia de pedidos en una base de datos PostgreSQL.
- Manejo de concurrencia con un **thread pool** personalizado.

---

## Arquitectura

### Diagrama Simplificado:

```
+------------------+      +---------------------+      +----------------------+
|  Cliente (API)    | ---> |  OrderController    | ---> |  OrderServiceImpl     |
+------------------+      +---------------------+      +----------^-----------+
                                                           |       |            
                                                           |       |            
                                                           |       v            
                                           +---------------------+             
                                           |  KafkaOrderProducer  |             
                                           +---------------------+             
                                                           |                   
                                                           v                   
                                           +---------------------+             
                                           |  KafkaOrderConsumer  |             
                                           +---------------------+             
                                                           |                   
                                                           v                   
                                           +---------------------+             
                                           |  OrderProcessingService |             
                                           +---------------------+             
```

---

## Componentes Principales

### 1. OrderService

El `OrderService` es la interfaz que define los métodos principales para manejar las operaciones de los pedidos, como:
- **Procesar Pedido**: Procesa el pedido, lo guarda en la base de datos y lo envía a Kafka para su procesamiento.
- **Obtener Pedido por ID**: Recupera un pedido de la base de datos (o de Redis si está en caché).
- **Obtener Todos los Pedidos**: Devuelve una lista de todos los pedidos almacenados.

#### Métodos Clave:
- `CompletableFuture<Order> processOrder(Order order)`
- `Order getOrderById(long orderId)`
- `List<Order> getAllOrders()`

### 2. OrderController

El `OrderController` expone los **endpoints REST** para interactuar con el servicio de pedidos. Los clientes pueden enviar pedidos para su procesamiento, así como recuperar pedidos específicos o todos los pedidos almacenados.

#### Métodos Clave:
- **`processOrder()`**: Procesa un pedido de manera asíncrona.
- **`getOrder()`**: Recupera un pedido por su ID.
- **`getAllOrders()`**: Recupera todos los pedidos.

### 3. OrderProcessingService

Este servicio simula el procesamiento de un pedido. Valida el pedido, recalcula el precio aplicando descuentos aleatorios, y luego marca el pedido como "COMPLETED" o "FAILED" dependiendo del resultado del procesamiento.

#### Métodos Clave:
- **`processOrder(Order order)`**: Procesa el pedido validando y recalculando el precio, y luego lo envía a Kafka para su almacenamiento.

### 4. KafkaOrderProducerService

Este servicio se encarga de enviar los pedidos a Kafka para su procesamiento asíncrono. Publica los pedidos en un **topic** de Kafka para que sean consumidos por el `KafkaOrderConsumerService`.

### 5. KafkaOrderConsumerService

Este servicio consume los mensajes de Kafka y pasa los pedidos al `OrderProcessingService` para su procesamiento.

---

## Endpoints

### 1. Procesar Pedido

#### Descripción:
Este endpoint procesa un nuevo pedido de manera **asíncrona**. El pedido se guarda en la base de datos, se envía a Kafka para procesamiento, y se almacena en Redis para futuras consultas rápidas.

#### Detalles del Endpoint:
- **URL**: `/api/orders/processOrder`
- **Método HTTP**: `POST`
- **Request Body**: `OrderDto` (JSON)

```json
{
  "customerId": 12345,
  "orderAmount": 150.75,
  "orderItems": [
    {
      "productId": "prod-001",
      "quantity": 2,
      "price": 50.25
    },
    {
      "productId": "prod-002",
      "quantity": 1,
      "price": 50.25
    }
  ],
  "status": "RECEIVED"
}
```

- **Response Body**: `OrderDto` (JSON)

#### Respuestas:
- **200 (OK)**: Pedido procesado exitosamente.
- **400 (Bad Request)**: La solicitud contiene datos inválidos.
- **500 (Internal Server Error)**: Error interno del servidor al procesar el pedido.

### 2. Obtener Pedido por ID

#### Descripción:
Este endpoint permite recuperar un pedido específico por su ID. Si el pedido ya fue procesado, se recuperará desde Redis para mejorar el tiempo de respuesta.

#### Detalles del Endpoint:
- **URL**: `/api/orders/{orderId}`
- **Método HTTP**: `GET`
- **Path Parameter**: `orderId` (Long)

- **Response Body**: `OrderDto` (JSON)

#### Respuestas:
- **200 (OK)**: Pedido recuperado exitosamente.
- **404 (Not Found)**: El pedido con el ID especificado no existe.
- **500 (Internal Server Error)**: Error interno del servidor.

### 3. Obtener Todos los Pedidos

#### Descripción:
Este endpoint permite recuperar una lista completa de todos los pedidos almacenados en la base de datos.

#### Detalles del Endpoint:
- **URL**: `/api/orders`
- **Método HTTP**: `GET`

- **Response Body**: `List<OrderDto>`

#### Respuestas:
- **200 (OK)**: Lista de pedidos recuperada exitosamente.
- **204 (No Content)**: No hay pedidos disponibles.
- **500 (Internal Server Error)**: Error interno del servidor.

---

## Manejo de Errores

El servicio maneja los errores comunes con respuestas adecuadas y mensajes descriptivos. Estas excepciones se manejan en `ErrorHandler` y se devuelven con códigos de estado HTTP apropiados.

### Excepciones Principales:
- **OrderNotFoundException**: Se lanza cuando un pedido no puede ser encontrado.
- **ValidateException**: Se utiliza para errores de validación durante el procesamiento del pedido.
- **General Exceptions**: Para otras excepciones no previstas, se devuelve un error 500 (Internal Server Error).

---

## Manejo de Logs

El servicio utiliza **Lombok** para generar logs automáticamente utilizando la anotación **`@Slf4j`**. Se generan logs en varios niveles (`DEBUG`, `INFO`, `ERROR`) para hacer un seguimiento de las operaciones y errores.

Ejemplo de log al procesar un pedido:

```plaintext
2024-09-23 13:00:00 DEBUG Recibido el pedido para procesar: {orderDto}
2024-09-23 13:00:02 INFO Pedido procesado exitosamente: {orderId}
2024-09-23 13:00:05 DEBUG Orden completada guardada en Redis con ID: {orderId}
```

---

## Configuración

### 1. Kafka

Kafka es utilizado para la comunicación entre el servicio de procesamiento de pedidos y el servicio de procesamiento de Kafka. La configuración de Kafka se realiza en el archivo `application.yml`.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: order-group
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### 2. Redis

Redis se utiliza para almacenar en caché los pedidos procesados, lo que permite una recuperación rápida de los datos. La configuración de Redis se realiza de la siguiente manera:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
  cache:
    type: redis
```

### 3. PostgreSQL

PostgreSQL se utiliza como base de datos para almacenar los pedidos y sus elementos. La configuración se realiza en el archivo `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: postgres
    password: 321
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 4. Configuración de Spring Boot

La configuración general de **Spring Boot** se realiza en el archivo `application.yml`, incluyendo la configuración de puertos, caché y datos de conexión a la base de datos.

---

## Consideraciones de Concurrencia

El servicio utiliza un **pool de hilos personalizado** (`ExecutorService`) para manejar solicitudes concurrentes y mejorar el rendimiento. Esto permite que múltiples pedidos se procesen de manera asíncrona sin bloquear el hilo principal.

```java
private final ExecutorService executorService = Executors.newFixedThreadPool(20);
```

El uso de **`CompletableFuture.supplyAsync()`** asegura que las operaciones de procesamiento de pedidos no bloqueen el hilo principal, mejorando la escalabilidad del servicio.

---

## Conclusión

Este servicio de procesamiento de pedidos está diseñado para ser escalable, eficiente y fácil de mantener. Utiliza Spring Boot, Kafka para mensajería asíncrona, Redis para almacenamiento en caché, y PostgreSQL para persistencia. 
El servicio está preparado para manejar múltiples solicitudes concurrentes de manera eficiente, asegurando que los pedidos se procesen y almacenen de forma óptima.
