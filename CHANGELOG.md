# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-08-07
### Added
- JWT-based stateless authentication with AWS SES email verification.
- User profile management with password change and recovery workflows.
- Idea management with full CRUD, status lifecycle, categorization, and RSQL-based filtering.
- Real-time chat using WebSocket (STOMP), message persistence in MongoDB, and status tracking.
- User presence tracking with session management.
- Apache Kafka integration for scalable message distribution.
- PostgreSQL for core relational data and Redis for caching/session management.
- OpenAPI/Swagger documentation endpoints.
- Test strategy with unit, integration, and TestContainers-backed database tests.
- Docker Compose setup for local infrastructure services.