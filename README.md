# IdeaMatch

**Idea Match – where ideas meet makers.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Project Overview
IdeaMatch is a backend application that connects innovators with collaborators through a platform for idea sharing and real-time communication. The system enables users to create, discover, and collaborate on innovative projects using advanced filtering capabilities, secure authentication, and integrated chat functionality.

Built with Java 21 and Spring Boot 3.4.5, the application follows clean architecture principles with command pattern implementation, providing scalable real-time messaging through WebSocket and Apache Kafka, robust JWT-based security with email verification, and efficient data management using PostgreSQL and MongoDB.

## Key Features

### 🔐 User Management System
- **Secure Authentication**: JWT-based stateless authentication with AWS SES email verification
- **Profile Management**: Comprehensive user profiles with customizable fields
- **Advanced Security**: BCrypt password hashing with strength validation using Passay
- **Password Recovery**: Email-based password reset workflow with secure token generation


### 💡 Idea Management System
- **CRUD Operations**: Full lifecycle management for project ideas
- **Advanced Filtering**: RSQL-based dynamic query system with pagination
- **Status Tracking**: Comprehensive idea lifecycle (DRAFT → ACTIVE → COMPLETED)
- **Categorization**: Technology, Business, Creative, Social Impact, and Education categories

### 💬 Real-Time Chat System
- **WebSocket Communication**: STOMP protocol for instant messaging
- **Message Persistence**: MongoDB storage for chat history and message tracking
- **Status Tracking**: Comprehensive message states (SENT → DELIVERED → READ)
- **User Presence**: Connection status tracking with session management
- **Scalable Architecture**: Apache Kafka integration for message distribution
- **Security**: WebSocket authentication interceptors with JWT validation

## 🛠️ Tech Stack

### Core Framework
- **Java 21**: Modern language features with enhanced performance and syntax improvements
- **Spring Boot 3.4.5**: Main application framework with auto-configuration and embedded server
- **Spring Security 6.x**: Comprehensive authentication, authorization, and security filters
- **Spring Data JPA 3.x**: Database abstraction layer with repository pattern implementation
- **Spring WebSocket 6.x**: Real-time communication support with STOMP protocol

### Databases & Persistence
- **PostgreSQL 15**: Primary relational database for user accounts and idea management
- **MongoDB**: Document database optimized for chat messages and real-time data
- **Redis 7**: In-memory data store for caching and session management

### Messaging & Real-Time Communication
- **Apache Kafka 4.0.0**: High-performance message streaming and event distribution
- **WebSocket with STOMP**: Bidirectional real-time communication protocol
- **Custom Message Handlers**: WebSocket authentication and message routing

### Security & Integration
- **JWT 4.5.0**: Stateless authentication with token-based security
- **AWS SES 2.31.68**: Cloud email service for verification and notifications
- **Passay 1.6.6**: Advanced password strength validation library
- **BCrypt**: Industry-standard password hashing algorithm

### Development & Testing Tools
- **Maven 3.8+**: Build automation, dependency management, and project lifecycle
- **Lombok**: Annotation-based boilerplate code reduction
- **MapStruct 1.6.3**: Compile-time type-safe bean mapping between DTOs and entities
- **SpringDoc OpenAPI 2.8.5**: Automated API documentation generation
- **TestContainers 1.20.3**: Integration testing with containerized databases
- **Docker**: Containerization for development environment setup

## 🚀 Getting Started

### Prerequisites
- **Java 21** or higher
- **Maven 3.8+** for build management
- **Docker & Docker Compose** for infrastructure services
- **Git** for version control

### Quick Start
1. **Clone the repository**:
   ```bash
   git clone <https://github.com/krzysztofKolodziej/idea-match.git>
   cd idea-match
   ```

2. **Set up environment variables**:
   ```bash
   # Create a local environment file from the template
   cp .env.example .env
   # Then edit .env with your values
   ```

3. **Start infrastructure services**:
   ```bash
   docker-compose up -d
   ```
   This starts:
   - **PostgreSQL** (port 5432) - User and idea data
   - **MongoDB** (port 27017) - Chat messages  
   - **Redis** (port 6379) - Caching layer
   - **Apache Kafka** (port 9094) - Message streaming

4. **Build and run the application**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application will be available at `http://localhost:8080`

### Development Profiles
- **dev** (default): Development mode with detailed logging
- **test**: Testing environment with H2 in-memory database

## 📡 API Documentation

### Authentication Endpoints
```http
POST /api/registration              # Register new user account
GET  /api/verify-email?token={}     # Verify email address with token
POST /api/login                     # User authentication
POST /api/auth/forgot-password      # Request password reset
GET  /api/auth/reset-password?token={}  # Validate reset token
POST /api/auth/reset-password       # Reset user password
```

