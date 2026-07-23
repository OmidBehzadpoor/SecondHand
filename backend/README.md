# 🏪 Secondhand Marketplace — Backend
 
This module is the **backend** of the Alborz secondhand-market project: a RESTful API server built with **Spring Boot** that provides authentication, advertisement management, chat, favorites, seller ratings, and administrative operations for the application. The **frontend** (a JavaFX desktop client) consumes this API; see [`../frontend/README.md`](../frontend/README.md) for details on the client side.
 
---
 
## 🧰 Technologies Used
- **Java 25**
- **Spring Boot** — Spring Web MVC, Spring Data JPA, Spring Security, Spring Validation
- **SQLite** as the embedded database, accessed through Hibernate (community SQLite dialect)
- **JWT (JJWT library)** for stateless authentication and authorization
- **Lombok** to reduce boilerplate code
- **springdoc-openapi (Swagger UI) + Scalar** for interactive API documentation
- **JUnit 5 & Mockito** for unit and integration testing
---
 
## ⚙️ Requirements
1. **JDK 25** — this project requires Java 25.
2. **Maven** (version 3.9 or higher) — for managing dependencies and building the project. The Maven Wrapper (`mvnw` / `mvnw.cmd`) is included, so a local Maven installation is not strictly necessary.
3. **Internet connection** (only once, not required every time you run the backend) — needed for Maven to download dependencies from Maven Central on the first build.
No separate database server needs to be installed; SQLite is embedded and its file is created automatically (see [Data storage](#-data-storage) below).
 
---
 
## 🚀 How to run
1. Through the terminal (using Maven):
```bash
   cd backend
```
   on Linux/Mac: `./mvnw spring-boot:run`
   on Windows: `mvnw.cmd spring-boot:run`
2. Through IntelliJ IDEA:
   Open `SecondhandApplication.java` and click the "Run" icon next to the `main()` method.
 
By default, the server starts on **`http://localhost:8080`**.
 
---
 
## 🌐 Live deployment
Besides running locally, this backend is deployed on **Render** and publicly reachable at:
**[https://secondhand-6kfg.onrender.com](https://secondhand-6kfg.onrender.com/)**
 
The same API documentation (Swagger UI, Scalar, and raw OpenAPI JSON — see [API documentation](#-api-documentation) below) is available on the deployed instance by replacing `http://localhost:8080` with the Render URL above (e.g. `https://secondhand-6kfg.onrender.com/swagger-ui.html`).
 
> [!NOTE]
> The deployed instance may spin down after periods of inactivity (Render free tier), so the first request after a while can take a few extra seconds to respond.
 
### Running the tests
```bash
./mvnw test
```
(or `mvnw.cmd test` on Windows)
 
---
 
## 🔧 Configuration
The application reads its configuration from `src/main/resources/application.properties` and `application-sqlite.properties`. The following values can be overridden through environment variables without editing the source code:
 
| Environment variable | Default | Description |
|----------------------|---------|-------------|
| `JWT_SECRET` | a placeholder secret (should be overridden in production) | Secret key used to sign JWT tokens; must be at least 32 characters long |
| `JWT_EXPIRATION` | `86400000` (24 hours, in ms) | JWT token expiration time |
| `UPLOAD_DIR` | `uploads/advertisements` | Directory where uploaded advertisement images are stored |
| `ACTUATOR_ENDPOINTS` | `health` | Spring Boot Actuator endpoints exposed over HTTP |
 
> [!IMPORTANT]
> The default `JWT_SECRET` is only a placeholder. Always set a strong, random `JWT_SECRET` (32+ characters) via an environment variable in any deployment — never rely on the default outside local development.
 
---
 
## 💾 Data storage
The project uses **SQLite** as its database, so there is no need to install any separate database server.
 
- The database file (`secondhand.db`) is created automatically, in the directory from which the backend is run (by default, the `backend/` folder), the first time the application starts. The schema (tables) is generated and updated automatically from the JPA entity classes (`spring.jpa.hibernate.ddl-auto=update`), so no manual migration step is needed.
- Uploaded advertisement images are stored on the local filesystem, under the `uploads/advertisements` folder (relative to where the backend is run) by default. This path can be overridden with the `UPLOAD_DIR` environment variable.
- No additional configuration is required to run the project with the default settings; the database and upload directories are created automatically on first run.
---
 
## 📄 API documentation
Once the backend is running, interactive API documentation is available at:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Scalar UI:** `http://localhost:8080/scalar`
- **Raw OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
> [!TIP]
> To test protected endpoints from Swagger/Scalar, first call `POST /api/auth/login`, copy the `token` value from the response, then use the "Authorize" button to attach it as a Bearer token to subsequent requests.
 
---
 
## 📋 API overview
All endpoints are prefixed with `/api`. Access levels below: **Public** (no token needed), **Authenticated** (any logged-in user), **Admin** (JWT must belong to a user with the `ADMIN` role).
 
> [!WARNING]
> Calling an **Admin** endpoint with a non-admin (or missing) token returns `403 Forbidden`. There is no self-service way to become an admin through the API — the `ADMIN` role must be assigned directly in the database.
 
### 🔐 Auth — `/api/auth`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Log in and receive a JWT |
 
### 📦 Advertisements — `/api/advertisements`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/advertisements` | Authenticated | Create a new advertisement |
| GET | `/api/advertisements/mine` | Authenticated | List the current user's own advertisements |
| GET | `/api/advertisements/{id}` | Public | Get a single advertisement by id |
| GET | `/api/advertisements` | Public | Search/list advertisements (paginated, filterable) |
| PUT | `/api/advertisements/{id}` | Authenticated | Update an advertisement (owner only) |
| DELETE | `/api/advertisements/{id}` | Authenticated | Delete an advertisement (owner only) |
| PATCH | `/api/advertisements/{id}/sold` | Authenticated | Mark an advertisement as sold |
 
### 🖼️ Advertisement images — `/api/advertisements/{advertisementId}/images`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/advertisements/{advertisementId}/images` | Public | List images of an advertisement |
| POST | `/api/advertisements/{advertisementId}/images` | Authenticated | Upload an image (`multipart/form-data`) |
| DELETE | `/api/advertisements/{advertisementId}/images/{imageId}` | Authenticated | Delete an image (owner only) |
 
### 🏷️ Categories — `/api/categories`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/categories` | Public | Browse the active category tree |
| GET | `/api/categories/admin` | Admin | List all categories, including inactive ones |
| POST | `/api/categories` | Admin | Create a category |
| PUT | `/api/categories/{id}` | Admin | Update a category |
| DELETE | `/api/categories/{id}` | Admin | Delete a category |
| PATCH | `/api/categories/{id}/activate` | Admin | Activate a category |
| PATCH | `/api/categories/{id}/deactivate` | Admin | Deactivate a category |
 
### 🏙️ Cities — `/api/cities`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/cities` | Public | List all cities |
| POST | `/api/cities` | Admin | Create a city |
| DELETE | `/api/cities/{id}` | Admin | Delete a city |
 
### ❤️ Favorites — `/api/favorites`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/favorites` | Authenticated | List the current user's favorite advertisements |
| POST | `/api/favorites/{advertisementId}` | Authenticated | Add an advertisement to favorites |
| DELETE | `/api/favorites/{advertisementId}` | Authenticated | Remove an advertisement from favorites |
 
### 💬 Chat — `/api/chat`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/chat/conversations/advertisement/{adId}` | Authenticated | Start, or get, the conversation for an advertisement |
| GET | `/api/chat/conversations` | Authenticated | List the current user's conversations |
| GET | `/api/chat/conversations/{conversationId}/messages` | Authenticated | Get messages in a conversation |
| POST | `/api/chat/conversations/{conversationId}/messages` | Authenticated | Send a message in a conversation |
 
### ⭐ Seller ratings — `/api/ratings`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/ratings/advertisements/{advertisementId}` | Authenticated | Rate the seller of an advertisement |
| GET | `/api/ratings/sellers/{sellerId}` | Authenticated | List all ratings received by a seller |
| GET | `/api/ratings/sellers/{sellerId}/average` | Authenticated | Get a seller's average rating |
| GET | `/api/ratings/sellers/{sellerId}/count` | Authenticated | Get a seller's total number of ratings |
 
> [!NOTE]
> Unlike advertisements/categories/cities, `/api/ratings/**` has no `permitAll` rule in `SecurityConfig`, so it falls through to the default `anyRequest().authenticated()` — a valid JWT is required even for these read-only GET endpoints. If these are meant to be publicly viewable (e.g. shown to visitors before they sign up), add an explicit `.requestMatchers(HttpMethod.GET, "/api/ratings/sellers/**").permitAll()` rule.
 
### 🛠️ Admin — advertisements — `/api/admin/advertisements`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/admin/advertisements/pending` | Admin | List advertisements pending approval |
| GET | `/api/admin/advertisements/all` | Admin | List all advertisements |
| PATCH | `/api/admin/advertisements/{id}/approve` | Admin | Approve an advertisement |
| PATCH | `/api/admin/advertisements/{id}/reject` | Admin | Reject an advertisement |
| DELETE | `/api/admin/advertisements/{id}` | Admin | Delete an advertisement |
 
### 👥 Admin — users — `/api/admin/users`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/admin/users` | Admin | List all users |
| PATCH | `/api/admin/users/{userId}/block` | Admin | Block a user account |
| PATCH | `/api/admin/users/{userId}/unblock` | Admin | Unblock a user account |
 
### 📊 Admin — dashboard — `/api/admin/dashboard`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/admin/dashboard` | Admin | Aggregate statistics for the admin panel |
 
See the Swagger/Scalar documentation above for full request/response schemas and validation rules.
 
---
 
## 📁 Project structure
 
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
 
