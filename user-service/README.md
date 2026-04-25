# User Service

A microservice responsible for user profile management in the Book Store Platform. This service handles user registration, profile retrieval, profile updates, and user information management for the distributed book store system.

## Overview

The User Service is a Spring Boot-based microservice that provides REST APIs for managing user profiles and information. It's designed as part of a microservices architecture and integrates with a service discovery system (Eureka) for dynamic service registration and discovery.

## Key Features

- **User Profile Management**: Create, retrieve, and update user profiles
- **Comprehensive User Information**: Store and manage email, name, contact details, and address information
- **RESTful API**: Clean and intuitive REST endpoints for user operations
- **Transactional Operations**: Ensures data consistency with Spring's transaction management
- **Service Discovery**: Integrated with Spring Cloud Eureka for service discovery
- **Health Monitoring**: Built-in actuator endpoints for service health checks
- **Docker Support**: Pre-configured Docker setup for containerized deployment
- **PostgreSQL Database**: Persistent storage with PostgreSQL for reliability

## Technology Stack

- **Framework**: Spring Boot 3.5.12
- **Java Version**: OpenJDK 17
- **Database**: PostgreSQL
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven 3.9
- **Service Discovery**: Spring Cloud Eureka Client
- **Validation**: Jakarta Bean Validation
- **Mapping**: MapStruct 1.5.5
- **Lombok**: 1.18.30 (for boilerplate reduction)
- **Testing**: JUnit 5, Spring Boot Test

## Architecture

### Component Structure

```
user-service/
├── controller/          # REST API endpoints
├── service/            # Business logic
├── model/              # JPA entities
├── dto/                # Data Transfer Objects
├── mapper/             # Entity to DTO mapping
└── repository/         # Data access layer
```

### API Endpoints

#### Base URL
```
http://localhost:8081/users
```

#### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user profile by ID |
| GET | `/users/me` | Get current user profile (via X-User-Id header) |
| PUT | `/users/me` | Update current user profile |

#### Request/Response Headers
- **X-User-Id**: Required for `/me` endpoints to identify the current user. Defaults to `1` if not provided.

### User Profile Schema

#### UserProfile Entity
```
- id (Long) - Primary key
- email (String) - Unique email address
- firstName (String) - User's first name
- lastName (String) - User's last name
- phoneNumber (String) - Contact phone number
- address (String) - Street address
- city (String) - City
- state (String) - State/Province
- zipCode (String) - Postal code
- createdAt (LocalDateTime) - Creation timestamp (auto-generated)
- updatedAt (LocalDateTime) - Last update timestamp (auto-updated)
```

#### UserProfileUpdateRequest
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1-234-567-8900",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001"
}
```

#### UserProfileResponse
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1-234-567-8900",
  "address": "123 Main Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001"
}
```

## Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- PostgreSQL 12 or higher
- Docker (optional, for containerized deployment)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd book-nest/user-service
```

### 2. Configure Database

Ensure PostgreSQL is running and create the required database:

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database and user
CREATE DATABASE userdb;
CREATE USER bookstore_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE userdb TO bookstore_user;
```

### 3. Configure Environment Variables

Edit `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: bookstore_user
    password: <your-password>
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## Building the Project

### Build with Maven

```bash
# Build the project
mvn clean package

# Build only this service (from parent directory)
mvn clean package -pl user-service -am
```

### Build Without Tests

```bash
mvn clean package -DskipTests
```

## Running the Service

### Run Locally

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Run the JAR file
java -jar target/user-service-1.0.0.jar
```

The service will be available at: `http://localhost:8081`

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### View Actuator Endpoints

```bash
curl http://localhost:8081/actuator
```

## Docker Support

### Build Docker Image

```bash
# From the project root directory
docker build -t user-service:1.0.0 -f user-service/Dockerfile .
```

### Run Docker Container

```bash
# Run with external PostgreSQL
docker run -d \
  --name user-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-host:5432/userdb \
  -e SPRING_DATASOURCE_USERNAME=bookstore_user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  user-service:1.0.0

# Run with Docker Compose
docker compose up user-service
```

