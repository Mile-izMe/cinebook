# 🎬 CineBook

> A backend system for movie ticket booking, built with Java and Spring Boot, focusing on transactional consistency,
> concurrent seat reservation, distributed locking, payment processing, and scalable backend architecture.

---

## 📖 Project Overview

**CineBook** is a movie ticket booking platform designed to simulate the backend architecture of a real-world cinema
reservation system.

The system allows users to:

- Browse movies and movie details
- Explore cinemas, rooms, and showtimes
- Select and temporarily reserve seats
- Create bookings
- Process payments
- Generate electronic tickets with barcodes
- Receive real-time seat availability updates
- View booking history

The main purpose of CineBook is not simply to implement CRUD operations, but to explore and demonstrate backend
engineering concepts commonly found in high-concurrency transactional systems.

The project focuses particularly on:

- **Distributed seat locking**
- **Race condition prevention**
- **Transactional consistency**
- **Reservation timeout**
- **Idempotent payment processing**
- **Real-time communication**
- **Payment state management**
- **Event-driven architecture**
- **Scalable backend design**

---

## ✨ Features

### 🔐 Authentication & Authorization

- User registration
- Email verification
- Login / Logout
- Access Token & Refresh Token
- Refresh Token Rotation
- Role-based authorization

### 🎬 Movie Management

- Browse movies
- Movie details
- Genres
- Movie reviews
- Movie search
- Movie poster management
- Trailer information

### 🏢 Cinema Management

- City management
- Cinema management
- Room management
- Seat management
- Showtime management
- Multiple viewing formats

### 💺 Seat Reservation

- Interactive seat map
- Temporary seat reservation
- Redis-based distributed locking
- Configurable reservation TTL
- Automatic seat release
- Multi-user concurrency handling
- Race condition prevention

### 📋 Booking

- Create booking
- Booking validation
- Booking status management
- Booking cancellation
- Booking history
- Cursor-based pagination
- Price calculation
- Booking snapshots

### 💳 Payment

- Payment creation
- Payment state management
- Mock payment gateway
- Payment callback handling
- Signature verification
- Amount verification
- Payment idempotency
- Payment retry handling

### 🎟️ Ticket

- Generate electronic ticket
- Generate barcode
- Associate ticket with completed booking

### 🔔 Real-time Notifications

- WebSocket communication
- Real-time seat lock notifications
- Real-time seat release notifications
- Booking/payment notifications

### ⚠️ Error Handling

Centralized error handling with structured error responses.

Example:

```json
{
  "timestamp": "2026-07-18T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "SEAT_ALREADY_RESERVED",
  "message": "Seat F5 is already reserved by another user",
  "path": "/api/bookings",
  "traceId": "a1b2c3d4"
}
```

Validation errors support multiple field-level errors:

Example:

```json
{
  "timestamp": "2026-07-18T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "email",
      "message": "must be a valid email"
    }
  ]
}
```

### 🛠️ Tech Stack

- Backend
- Database & Storage
- Messaging & Real-time
- Infrastructure
- Testing & API

### 🏗️ Architecture

- CineBook follows a package-by-feature architecture.
- Instead of organizing the entire application by technical layer, the project groups code around business features:

```
src/
└── main/
    └── java/
        └── com/
            └── cinebook/
                ├── common/
                    ├── config/
                    ├── security/
                    ├── ...
                ├── module/
                    ├── auth/
                    ├── movie/
                    ├── genre/
                    ├── review/
                    ├── city/
                    ├── cinema/
                    ├── room/
                    ├── seat/
                    ├── showtime/
                    ├── booking/
                    ├── payment/
                    ├── seatLock/
                    ├── .../
```

- A typical feature contains its own application components:

```
booking/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
├── exception/
└── validator/
```

- This structure keeps business logic cohesive and makes individual modules easier to maintain and evolve.

### 🔄 Core Booking Flow

- The core booking flow is designed around temporary seat reservation.

```
User
 │
 │ Select Seats
 ▼
Seat Lock Service
 │
 │ Redis Distributed Lock
 ▼
Seats Temporarily Reserved
 │
 ▼
Create Booking
 │
 │ Booking = PENDING
 ▼
Create Payment
 │
 │ Payment = PENDING
 ▼
Payment Gateway
 │
 │ Callback
 ▼
Payment Verification
 │
 ├── Verify Signature
 ├── Verify Amount
 └── Verify Transaction
 │
 ▼
Database Transaction
 │
 ├── Payment → SUCCESS
 └── Booking → PAID
 │
 ▼
Generate Ticket
 │
 ▼
Generate Barcode
 │
 ▼
Release Seat Lock
```

### 🔒 Distributed Seat Locking

- Seat reservation is handled using Redis + Lua Script to prevent multiple users from reserving the same seat
  concurrently.

```
User A ───────────────┐
                      │
                      ▼
                 Redis SETNX
                      │
                      ▼
                Seat A1 LOCKED
                      │
                      │
User B ───────────────┘
                      │
                      ▼
                 SETNX fails
                      │
                      ▼
              409 CONFLICT
```

- Each seat lock has a TTL to prevent abandoned reservations from remaining indefinitely.
- This allows CineBook to handle:
  Concurrent seat selection
  Race conditions
  Temporary reservations
  Automatic seat release
  Distributed application instances

### 🔁 Payment Flow

- Payment processing is separated from booking creation.

```
Booking
   │
   │ PENDING
   ▼
Payment
   │
   │ PENDING
   ▼
Mock Payment Gateway
   │
   │ Callback
   ▼
Payment Verification
   │
   ├── Signature
   ├── Amount
   └── Transaction
   │
   ▼
Payment SUCCESS
   │
   ▼
Booking PAID
   │
   ▼
Ticket Generated
```

- Payment callbacks are designed to be idempotent, allowing the system to safely handle duplicate callbacks.
  For example:

```
Callback #1
    ↓
Payment SUCCESS
    ↓
Booking PAID
    ↓
Ticket CREATED

Callback #2
    ↓
Already processed
    ↓
No duplicate ticket
```

## 🚀 Getting Started

### Prerequisites

- Make sure the following are installed:
  Java 21+
  Maven
  Docker
  Docker Compose
  Git

1. Clone the repository

```bash
git clone https://github.com/<your-username>/cinebook.git
cd cinebook
```

2. Configure environment variables

- Create your environment configuration based on the project's environment template.

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cinebook
DB_USERNAME=postgres
DB_PASSWORD=postgres

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your-secret-key

MAIL_HOST=localhost
MAIL_PORT=1025
```

3. Start infrastructure with Docker Compose

- docker compose up -d

4. Run application

### 📚 API Documentation

- http://localhost:8080/swagger-ui/index.html

## 📈 Engineering Focus

- CineBook is intentionally designed around several backend engineering challenges:

- Concurrency:
  Multiple users may attempt to reserve the same seat simultaneously.

- Distributed Locking:
  Redis provides shared locking state across application instances.

- Transactional Consistency:
  Booking and payment state transitions must remain consistent.

- Idempotency:
  Payment gateways may retry callbacks, so repeated requests must not create duplicate side effects.

- Reservation Timeout:
  Seats must automatically become available when a reservation expires.

- Real-time Synchronization:
  WebSocket events allow users to see seat availability changes without manually refreshing the page.

- Extensibility:
  The architecture is designed so that external payment providers and asynchronous processing can be introduced without
  coupling core business logic to a specific provider.