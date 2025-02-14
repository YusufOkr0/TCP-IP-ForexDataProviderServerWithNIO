# FX Data Server - README & API Documentation

## Overview & Key Features
The FX Data Server is a Java-based NIO (Non-blocking I/O) server that provides real-time currency pair data. It is designed for high performance and scalability, enabling multiple clients to connect, authenticate, and subscribe to currency updates efficiently.

### Key Features:
- **Concurrent Connections:** Non-blocking I/O supports multiple clients simultaneously.
- **Real-Time Updates:** Delivers live bid/ask values to subscribed clients.
- **Secure Authentication:** Validates users with a credentials repository.
- **Flexible Subscriptions:** Supports subscribing and unsubscribing to currency pairs.
- **Robust Error Handling:** Provides clear feedback for invalid commands.

---

## Project Structure
```
TCP-IP-ForexDataProviderServerWithNIO
└── src/main/java/com/toyota
    ├── broadcast
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

### 2. Build and Start the Server:
- Modify src/main/resources/application.properties to adjust settings:
```bash
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
- Commands should end with a newline character (`\n`).
- The server auto-disconnects inactive clients.
- Supports multiple simultaneous client connections.

---

