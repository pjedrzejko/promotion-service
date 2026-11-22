# promotion-service

A small Spring Boot + JPA service for retail promotions (e.g. "20% off tools", "$10 off a paint gallon").
Built as a learning project: connect a real data source and write/read it end to end with JPA/Hibernate.

## Stack

- Java 21, Spring Boot 4.2, Spring Data JPA (Hibernate 7)
- H2 in-memory database (default) or PostgreSQL 17 via Docker Compose
- Bean Validation (Jakarta), Lombok
- Tests: JUnit 5, `@DataJpaTest`, `@WebMvcTest` + Mockito, Testcontainers

## How it fits together

```
HTTP request -> PromotionController -> PromotionRepository (Spring Data JPA) -> Hibernate -> H2 / Postgres
```

- **Promotion** (`@Entity`): name, discount type (`PERCENT` / `AMOUNT`), discount value, start/end date, active flag. Validation annotations reject bad input before it reaches the DB.
- **PromotionRepository**: extends `JpaRepository`, so CRUD is free. One derived query finds promotions active on a given date:
  `findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(...)`. Spring generates the SQL from the method name.
- **PromotionController**: REST endpoints under `/promotions`.
- **SeedData**: inserts 3 sample promotions on startup if the table is empty.

## Running it

### Option 1: H2 (no setup)

```bash
./mvnw spring-boot:run
```

Data lives in memory and resets on every restart (`ddl-auto=create-drop`).
H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:promotions`, user `sa`, no password).

### Option 2: PostgreSQL (data survives restarts)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Postgres runs in Docker on host port **5433**. The `postgres` profile uses `ddl-auto=update`, so Hibernate creates the table once and keeps the data.

## API

| Method | Path                  | Description                           |
|--------|-----------------------|---------------------------------------|
| GET    | `/promotions`         | List all promotions                   |
| GET    | `/promotions/{id}`    | Get one (404 if missing)              |
| GET    | `/promotions/active`  | Promotions active today               |
| POST   | `/promotions`         | Create (201; 400 if validation fails) |
| DELETE | `/promotions/{id}`    | Delete (204; 404 if missing)          |

### Examples

```bash
curl localhost:8080/promotions
curl localhost:8080/promotions/active

curl -X POST localhost:8080/promotions \
  -H "Content-Type: application/json" \
  -d '{"name":"Holiday Lights 25% Off","discountType":"PERCENT","discountValue":25,"startDate":"2026-12-01","endDate":"2026-12-24","active":true}'

curl -X DELETE localhost:8080/promotions/4
```

## Tests

```bash
./mvnw test
```

- `PromotionRepositoryTest`: `@DataJpaTest` against H2; saves and reads back, and checks the "active today" query against edge cases (starts today, ends today, inactive, expired, future).
- `PromotionControllerTest`: `@WebMvcTest` with a mocked repository; checks 200, 404, and that invalid input returns 400 without touching the DB.
- `PromotionServiceApplicationTests`: boots the full app against a real Postgres started by Testcontainers (Docker must be running).

## Roadmap

- [ ] JWT auth: public reads, admin-only writes
- [ ] Dockerfile + run app and DB together with Compose
- [ ] Deploy to local Kubernetes (Rancher Desktop)
