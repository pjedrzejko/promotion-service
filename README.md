# promotion-service

A Java 21 / Spring Boot project to practice connecting to a database with JPA and reading/writing to it through API calls. Next I'm adding JWT authentication and running it in Docker and local Kubernetes (Rancher Desktop).

## What it does

This service stores promotions (name, % or $ off, dates), and lets you list/create/delete them and see which are active today.

Built with Java 21, Spring Boot, Spring Data JPA, H2 and Postgres.

## Run it

H2 (in-memory, resets every restart):

    ./mvnw spring-boot:run

Postgres (data sticks around):

    docker compose up -d
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres

Try it:

    curl localhost:8080/promotions
    curl localhost:8080/promotions/active

## How it works

- `Promotion.java` describes what a promotion is (name, % or $ off, dates)
- `PromotionRepository.java` talks to the database; Spring writes the SQL from method names like `findByActiveTrue...`
- `PromotionController.java` handles the HTTP requests (GET/POST/DELETE)

## Things that broke (and what I learned)

I had JDK 24 installed, which isn't a long-term support version, so I installed JDK 21. `java --version` still showed 24 because Homebrew doesn't add 21 to your PATH automatically. Adding it to PATH and JAVA_HOME in ~/.zshrc fixed it.

There was a compile error due to capitalization as promotion.java was the file name vs public class Promotion. Renaming this file didn't work initially as the Mac filesystem ignores case, but a two-step rename using a temporary name worked.

The app connected to the wrong Postgres on 5432 due to a previous project still using this port. I fixed it by mapping my Docker Postgres to port 5433 instead.

## Next

JWT auth (writes only), then Docker and Kubernetes.
