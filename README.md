# FX Data Server - README & API Documentation

## Overview & Key Features
The FX Data Server is a Java-based NIO (Non-blocking I/O) server that provides real-time currency pair data. It is designed for high performance and scalability, enabling multiple clients to connect, authenticate, and subscribe to currency updates efficiently.

### Key Features:
- **Concurrent Connections:** Supports multiple clients with non-blocking I/O.
- **Live Broadcasts:** Sends continuous real-time bid/ask updates to subscribers.
- **Secure Authentication:** Validates users using configured credentials.
- **Customizable:** Settings are easily configurable via `application.properties`.
- **Flexible Subscriptions:** Clients can subscribe or unsubscribe from currency pairs.

---

## Project Structure
```
TCP-IP-ForexDataProviderServerWithNIO
└── src/main/java/com/Forex
    ├── broadcast
    │   └── FXDataPublisher.java
    ├── config
    ├── entity
    └── server
        └── FXDataServer.java
    └── Main.java
└── resources
└── pom.xml
```

---

## How to Run the Server
### 1. Clone the Repository:
```bash
git clone https://github.com/YusufOkr0/TCP-IP-ForexDataProviderServerWithNIO.git
cd TCP-IP-ForexDataProviderServerWithNIO
```
### 2. Configure Server Properties:
Modify `src/main/resources/application.properties`:
```properties
server.port=8081
currency.pairs=TCP_USDTRY,TCP_EURUSD,TCP_GBPUSD
TCP_USDTRY.bid=33.90
TCP_USDTRY.ask=34.60
TCP_EURUSD.bid=1.0
TCP_EURUSD.ask=1.2
TCP_GBPUSD.bid=2.0
TCP_GBPUSD.ask=2.4
publish.frequency=1000

user.credentials=user|pass,admin|admin
```
### 3. Build and Start the Server:
```bash
mvn clean package
java -jar target/FXDataServer.jar
```
---

## How to Interact via Telnet
### 1. Connect:
```bash
telnet localhost 8081
```
### 2. Authenticate:
```bash
connect|admin|admin
```
**Response:** `SUCCESS|Connected as: admin`

### 3. Subscribe:
```bash
subscribe|TCP_USDTRY
```
**Response:** `SUCCESS|Subscribed to currency pair: TCP_USDTRY`

### Live Broadcast Example from `FXDataPublisher`:
The server will broadcast updated rates based on real-time fluctuations:
```
TCP_USDTRY|33.9578638183782944|34.6578638183782944|2025-02-14 16:10:18.8470144
TCP_USDTRY|33.9898486584141338|34.6898486584141338|2025-02-14 16:10:19.8626141
TCP_USDTRY|33.9667324048896522|34.6667324048896522|2025-02-14 16:10:20.8640348
```
---

### 4. Unsubscribe:
```bash
unsubscribe|TCP_USDTRY
```
**Response:** `SUCCESS|Unsubscribed from currency pair: TCP_USDTRY`

### 5. Disconnect:
```bash
disconnect|admin|admin
```
**Response:** `SUCCESS|Disconnected`

---

## Error Responses
- `ERROR|You are not authenticated`
- `ERROR|Invalid message format`
- `ERROR|Invalid currency pair`

---

## Notes
- All commands should end with a newline character (`\n`).
- The server automatically disconnects inactive clients.
- Multiple clients can connect and subscribe simultaneously.

---

