# Project Structure & Organization

## Root Structure
```
fm/
├── src/                    # Java backend source
├── fm-web/                 # React main application
├── fm-admin/               # React admin application  
├── tasks/                  # Implementation roadmap
├── .kiro/                  # Kiro configuration
├── pom.xml                 # Maven configuration
└── README.md
```

## Backend Structure (src/)
```
src/main/java/com/lollito/fm/
├── FmApplication.java           # Main Spring Boot application
├── DatabaseLoader.java         # Data initialization
├── ScheduledTasks.java         # Scheduled operations
├── aop/                        # Aspect-oriented programming
│   └── logging/                # Logging aspects
├── config/                     # Configuration classes
│   ├── security/               # Security & JWT config
│   └── SwaggerConfig.java      # API documentation
├── controller/rest/            # REST API endpoints
│   └── errors/                 # Error handling
├── model/                      # JPA entities & DTOs
│   └── rest/                   # REST-specific models
├── repository/rest/            # Data access layer
├── service/                    # Business logic layer
└── utils/                      # Utility classes
```

## Frontend Structure
Both `fm-web` and `fm-admin` follow standard React structure:
```
src/
├── App.js                 # Main component
├── index.js              # Entry point
├── components/           # Reusable UI components
├── pages/                # Page-level components
├── context/              # React context (auth, etc.)
├── services/             # API service layer
├── styles/               # CSS styling
├── assets/               # Static assets
└── hooks/                # Custom React hooks (fm-web only)
```

## Package Conventions

### Java Packages
- **controller.rest**: REST API endpoints grouped by domain
- **model**: JPA entities for database mapping
- **model.rest**: DTOs for API requests/responses
- **repository.rest**: Spring Data JPA repositories
- **service**: Business logic, one service per domain
- **config**: All configuration classes
- **utils**: Stateless utility classes

### React Components
- **components**: Reusable UI components (Layout, Navbar, etc.)
- **pages**: Route-specific page components
- **context**: Global state management
- **services**: API communication layer

## Naming Conventions
- **Java**: PascalCase for classes, camelCase for methods/variables
- **REST endpoints**: `/api/{domain}/` pattern
- **Database**: Snake_case for table/column names (via Hibernate)
- **React**: PascalCase for components, camelCase for functions
- **Files**: kebab-case for CSS, PascalCase for React components

## Key Architectural Patterns
- **Layered Architecture**: Controller → Service → Repository → Entity
- **DTO Pattern**: Separate models for API and database
- **Repository Pattern**: Spring Data JPA repositories
- **JWT Authentication**: Stateless security with tokens
- **AOP**: Cross-cutting concerns (logging) via aspects
- **Scheduled Tasks**: Background processing for match simulation