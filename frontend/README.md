# Secondhand Marketplace — Frontend

This module is the **frontend** of the Alborz secondhand-market project: a desktop client built with **JavaFX** that provides the graphical user interface for browsing, posting, and managing secondhand advertisements. It communicates with the [backend](../backend/README.md) exclusively through its REST API.

## Technologies Used
- **Java 25**
- **JavaFX** (`javafx-controls`, `javafx-fxml`) for the desktop GUI, organized in an MVC-like pattern with FXML views and dedicated controller classes
- **Jackson** (`jackson-databind`, `jackson-datatype-jsr310`) for JSON serialization/deserialization when communicating with the backend REST API
- **Lombok** to reduce boilerplate code
- **JUnit 5** for testing

## Requirements
1. **JDK 25** — this project requires Java 25.
2. **Maven** (version 3.9 or higher) — for managing dependencies and building the project. The Maven Wrapper (`mvnw` / `mvnw.cmd`) is included, so a local Maven installation is not strictly necessary.
3. **Internet connection** (only once, not required every time you run the frontend) — needed for Maven to download dependencies from Maven Central on the first build.
4. **A running instance of the backend** — the frontend cannot function on its own; make sure the backend server is running before starting the client (see [Configuration](#configuration) below for how to point the client at it).

## Configuration
The backend server URL is read from `config/config.properties`:

```properties
API_BASE_URL=http://localhost:8080
```

If the backend is running on a different host or port, update this value accordingly before starting the frontend.

## How to run
1. Through the terminal (using Maven):
   ```
   cd frontend
   ```
   on Linux/Mac: `./mvnw javafx:run`
   on Windows: `mvnw.cmd javafx:run`
2. Through IntelliJ IDEA:
   Open `Launcher.java` and click the "Run" icon next to the `main()` method.

Note that in order to prevent the JVM's module check from failing to locate separate JavaFX named modules (such as `javafx.graphics`, etc.), we use a `Launcher` class that does not directly extend the `javafx.application.Application` class.

### Running the tests
```
./mvnw test
```
(or `mvnw.cmd test` on Windows)

## Features
- **Authentication**: registration and login screens, with the JWT issued by the backend kept in an in-memory session for the duration of the app's run.
- **Browsing & searching advertisements**: keyword search, filtering by category (including subcategories), city, and price range, plus sorting options.
- **Advertisement management**: creating, editing, deleting, and marking your own advertisements as sold, including image uploads.
- **Favorites**: adding/removing advertisements to/from a personal favorites list.
- **Chat**: starting a conversation with a seller from an advertisement page and exchanging messages with buyers/sellers.
- **Seller ratings**: rating a seller after a deal and viewing a seller's average rating.
- **Admin panel**: a dedicated view for administrators to review pending advertisements (approve/reject), manage categories and cities, and block/unblock users.
- **Theming**: light/dark mode switching.
- **UX utilities**: form validation with inline feedback and toast notifications for success/error messages.

## Project structure

```text
frontend/
├── config/
│   └── config.properties            # Server URL (for connecting to backend)
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/secondhandfx/
│       │       ├── controller/      # JavaFX Controllers
│       │       ├── exception/       # Custom exceptions
│       │       ├── Launcher.java    # Entry point
│       │       ├── MainApplication.java # Main JavaFX class
│       │       ├── model/           # Data models (DTOs)
│       │       ├── service/         # Services communicating with the backend
│       │       └── util/            # Utilities (session, HTTP client, alerts, theme, validation, navigation, etc.)
│       └── resources/
│           ├── com/example/secondhandfx/fxml/   # FXML files (views)
│           └── css/                             # Stylesheets (themes & components)
└── pom.xml                          # Maven dependencies
```
