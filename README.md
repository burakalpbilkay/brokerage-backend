# Brokerage Service

Small Spring Boot service for a brokerage firm. Supports creating/listing/canceling orders, listing assets, and (admin-only) matching orders. Uses HTTP Basic auth, JPA, and H2 by default.

## Requirements
- Java 21  
- Maven 4.0

## Run

### Dev / Test (H2 in-memory)
```bash
./mvnw spring-boot:run
```
H2 console: `http://localhost:8080/h2-console` (username: `sa`, no password).

```
Then:
```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### Admin & Seed
- Admin user is in-memory (configure in `application.yml`):
  ```yaml
  app.security.admin.username: admin
  app.security.admin.password: admin123
  ```
- Non-prod runs a small data loader (users `user1`, `user2`, TRY balances, some INGA).

## API

All endpoints require Basic auth.

### Current Customer's Profile
`GET /api/customers/me` → current user’s profile.

### Assets
`GET /api/assets?customerId={uuid}[&assetName=...]`  
- Admin: any customer  
- Customer: only own `customerId`

### Orders
- **Create:** `POST /api/orders`
  ```json
  { "customerId":"...", "assetName":"INGA", "side":"BUY|SELL", "size":"1.0000", "price":"10.00" }
  ```
  Creates a `PENDING` order and reserves balances (TRY for BUY, shares for SELL).
- **List (paged):**  
  `GET /api/orders?customerId={uuid}[&from=...&to=...&status=...&assetName=...&page=0&size=20]`
- **Cancel:** `DELETE /api/orders/{orderId}`  
  Only if `PENDING`. Restores the reservation.

### Admin
- **Match order:** `POST /api/admin/orders/{id}/match`  
  Applies settlement and sets `MATCHED`.

### Error shape
Errors return a small JSON with `status`, `error`, `message`, `timestamp`.

## Money & Precision
- Prices (TRY): 2 decimals  
- Shares: 4 decimals  
- Rounding: HALF_UP  
- Centralized in `service.Money`

## Concurrency
- `Asset` and `Order` uses optimistic locking (`@Version`)  
- Order creation retries on lock failures to avoid over-withdraw

## Build & Test
```bash
./mvnw clean verify
```

## Project Layout
```
api/
  controller/   # AdminOrder, Asset, Customer, Order controllers
  dto/          # Request/response models
  Error/        # ApiError, GlobalExceptionHandler, custom exceptions
config/         # SecurityConfig, AuthContext, DevDataLoader
domain/         # JPA entities + enums
repository/     # Spring Data JPA repositories
service/        # OrderService, AdminMatchingService, Money
```
