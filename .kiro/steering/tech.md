# Technology Stack & Build System

## Backend Stack
- **Framework**: Spring Boot 3.5.10
- **Java Version**: 21
- **Build Tool**: Maven
- **Database**: H2 (dev), MySQL (prod)
- **Security**: Spring Security with JWT authentication
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit, Spring Boot Test
- **AOP**: AspectJ for logging
- **Email**: Spring Mail

## Frontend Stack
- **Framework**: React 19.0.0
- **Build Tool**: Create React App (react-scripts 5.0.1)
- **Routing**: React Router DOM 7.0.0
- **HTTP Client**: Axios 1.5.1
- **Charts**: Recharts (fm-web only)

## Key Dependencies
- **Lombok**: Code generation
- **Apache Commons**: Utilities
- **JJWT**: JWT token handling
- **JaCoCo**: Code coverage

## Common Commands

### Backend (Maven)
```bash
# Clean and build
mvn clean package

# Run application
java -jar target/fm.jar
# OR
mvn spring-boot:run

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report
```

### Frontend (React Apps)
```bash
# Main web app
cd fm-web
npm start          # Development server (port 3000)
npm run build      # Production build
npm test           # Run tests

# Admin app  
cd fm-admin
npm start          # Development server (port 3001)
npm run build      # Production build
npm test           # Run tests
```

## Development Setup
1. Ensure Java 21 is installed
2. Backend runs on port 8080 by default
3. Frontend apps run on ports 3000 (main) and 3001 (admin)
4. CORS is configured for localhost:3000 and localhost:3001
5. H2 database file stored at `~/test`

## Configuration
- Main config: `src/main/resources/application.properties`
- Production config: `src/main/resources/application-prod.properties`
- JWT secret and expiration configurable via properties
- Database connection configurable (H2 vs MySQL)