# IdeaMatch

## Project Overview
IdeaMatch is a comprehensive platform built with Java 21 and Spring Boot that connects innovators with skilled makers. The platform combines user authentication, profile management, and real-time chat functionality to create a collaborative environment.

## Key Features

### User Management System
- **Secure Registration & Authentication**: JWT-based authentication with email verification
- **Profile Management**: Complete user profile system with customizable information
- **Password Security**: Advanced password validation with reset functionality via email
- **Role-Based Access Control**: Flexible user roles and permissions system
- **Account Verification**: Email-based account activation and verification

### Real-Time Chat System
- **Private Messaging**: Direct 1:1 conversations between users
- **Group Chat**: Multi-user chat rooms with administrative controls
- **Message Status Tracking**: Read receipts, delivery confirmations, and message history
- **File Attachments**: Support for file sharing with thumbnails
- **WebSocket Integration**: Real-time message delivery using WebSocket connections
- **Message Persistence**: Chat history stored in MongoDB for reliable message retrieval

### Advanced Features
- **Real-time Notifications**: Instant notifications for messages and system events
- **Message Search**: Full-text search capabilities across chat history
- **User Presence**: Online/offline status tracking
- **Moderation Tools**: Admin controls for managing chat rooms and users

## Tech Stack

### Backend Framework
- **Java 21**: Modern Java features and performance improvements
- **Spring Boot 3.4.5**: Latest Spring Boot with enhanced features
- **Spring Security**: Comprehensive security framework
- **Spring Data JPA**: Database abstraction layer
- **Spring Data MongoDB**: NoSQL document database integration
- **Spring WebSocket**: Real-time communication support

### Messaging & Communication
- **Apache Kafka**: High-performance message streaming
- **WebSocket**: Real-time bidirectional communication
- **STOMP Protocol**: Simple Text Oriented Messaging Protocol

### Database Systems
- **PostgreSQL**: Primary relational database for user data
- **MongoDB**: Document database for chat messages and rooms
- **Redis**: In-memory caching and session management

### Security & Authentication
- **JWT (JSON Web Tokens)**: Stateless authentication
- **Spring Security**: Security configuration and filters
- **Password Validation**: Custom password strength validation
- **Email Verification**: Secure account activation process

### Cloud Services
- **AWS SES**: Email service for notifications and verification
- **Docker**: Containerization for development and deployment

### Development Tools
- **Lombok**: Reduce boilerplate code
- **MapStruct**: Type-safe bean mapping
- **Swagger/OpenAPI**: API documentation
- **Maven**: Build automation and dependency management

## Installation and Running

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL database
- MongoDB database
- Redis server

### Environment Setup
1. Copy environment configuration:
   ```bash
   cp .env.example .env
   ```

2. Update environment variables in `.env`:
   ```env
   POSTGRES_PASSWORD=your_postgres_password
   JWT_SECRET=your_jwt_secret_key
   AWS_ACCESS_KEY_ID=your_aws_access_key
   AWS_SECRET_ACCESS_KEY=your_aws_secret_key
   ```

### Database Setup
Start the required services using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database (port 5432)
- MongoDB database (port 27017)  
- Redis server (port 6379)
- Apache Kafka (port 9094)

### Build and Run
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd idea-match
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port 8080 by default.

## API Endpoints

### User Registration & Authentication
- `POST /api/registration` - Register a new user account
- `GET /api/verify-email?token={token}` - Verify email address
- `POST /api/login` - User authentication
- `POST /api/auth/forgot-password` - Request password reset
- `GET /api/auth/reset-password?token={token}` - Validate reset token
- `POST /api/auth/reset-password` - Reset user password

### User Profile Management
- `GET /api/user/profile` - Get user profile information
- `PUT /api/user/profile` - Update user profile
- `POST /api/user/change-password` - Change user password

### Chat System (WebSocket)
- `CONNECT /ws` - Establish WebSocket connection
- `SEND /app/chat.send-message` - Send public message
- `SEND /app/chat.private-message` - Send private message
- `SEND /app/chat.join-room` - Join chat room
- `SEND /app/chat.add-user` - Add user to chat

### WebSocket Subscriptions
- `SUBSCRIBE /topic/public` - Public chat messages
- `SUBSCRIBE /topic/room/{roomId}` - Room-specific messages
- `SUBSCRIBE /user/queue/messages` - Private messages

## Database Schema

### PostgreSQL Tables
- **idea_match_user**: User accounts and profile information
- **user_roles**: Role assignments and permissions

### MongoDB Collections
- **chat_rooms**: Chat room information and participant management
- **chat_messages**: Message history with full metadata
- **message_attachments**: File attachments and media

## Security Features

### Authentication
- JWT-based stateless authentication
- Token blacklisting for secure logout
- Password strength validation using Passay library
- Email verification for account activation

### Authorization
- Role-based access control
- WebSocket authentication interceptor
- API endpoint protection
- Resource-level security

### Data Protection
- Password hashing with BCrypt
- Secure token generation
- Input validation and sanitization
- SQL injection prevention

## Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn failsafe:integration-test
```

### Test Coverage
The project includes comprehensive testing:
- **Unit Tests**: Service layer and business logic testing
- **Integration Tests**: Full application context testing
- **Controller Tests**: REST API endpoint testing
- **Security Tests**: Authentication and authorization testing
- **WebSocket Tests**: Real-time communication testing

## Development Profiles

### Available Profiles
- **dev** (default): Development environment with debug logging
- **test**: Testing environment with H2 in-memory database
- **prod**: Production environment with optimized settings

### Profile Configuration
Activate specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Error Handling

The application provides comprehensive error handling:
- **Global Exception Handler**: Centralized error processing
- **Custom Exceptions**: Domain-specific error types
- **Validation Errors**: Detailed field-level error messages
- **Security Errors**: Authentication and authorization failures

Example error response:
```json
{
  "status": "BAD_REQUEST",
  "message": "Email address is already registered",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Monitoring and Logging

### Application Logging
- **Structured Logging**: JSON format for production
- **Log Levels**: Configurable logging levels per component
- **Database Logging**: SQL query logging for development
- **WebSocket Logging**: Connection and message tracking

### Health Monitoring
- **Spring Boot Actuator**: Health checks and metrics
- **Database Health**: Connection pool and query performance
- **Kafka Health**: Message broker connectivity
- **Redis Health**: Cache availability

## Contributing

### Code Style
- Follow Java coding conventions
- Use Lombok for boilerplate reduction
- Implement comprehensive testing
- Document public APIs

### Pull Request Process
1. Fork the repository
2. Create feature branch
3. Implement changes with tests
4. Update documentation
5. Submit pull request
