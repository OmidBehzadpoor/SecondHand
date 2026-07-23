# 🖥️ Secondhand Marketplace — Frontend
 
This module is the **frontend** of the Alborz secondhand-market project: a desktop client built with **JavaFX** that provides the graphical user interface for browsing, posting, and managing secondhand advertisements. It communicates with the [backend](../backend/README.md) exclusively through its REST API.
 
---
 
## 🧰 Technologies Used
 
- **Java 25**
- **JavaFX** (`javafx-controls`, `javafx-fxml`) for the desktop GUI, organized in an MVC-like pattern with FXML views and dedicated controller classes
- **Jackson** (`jackson-databind`, `jackson-datatype-jsr310`) for JSON serialization/deserialization when communicating with the backend REST API
- **Lombok** to reduce boilerplate code
- **JUnit 5** for testing
---
 
## ⚙️ Requirements
 
1. **JDK 25** — this project requires Java 25.
2. **Maven** (version 3.9 or higher) — for managing dependencies and building the project. The Maven Wrapper (`mvnw` / `mvnw.cmd`) is included, so a local Maven installation is not strictly necessary. (Not needed if you just [download a pre-built release jar](#-downloading-a-pre-built-release).)
3. **Internet connection** — needed once for Maven to download dependencies on the first build, and every time the app runs, to reach the backend API.
4. **A backend to talk to** — by default the app points at the hosted Render deployment, so it works out of the box with no setup; see [Configuration](#-configuration) below if you want to point it at a local backend instead.
---
 
## 🔧 Configuration
 
The backend server URL is read from `config/config.properties`, which is **not** committed to the repo — only a template, `config/config.properties.example`, is:
 
```properties
API_BASE_URL=http://localhost:8080
```
 
> [!NOTE]
> If `config/config.properties` does not exist, the app does **not** fail — it silently falls back to the hosted backend at `https://secondhand-6kfg.onrender.com`. You only need to create this file if you want to point the client at a different backend (e.g. one running locally).
 
To point the client at your own backend:
1. Copy the template: `cp config/config.properties.example config/config.properties` (or copy it manually on Windows).
2. Edit `API_BASE_URL` in the new file to your backend's URL (e.g. `http://localhost:8080`).
3. Restart the app — the value is read once, at startup.
---
 
## 🚀 How to run
 
1. Through the terminal (using Maven):
```bash
   cd frontend
```
   on Linux/Mac: `./mvnw javafx:run`
   on Windows: `mvnw.cmd javafx:run`
2. Through IntelliJ IDEA:
   Open `Launcher.java` and click the "Run" icon next to the `main()` method.
 
Note that in order to prevent the JVM's module check from failing to locate separate JavaFX named modules (such as `javafx.graphics`, etc.), we use a `Launcher` class that does not directly extend the `javafx.application.Application` class.
 
### Running the tests
```bash
./mvnw test
```
(or `mvnw.cmd test` on Windows)
 
---
 
## 📦 Downloading a pre-built release
 
If you don't want to build from source, a runnable jar is published automatically on every push to the frontend, as a GitHub Release: **[Latest Frontend Build (Desktop)](https://github.com/OmidBehzadpoor/SecondHand/releases/tag/latest-frontend)**.
 
Download the jar matching your OS from that release's assets and run it directly:
```bash
java -jar secondhand-frontend-ubuntu-latest.jar    # Linux
java -jar secondhand-frontend-windows-latest.jar   # Windows
java -jar secondhand-frontend-macos-latest.jar     # macOS
```
 
> [!NOTE]
> `latest-frontend` is a rolling tag — its assets are overwritten by an automated build (one jar per OS: Linux, Windows, macOS) on every update, so it always reflects the most recent frontend commit rather than a fixed version number.
 
---
 
## 🏗️ Building a runnable jar yourself
 
```bash
cd frontend
./mvnw package
```
The `maven-shade-plugin` bundles all dependencies (JavaFX, Jackson, etc.) into a single runnable jar at `target/secondhand-frontend.jar`, with `Launcher` set as the main class. This is the same jar published in the pre-built GitHub release described above.
 
---
 
## ✨ Features
 
- **Authentication**: registration and login screens, with the JWT issued by the backend kept in an in-memory session for the duration of the app's run (you'll need to log in again each time you start the app).
- **Browsing & searching advertisements**: keyword search, filtering by category (including subcategories) and city, price range (min/max), sorting (newest, oldest, cheapest, most expensive), and pagination with a selectable page size (12/24/48 items).
- **Advertisement management**: creating, editing, deleting, and marking your own advertisements as sold, including image uploads.
- **Favorites**: adding/removing advertisements to/from a personal favorites list.
- **Chat**: starting a conversation with a seller from an advertisement page and exchanging messages with buyers/sellers.
- **Seller ratings**: rating a seller after a deal and viewing a seller's average rating.
- **Admin panel**: a dedicated view for administrators to review pending advertisements (approve/reject), manage categories and cities, and block/unblock users.
- **Theming**: light/dark mode switching, with the chosen theme persisted across app restarts (via Java `Preferences`, stored per OS user — not in `config.properties`).
- **UX utilities**: form validation with inline feedback, and toast-style pop-up notifications (auto-dismissing) for success/error/info messages.
---
 
## 📁 Project structure
 
```text
frontend/
├── config/
│   └── config.properties.example    # Template — copy to config.properties to override the backend URL
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/secondhandfx/
│   │   │       ├── controller/      # JavaFX Controllers
│   │   │       ├── exception/       # Custom exceptions
│   │   │       ├── model/           # Data models (DTOs)
│   │   │       ├── service/         # Services communicating with the backend
│   │   │       ├── util/            # Utilities (session, HTTP client, alerts, theme, validation, navigation, etc.)
│   │   │       ├── Launcher.java    # Entry point (avoids JavaFX module-path issues)
│   │   │       └── MainApplication.java # Main JavaFX Application class
│   │   └── resources/
│   │       ├── com/example/secondhandfx/fxml/   # FXML files (views)
│   │       └── css/                             # Stylesheets (themes & components)
│   └── test/                        # Unit tests (session, theme, validation utilities)
└── pom.xml                          # Maven dependencies & build (javafx-maven-plugin, maven-shade-plugin)
```
 
