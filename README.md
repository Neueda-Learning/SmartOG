
# Payment Processing Platform
**Team**: SmartOG

**Member**: Gyro Wang, Olivia Wei, Simon Sun

## Project Overview

SmartOG Payment Processing Platform is a customer-facing digital banking payment system designed to simulate the lifecycle of financial transactions.

The platform enables customers to create payments, validate transactions, process multi-currency payments, track payment status, and receive support guidance when payment issues occur.

The system focuses on building a reliable payment workflow through:

- payment lifecycle management
- automated validation
- currency conversion
- account balance verification
- transaction history tracking
- structured error handling

***
# Key Features

## 1. Payment Lifecycle Management

The system manages the complete payment processing lifecycle:

```text
Create Payment

      ↓

Validate Payment

      ↓

Send Payment

      ↓

Complete / Fail
```

Each stage updates the payment status and records status changes to maintain transaction transparency and auditability.

---

## 2. Payment Validation

Before processing a payment, the system performs multiple validation steps.

### Currency Validation

The system validates whether the selected payment currency is supported.

Features:

- Supported currencies are managed centrally.
- Frontend currency selection is validated by backend rules.
- Invalid currency requests are rejected.


### Exchange Rate Conversion

The system supports multi-currency payments.

Customer accounts use USD as the default account currency. When a customer submits a payment in another currency, the system converts the requested amount into USD before performing balance verification.

Workflow:

```text
Customer Input

Target Currency
+
Target Amount

        ↓

Exchange Rate Service

        ↓

Convert to Target Currency

        ↓

Balance Verification
```


### Account Balance Verification

The system verifies whether the customer has sufficient funds before processing a payment.

Example:

```text
Available Account Balance (USD)

            >=

Converted Payment Amount (USD)
```

If the balance is insufficient, the payment is rejected with alerting `FAIL` in the record.


## 3. Payment Processing & Bank Acknowledgement Simulation

After successful validation, the system processes the payment.

The processing workflow includes:

- updating payment status
- simulating account balance deduction
- recording payment processing information
- simulating communication with external banking systems

The system supports simulated bank acknowledgement responses to represent real-world asynchronous payment processing.


## 4. Transaction History & Audit Trail

The system maintains a complete history of payment status changes.

Payment history records provide:

- previous status
- new status
- timestamp
- processing information

This supports:

- transaction transparency
- troubleshooting
- audit requirements


## 5. Customer Dashboard

The customer dashboard provides visibility into payment activities.

Customers can view:

### Account Information

- account details
- available balance

### Payment Information

- payment records
- current payment status
- converted payment amount
- transaction history

### Support Information

For failed payments, customers receive:

- error information
- support guidance
- technical support contact options



## 6. Error Handling

The system provides structured error handling for different payment scenarios.

Supported error scenarios include:

- invalid payment input
- unsupported currency
- exchange rate unavailable
- insufficient balance
- invalid payment state transition
- bank processing failure
- payment acknowledgement timeout

Errors are returned with meaningful information to support both customers and developers.

---

# System Workflow

High-level payment workflow:

```text
Customer

   ↓

Create Payment

   ↓

Currency Validation

   ↓

Exchange Rate Conversion

   ↓

Balance Verification

   ↓

Payment Rules Validation

   ↓

Payment Processing

   ↓

Bank Acknowledgement

   ↓

Update Payment Status

   ↓

Save Payment History
```

---

# Tech Stack

## Backend

### Java 21 + Spring Boot

Spring Boot is used to build the backend service because it provides:

- REST API development
- Dependency Injection
- Layered application architecture
- Transaction management support

Java's object-oriented design fits well with financial domain modeling, including:

- Payment
- Payment Status
- Account
- Transaction History


### JDBC

JDBC is used for database interaction.

Using JDBC provides:

- direct control over SQL operations
- explicit database access logic
- better visibility into data persistence workflows

The repository layer uses JDBC-based implementations for:

- payment operations
- transaction history management
- account balance operations


### MySQL

MySQL is used as the relational database for storing payment-related data.

Reasons:

- reliable relational data management
- strong consistency for financial records
- structured storage for transactional information

The database manages core business data including:

- payment records
- payment status history
- account information
- supporting transaction data

### External API Integration

The system integrates with external services for:

- exchange rate retrieval
- simulated bank acknowledgement processing

These integrations simulate real-world banking communication workflows.

### Testing

JUnit 5 and integration testing are used to verify:

- payment creation validation
- currency validation
- exchange rate conversion
- balance verification
- payment state transition
- payment completion and failure scenarios



### Docker

Docker is used to provide a consistent development environment for application dependencies and database services.

---

## Frontend

(To be updated)

---

# Architecture

The application follows a layered backend architecture:

```text
Controller Layer

        ↓

Service Layer

        ↓

Repository Layer (JDBC)

        ↓

MySQL Database
```


## Controller Layer

Responsible for:

- receiving HTTP requests
- request validation
- response handling


## Service Layer

Contains the core business logic:

- payment workflow orchestration
- currency validation
- exchange rate conversion
- balance verification
- payment processing
- payment status management


## Repository Layer

Responsible for:

- database access
- SQL execution
- data persistence through JDBC

---

# Domain Model

Core domain objects:

```text
Payment

PaymentStatus

PaymentStatusHistory
```

Supporting components:

```text
Account Balance

Currency Management

Exchange Rate Service

Bank Gateway Simulation
```

---

# Why This Project

This project demonstrates backend engineering practices commonly used in financial systems:

- payment lifecycle management
- business rule validation
- external service integration
- database consistency
- audit trail design
- structured error handling

The project focuses on building a reliable and transparent payment processing workflow similar to modern digital banking platforms.

---

# Future Improvements

## Authentication & Authorization

Potential improvements:

- user authentication
- role-based access control
- multi-factor authentication


## Real Banking Integration

Future integration possibilities:

- real payment gateway
- external banking network


## Advanced Payment Monitoring

Potential enhancements:

- configurable payment rules
- real-time transaction monitoring


## Scalability Improvements

Future improvements:

- event-driven processing
- message queues
- distributed payment services

---

# Getting Started

## Prerequisites

- Java 21
- Maven
- MySQL
- Docker


## Run Application

```bash
git clone <repository-url>

cd payment-processing

docker-compose up

mvn spring-boot:run
```


git status
git add README.md
git commit -m "docs: update README with latest backend changes"
git push origin main
