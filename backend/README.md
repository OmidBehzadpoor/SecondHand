# Secondhand Marketplace — Backend

This module is the **backend** of the Alborz secondhand-market project: a RESTful API server built with **Spring Boot** that provides authentication, advertisement management, chat, favorites, seller ratings, and administrative operations for the application. The **frontend** (a JavaFX desktop client) consumes this API; see [`../frontend/README.md`](../frontend/README.md) for details on the client side.

## Technologies Used
- **Java 25**
- **Spring Boot** — Spring Web MVC, Spring Data JPA, Spring Security, Spring Validation
- **SQLite** as the embedded database, accessed through Hibernate (community SQLite dialect)
- **JWT (JJWT library)** for stateless authentication and authorization
- **Lombok** to reduce boilerplate code
- **springdoc-openapi (Swagger UI) + Scalar** for interactive API documentation
- **JUnit 5 & Mockito** for unit and integration testing

## Requirements
1. **JDK 25** — this project requires Java 25.
2. **Maven** (version 3.9 or higher) — for managing dependencies and building the project. The Maven Wrapper (`mvnw` / `mvnw.cmd`) is included, so a local Maven installation is not strictly necessary.
3. **Internet connection** (only once, not required every time you run the backend) — needed for Maven to download dependencies from Maven Central on the first build.

No separate database server needs to be installed; SQLite is embedded and its file is created automatically (see [Data storage](#data-storage) below).

## How to run
1. Through the terminal (using Maven):
   ```
   cd backend
   ```
   on Linux/Mac: `./mvnw spring-boot:run`
   on Windows: `mvnw.cmd spring-boot:run`
2. Through IntelliJ IDEA:
   Open `SecondhandApplication.java` and click the "Run" icon next to the `main()` method.

By default, the server starts on **`http://localhost:8080`**.

### Running the tests
```
./mvnw test
```
(or `mvnw.cmd test` on Windows)

## Configuration
The application reads its configuration from `src/main/resources/application.properties` and `application-sqlite.properties`. The following values can be overridden through environment variables without editing the source code:

| Environment variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | a placeholder secret (should be overridden in production) | Secret key used to sign JWT tokens; must be at least 32 characters long |
| `JWT_EXPIRATION` | `86400000` (24 hours, in ms) | JWT token expiration time |
| `UPLOAD_DIR` | `uploads/advertisements` | Directory where uploaded advertisement images are stored |
| `ACTUATOR_ENDPOINTS` | `health` | Spring Boot Actuator endpoints exposed over HTTP |

## Data storage
The project uses **SQLite** as its database, so there is no need to install any separate database server.

- The database file (`secondhand.db`) is created automatically, in the directory from which the backend is run (by default, the `backend/` folder), the first time the application starts. The schema (tables) is generated and updated automatically from the JPA entity classes (`spring.jpa.hibernate.ddl-auto=update`), so no manual migration step is needed.
- Uploaded advertisement images are stored on the local filesystem, under the `uploads/advertisements` folder (relative to where the backend is run) by default. This path can be overridden with the `UPLOAD_DIR` environment variable.
- No additional configuration is required to run the project with the default settings; the database and upload directories are created automatically on first run.

## API documentation
Once the backend is running, interactive API documentation is available at:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Scalar UI:** `http://localhost:8080/scalar`
- **Raw OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

To test protected endpoints from Swagger/Scalar, first call `POST /api/auth/login`, copy the `token` value from the response, then use the "Authorize" button to attach it as a Bearer token to subsequent requests.

## API overview
All endpoints are prefixed with `/api`. A summary of the main resources:

| Resource | Base path | Description |
|---|---|---|
| Auth | `/api/auth` | Register and log in (returns a JWT) |
| Advertisements | `/api/advertisements` | Create, search, view, update, delete, and mark advertisements as sold |
| Advertisement images | `/api/advertisements/{advertisementId}/images` | Upload, list, and delete advertisement images |
| Categories | `/api/categories` | Browse the category tree; admin-only create/update/delete/activate/deactivate |
| Cities | `/api/cities` | Browse, create, and delete cities |
| Favorites | `/api/favorites` | Add, remove, and list favorite advertisements |
| Chat | `/api/chat` | Start/get conversations and send/receive messages between buyer and seller |
| Seller ratings | `/api/ratings` | Rate a seller and retrieve seller rating summaries |
| Admin — advertisements | `/api/admin/advertisements` | Review, approve, reject, and delete pending advertisements |
| Admin — users | `/api/admin/users` | List users, block/unblock accounts |
| Admin — dashboard | `/api/admin/dashboard` | Aggregate statistics for the admin panel |

Endpoints under `/api/admin/**` require a JWT belonging to a user with the `ADMIN` role. See the Swagger/Scalar documentation above for the full list of endpoints, request/response schemas, and validation rules.

## Project structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/secondhand/
│   │   │       ├── config/         # Configuration classes (security, OpenAPI, web)
│   │   │       ├── controller/     # REST Controllers
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       │   └── response/   # Response DTOs
│   │   │       ├── exception/      # Custom exceptions & global exception handler
│   │   │       ├── model/          # Entities (JPA models)
│   │   │       ├── repository/     # JPA Repositories
│   │   │       ├── security/       # JWT security filter
│   │   │       └── service/        # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-sqlite.properties
│   └── test/                        # Unit/Integration tests
├── pom.xml                          # Maven dependencies
└── ... (mvnw, Dockerfile, etc.)
```
