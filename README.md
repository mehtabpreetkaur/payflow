# PayFlow — Payment Gateway Simulation API

PayFlow is a **Spring Boot-based payment gateway simulation API** that demonstrates core backend engineering concepts: merchant onboarding, API key authentication, payment processing, refund handling, and state-managed transaction lifecycles.

This project is designed as a realistic fintech-style backend system and uses a **state machine pattern** to enforce valid payment transitions.

---

## 🚀 Features

### **Merchant Management**

* Create merchant accounts
* Auto-generate merchant IDs and API keys
* Fetch merchant details and list all merchants

### **Payment Processing**

* Create a payment using merchant API key
* Masks card data: **only last 4 digits stored**
* Tracks lifecycle using Spring State Machine
* States include: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `REFUNDED`

### **Refunds**

* Full or partial refunds
* Only allowed when the payment is in a refundable state
* Enforced through domain rules and state machine transitions

### **Transaction History**

* Fetch payment by transaction ID
* List all payments for a merchant
* Filter payments by status

### **Error Handling**

* Centralized error mapping via `GlobalExceptionHandler`
* Domain-specific exceptions:

  * `MerchantNotFoundException`
  * `InvalidStateTransitionException`
  * `PaymentException`

---

## 🧱 Architecture Overview

### **Layered Structure**

```
Controller → Service → Repository → Entity
```

### **Components**

* **Controllers:** REST endpoints for merchants & payments
* **Services:** business logic, state transitions, validation
* **Repositories:** JPA interfaces for persistence
* **Entities:** Merchant & Payment mapped via JPA
* **State Machine:** defines payment states, events & transitions
* **DTOs:** clean request/response models with validation

---

## 🔑 API Authentication

All payment-related endpoints require:

```
X-API-Key: <merchant_api_key>
```

The key is validated via `MerchantService` before processing any request.

---

## 🗃️ Tech Stack

* **Java 17**
* **Spring Boot 3.x**
* **Spring Web**
* **Spring Data JPA**
* **Spring State Machine**
* **Lombok**
* **H2 Database (dev)**
* **PostgreSQL driver (prod-ready)**
* **Maven**

---

## 📦 Running the Application

### **1. Clone the repository**

```bash
git clone https://github.com/mehtabpreetkaur/payflow.git
cd payflow
```

### **2. Build and run**

```bash
mvn clean package
java -jar target/payflow-0.0.1-SNAPSHOT.jar
```

The app runs by default at:

```
http://localhost:8080
```

---

## 📝 API Endpoints

### **Merchant API** (`/api/v1/merchants`)

| Method | Endpoint | Description                    |
| ------ | -------- | ------------------------------ |
| POST   | `/`      | Create a merchant              |
| GET    | `/{id}`  | Get merchant by internal DB ID |
| GET    | `/`      | List all merchants             |

---

### **Payments API** (`/api/v1/payments`)

| Method | Endpoint                   | Description                           |
| ------ | -------------------------- | ------------------------------------- |
| POST   | `/`                        | Create payment (requires `X-API-Key`) |
| POST   | `/{transactionId}/process` | Process a payment                     |
| POST   | `/refund`                  | Refund a payment                      |
| GET    | `/{transactionId}`         | Get payment details                   |
| GET    | `/merchant/{merchantId}`   | List merchant payments                |
| GET    | `/state/{state}`           | List payments by status               |

---

## 📄 Sample Requests

### **Create Merchant**

```bash
curl -X POST http://localhost:8080/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{"name": "Acme Corp", "email": "admin@acme.com"}'
```

### **Create Payment**

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-API-Key: <merchant_api_key>" \
  -d '{
    "merchantId": "<merchantId>",
    "amount": 120.50,
    "currency": "USD",
    "cardNumber": "4242424242424242",
    "cardHolderName": "John Doe",
    "expiry": "12/27",
    "cvv": "123"
  }'
```

> Only the **last 4 digits** of the card number are stored.
> CVV is accepted but **never persisted**.

### **Process Payment**

```bash
curl -X POST http://localhost:8080/api/v1/payments/<transactionId>/process
```

### **Refund Payment**

```bash
curl -X POST http://localhost:8080/api/v1/payments/refund \
  -H "Content-Type: application/json" \
  -d '{"transactionId": "<transactionId>", "amount": 50.00}'
```

---

## 🧪 Validation & Error Handling

Common responses include:

* `400 Bad Request` — Invalid input
* `401 Unauthorized` — Missing or invalid API key
* `404 Not Found` — Merchant or payment not found
* `409 Conflict` — Invalid state transition

All handled through `GlobalExceptionHandler`.

---

## ⚠️ Security Notes

This project is **not PCI-compliant**.
It simulates payment processing. Do **NOT** use real cardholder data.

* CVV must never be logged or stored
* PAN is masked (only last 4 digits kept)
* API keys should be rotated and stored securely in real systems


