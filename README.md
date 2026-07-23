# Alborz secondhand-market project


Contributors:

- Omid BehzadPour

- Parsa Zebardast

# Brief Description of the project
This is an application for secondhand marketing written in Java. The application consists of two separate Maven projects: backend and frontend. These two applications are related to each other through REST API.

# Backend
Backend is implemented using the Spring Boot framework, which provides a robust and scalable foundation for building RESTful APIs. It follows a layered architecture consisting of controllers, services, repositories, and DTOs. The backend handles authentication via JWT (JSON Web Tokens), manages business logic such as advertisement creation, user management, chat, and seller ratings, and provides secure endpoints for both regular users and administrators. It uses SQLite as its embedded database, which eliminates the need for separate database installation and makes the setup process straightforward.

### Backend's requirements
1. JDK 25: this project works with Java 25.
2. Maven: for managing the dependencies and building the project (version 3.9 or higher).
3. Internet connection (just once, not for every time you want to run backend): for Maven to download the dependencies from Maven Central.

Note that the project uses SQLite as its database, so no need to install any separate database. The secondhand.db (database file) is automatically created in the project's root once you run the project.

### How to run the backend
1. Through terminal (using Maven):  
  cd backend  
  on Linux/Mac: ./mvnw spring-boot:run  
  on Windows:   mvnw.cmd spring-boot:run  
3. Through IntelliJ IDEA:  
   Open SecondhandApplication.java and click on the "Run" icon next to the main() method.

### The general structure of `/backend`

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/secondhand/
│   │   │       ├── config/         # Configuration classes
│   │   │       ├── controller/     # REST Controllers
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       │   └── response/   # Response DTOs
│   │   │       ├── exception/      # Custom exceptions & handler
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

# Frontend
The frontend is a desktop application built with JavaFX, which provides the graphical user interface (GUI). It is organized following an MVC-like pattern, with FXML files defining the views and controllers handling user interactions and navigation. The frontend communicates with the backend through REST API calls, using a custom HTTP client that manages JSON serialization, error handling, and authentication headers. It also includes utilities for session management, theme switching (light/dark mode), form validation, and toast notifications, ensuring a smooth and consistent user experience.

### How to run the frontend
1. Through terminal (using Maven):  
   cd frontend  
   ./mvnw javafx:run   (Linux/Mac)  
   mvnw.cmd javafx:run (Windows)  
2. Through IntelliJ IDEA:  
   Open Launcher.java and click on the "Run" icon next to the main() method.  

Note that in order for the JVM check to find separate JavaFX named modules (like javafx.graphics and etc.) not to fail, we are using a Launcher class which does not directly extend javafx.application.Application class.


### The general structure of `/frontend`

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
│       │       ├── service/         # Services communicating with backend
│       │       └── util/            # Utilities (session, HTTP client, alerts, theme, etc.)
│       └── resources/
│           ├── com/example/secondhandfx/fxml/   # FXML files (views)
│           └── css/                             # Stylesheets (themes & components)
└── pom.xml                          # Maven dependencies
```