### User Profile Management
```http
GET  /api/user/profile              # Get user profile information
PUT  /api/user/profile              # Update user profile
POST /api/user/change-password      # Change user password
```

### Idea Management
```http
GET    /api/ideas                   # Get paginated and filtered ideas
GET    /api/ideas/{id}              # Get detailed idea information
POST   /api/account/idea            # Create new idea (authenticated)
PATCH  /api/account/idea/{id}       # Update existing idea (owner only)
DELETE /api/account/idea/{id}       # Delete idea (owner only)
```

### WebSocket Communication
```javascript
// Connection
CONNECT /ws

// Message Operations
SEND /app/sendMessage               # Send message
SEND /app/markAsRead                # Mark message as read
SEND /app/markAsDelivered          # Mark message as delivered
SEND /app/connect                   # Handle user connection

// Subscriptions
SUBSCRIBE /topic/public             # Public messages
SUBSCRIBE /user/queue/messages      # Private messages
```

### Interactive API Documentation
Once the application is running, visit:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

## 🗄️ Database Architecture

### PostgreSQL Schema
- **`users`**: User accounts, profiles, authentication data, and project relationships
- **`ideas`**: Project ideas with status, category, description, and collaboration details
- **`user_ideas`**: Many-to-many relationship table for collaborations
- **`roles`**: User role definitions and permissions
- **`verification_tokens`**: Email verification and password reset tokens

### MongoDB Collections
- **`chatMessages`**: Message history with content, status, timestamps, and user references
- **`messageStatus`**: Message delivery and read tracking for real-time status updates
- **Connection tracking**: User presence and WebSocket session management

## 🔒 Security & Architecture

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication with configurable expiration (30 days default)
- **Token Blacklisting**: Secure logout with Redis-backed token invalidation
- **Password Security**: BCrypt hashing with Passay strength validation rules
- **Email Verification**: AWS SES integration for account activation workflow
- **Role-Based Access**: Granular permissions with USER/ADMIN role hierarchy
- **WebSocket Security**: Custom authentication interceptors for real-time communication

### Data Protection & Validation
- **Input Sanitization**: Jakarta Bean Validation with custom constraint validators
- **SQL Injection Prevention**: JPA/Hibernate query parameterization
- **CSRF Protection**: Spring Security configuration with token-based validation
- **Secure Headers**: Security headers configuration for API protection

### Architecture Patterns
- **Command Pattern**: Encapsulated request handling with validation (LoginCommand, SendMessageCommand)
- **Repository Pattern**: Data access abstraction with Spring Data JPA and MongoDB
- **Event-Driven Architecture**: Application events for registration and password reset workflows
- **Clean Architecture**: Separation of concerns between controllers, services, and repositories

## 🧪 Testing Strategy

### Running Tests
```bash
# Unit tests only
mvn test

# Integration tests with TestContainers
mvn failsafe:integration-test

# All tests with coverage
mvn clean test integration-test
```

### Test Architecture
- **Unit Tests**: Service layer business logic and validation rules
- **Integration Tests**: Full application context with TestContainers (PostgreSQL)
- **Security Tests**: Authentication flows and authorization checks
- **WebSocket Tests**: Real-time communication and message handling
- **Controller Tests**: REST API endpoints with MockMvc

### Test Configuration
- **TestContainers**: Dockerized PostgreSQL for integration testing
- **MockMvc**: Spring MVC testing framework for controllers
- **Test Profiles**: Isolated test configuration with H2 fallback

## 📊 Monitoring & Error Handling

### Comprehensive Error Management
- **GlobalErrorHandler**: Centralized exception processing with HTTP status mapping
- **Custom Exceptions**: Domain-specific error types (UserNotFoundException, IdeaAccessDeniedException)
- **Validation Errors**: Jakarta Bean Validation with detailed field-level messaging
- **WebSocket Errors**: Real-time error handling with custom exception handlers

### Example Error Response
```json
{
  "status": "BAD_REQUEST",
  "message": "Email address is already registered",
  "timestamp": "2025-08-07T10:30:00Z",
  "path": "/api/registration"
}
```

### Application Monitoring
- **Structured Logging**: Configurable log levels with Hibernate SQL logging
- **Database Monitoring**: Connection pool and query performance tracking

## 🤝 Contributing

### Development Guidelines
- **Code Style**: Java conventions with Lombok for boilerplate reduction
- **Testing**: Comprehensive unit and integration test coverage
- **Documentation**: OpenAPI documentation for all public endpoints
- **Security**: Follow OWASP security guidelines and best practices

### Development Workflow
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Implement changes with comprehensive tests
4. Update documentation and API specs
5. Submit a pull request with detailed description

### Code Quality Standards
- **Clean Architecture**: Maintain separation between layers
- **Command Pattern**: Use command objects for request handling
- **Exception Handling**: Implement proper error handling and logging
- **Security First**: Validate all inputs and secure all endpoints