### Health Check

The Docker container includes a built-in health check:

```bash
docker inspect --format='{{.State.Health.Status}}' user-service
```

## Configuration Options

### Application Properties (`application.yml`)

```yaml
server:
  port: 8081                          # Service port

spring:
  application:
    name: user-service              # Service name for discovery
  datasource:
    url: jdbc:postgresql://...      # Database connection URL
    username: bookstore_user        # Database user
    password: password              # Database password
  jpa:
    hibernate:
      ddl-auto: update              # Auto-create/update schema
    show-sql: true                  # Log SQL statements

# Uncomment for Eureka service discovery
# eureka:
#   client:
#     service-url:
#       defaultZone: http://localhost:8761/eureka/
#     enabled: false
```

## Testing

### Run Tests

```bash
mvn test
```

### Test with Coverage

```bash
mvn clean test
```

The test suite uses:
- H2 in-memory database for test isolation
- Spring Boot Test Framework
- JUnit 5

## API Examples

### Get All Users

```bash
curl -X GET http://localhost:8081/users
```

### Get User by ID

```bash
curl -X GET http://localhost:8081/users/1
```

### Get Current User Profile

```bash
curl -X GET http://localhost:8081/users/me \
  -H "X-User-Id: 1"
```

### Update User Profile

```bash
curl -X PUT http://localhost:8081/users/me \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+1-555-123-4567",
    "address": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90001"
  }'
```

## Service Dependencies

### Internal Dependencies
- Relies on authentication/user creation events from the Auth Service
- Integrates with service discovery for inter-service communication

### External Dependencies
- PostgreSQL database
- Eureka Service Discovery Server (optional)
- Spring Cloud infrastructure

## Troubleshooting

### Database Connection Issues

```
Error: Unable to connect to PostgreSQL
Solution: Verify PostgreSQL is running and credentials in application.yml are correct
```

```bash
# Test PostgreSQL connection
psql -h localhost -U bookstore_user -d userdb
```

### Port Already in Use

```
Error: Address already in use
Solution: Change port in application.yml or kill process using port 8081
```

```bash
# macOS/Linux
lsof -i :8081
kill -9 <PID>
```

### Build Failures

```bash
# Clean build
mvn clean package

# If MapStruct issues occur
mvn clean compile
```

## Deployment

### Production Checklist

- [ ] Update database credentials (not hardcoded)
- [ ] Enable HTTPS/TLS
- [ ] Configure proper logging levels
- [ ] Set up monitoring and alerting
- [ ] Configure resource limits (CPU/Memory)
- [ ] Enable request validation
- [ ] Set up backup strategy
- [ ] Configure CORS appropriately for production

### Environment Configuration

```bash
# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/userdb
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=<secure-password>
export SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

## Monitoring & Logging

### Actuator Health Endpoint

```bash
curl http://localhost:8081/actuator/health
```

### Enable SQL Logging

Edit `application.yml`:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.hibernate.SQL: DEBUG
```

## Contributing

1. Follow Spring Boot best practices
2. Use MapStruct for entity/DTO mapping
3. Maintain transactional consistency
4. Write unit tests for business logic
5. Document API endpoints and request/response formats

## Project Structure

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/com/bookstore/user/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── service/          # Business logic
│   │   │   ├── model/            # JPA entities
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── mapper/           # MapStruct mappers
│   │   │   ├── repository/       # JPA repositories
│   │   │   └── UserServiceApplication.java
│   │   └── resources/
│   │       └── application.yml   # Configuration
│   └── test/                      # Unit tests
├── pom.xml                         # Maven configuration
├── Dockerfile                      # Docker image definition
└── README.md                       # This file
```

## License

[Include appropriate license information]

## Support

For issues, questions, or contributions, please refer to the main project repository.

## Version History

| Version | Release Date | Notes |
|---------|--------------|-------|
| 1.0.0 | 2026-04-24 | Initial release |

