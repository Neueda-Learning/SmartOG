# Payments Frontend (React + Vite)

A separate frontend app for the Spring Boot payments API.

## Prerequisites

- Node.js 18+
- Backend running on `http://localhost:8080`

## Run

```powershell
npm install
npm run dev
```

Open `http://localhost:5173`.

## Build

```powershell
npm run build
npm run preview
```

## Features in this minimal UI

- Create a payment
- Query payment by id
- List payments with status filter
- Trigger `validate`, `send`, `complete` transitions
- Trigger `fail` transition with custom `errorCode` and `errorMessage`
- View payment status history

## If Create looks unresponsive

- Confirm backend is running on `http://localhost:8080`
- Use 8-20 digit account numbers (for both source and destination)
- Source and destination cannot be the same
- Check the message at the bottom of the page for API/validation errors


