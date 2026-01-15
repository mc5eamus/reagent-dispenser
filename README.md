# Lab Reagent Dispenser Application

A comprehensive web application for managing laboratory reagent dispensing operations with real-time monitoring and 96-well plate management.

## Architecture Overview

This application uses a **Spring Boot 2.5.5** backend with REST and WebSocket APIs, and an **Angular 14.2** TypeScript frontend for real-time monitoring and control.

### Technology Stack

**Backend:**
- Spring Boot 2.5.5 (Java 17)
- Maven 3.9.11
- JPA/Hibernate with H2 in-memory database
- WebSocket (STOMP over SockJS)
- Lombok for boilerplate reduction
- JUnit 5 + Mockito for testing

**Frontend:**
- Angular 14.2
- TypeScript 4.7
- RxJS 7.5 for reactive programming
- @stomp/stompjs for WebSocket communication
- Jasmine + Karma for testing

## Domain Model

### Core Entities

- **Plate**: Represents a physical laboratory plate container (default: 96-well, 8×12 format)
  - Unique barcode identification
  - Configurable rows and columns
  - Associated wells

- **Well**: Individual well position within a plate
  - Standard notation (A1-H12 for 96-well plates)
  - Volume tracking (current and maximum capacity in μL)
  - Unique position per plate

- **Reagent**: Chemical reagent available for dispensing
  - Name, description, concentration
  - Stock volume management
  - Unit tracking (default: μL)

- **DispenseOperation**: Records a dispense operation
  - Links plate, well, and reagent
  - Status lifecycle: PENDING → IN_PROGRESS → COMPLETED/FAILED
  - Volume dispensed tracking
  - Timestamp and error tracking

## Project Structure

```
ReagentDispenser/
├── backend/                          # Spring Boot application
│   ├── src/main/java/
│   │   └── com/lab/reagentdispenser/
│   │       ├── ReagentDispenserApplication.java
│   │       ├── config/               # WebSocket, CORS configuration
│   │       ├── entity/               # JPA entities
│   │       ├── repository/           # Spring Data JPA repositories
│   │       ├── service/              # Business logic
│   │       ├── controller/           # REST & WebSocket controllers
│   │       └── dto/                  # Data Transfer Objects
│   ├── src/main/resources/
│   │   ├── application.properties    # Application configuration
│   │   ├── schema.sql                # Database schema
│   │   └── data.sql                  # Seed data
│   └── pom.xml
│
├── frontend/                         # Angular application
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   └── services/         # HTTP & WebSocket services
│   │   │   ├── shared/
│   │   │   │   └── models/           # TypeScript interfaces
│   │   │   ├── app.component.*       # Root component
│   │   │   ├── app.module.ts         # Root module
│   │   │   └── app-routing.module.ts # Routing configuration
│   │   ├── environments/             # Environment configs
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.css
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
│
└── .github/
    └── copilot-instructions.md       # AI-assisted development guidelines
```

## Setup Instructions

### Prerequisites

- **Java 17** (LTS version recommended)
- **Maven 3.6+** (Maven 3.9.11 recommended)
- **Node.js 16.x or 18.x** (with npm)
- **Chrome** (for running frontend tests)

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Install dependencies and build:**
   ```bash
   mvn clean install
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on **http://localhost:8080**

5. **Access H2 Console (optional):**
   - URL: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:reagentdb`
   - Username: `sa`
   - Password: (leave empty)

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Run tests:**
   ```bash
   npm test -- --watch=false --browsers=ChromeHeadless
   ```

4. **Start development server:**
   ```bash
   npm start
   ```

   The frontend will start on **http://localhost:4200**

5. **Build for production:**
   ```bash
   npm run build
   ```

   Production files will be in `dist/reagent-dispenser-frontend/`

## API Documentation

### REST API Endpoints

**Base URL:** `http://localhost:8080/api`

#### Plates
- `GET /plates` - List all plates
- `GET /plates/{id}` - Get plate by ID
- `GET /plates/barcode/{barcode}` - Get plate by barcode
- `GET /plates/{id}/wells` - Get wells for a plate
- `POST /plates` - Create new plate
- `PUT /plates/{id}` - Update plate
- `DELETE /plates/{id}` - Delete plate

#### Reagents
- `GET /reagents` - List all reagents
- `GET /reagents/{id}` - Get reagent by ID
- `POST /reagents` - Create new reagent
- `PUT /reagents/{id}` - Update reagent
- `DELETE /reagents/{id}` - Delete reagent

#### Dispense Operations
- `GET /dispense/history` - Get operation history
- `GET /dispense/{id}` - Get operation details
- `GET /dispense/status/{status}` - Get operations by status
- `POST /dispense` - Create dispense operation (not executed immediately)
- `POST /dispense/{id}/execute` - Execute existing operation

#### Batch Dispense Operations
- `POST /dispense/batch` - Create new batch for a plate
- `GET /dispense/batch` - Get all batches
- `GET /dispense/batch/{id}` - Get batch details with operations
- `POST /dispense/batch/{id}/add-operation` - Add operation to batch
- `POST /dispense/batch/{id}/execute` - Execute all operations in batch sequentially

### WebSocket API

**Connection:** `ws://localhost:8080/ws` (SockJS endpoint)

**Subscribe to topics:**
- `/topic/dispense-status` - All dispense operation updates

**Message format:**
```json
{
  "type": "OPERATION_STATUS_CHANGE",
  "payload": {
    "operationId": 123,
    "status": "COMPLETED",
    "plateId": 1,
    "wellPosition": "A5",
    "volumeDispensed": 50.0
  },
  "timestamp": "2026-01-15T10:30:00"
}
```

**Message types:**
- `OPERATION_CREATED` - New operation created
- `OPERATION_STATUS_CHANGE` - Status changed (PENDING → IN_PROGRESS → COMPLETED/FAILED)
- `BATCH_EXECUTION_STARTED` - Batch execution started
- `BATCH_EXECUTION_COMPLETED` - Batch execution completed
- `PLATE_UPDATED` - Plate information updated
- `WELL_VOLUME_CHANGED` - Well volume changed

## Sample Data

The application comes pre-loaded with sample data:

**Plates:**
- PLATE-001, PLATE-002, PLATE-003 (96-well plates with 8×12 grids)
- PLATE-001 has all 96 wells pre-created (A1-H12)

**Reagents:**
- DMSO (10% stock, 1000 μL)
- PBS Buffer (1X, 5000 μL)
- Trypsin-EDTA (0.25%, 2000 μL)
- FBS (100%, 3000 μL)
- Penicillin-Streptomycin (100X, 1500 μL)

**Operations:**
- 5 sample dispense operations with various statuses

## Batch Operations Workflow

The application supports planning and executing dispense operations as a batch:

1. **Create a batch** for a specific plate:
   ```bash
   POST /api/dispense/batch
   {"plateBarcode": "PLATE-001"}
   ```

2. **Add operations** to the batch (one or more):
   ```bash
   POST /api/dispense/batch/{batchId}/add-operation
   {
     "wellPosition": "A1",
     "reagentId": 1,
     "volume": 50.0
   }
   ```

3. **Execute the batch** - operations run sequentially with 0.5s delay between each:
   ```bash
   POST /api/dispense/batch/{batchId}/execute
   ```

4. **Monitor progress** via WebSocket messages (`/topic/dispense-status`):
   - `BATCH_EXECUTION_STARTED` - Batch begins executing
   - `OPERATION_STATUS_CHANGE` - Each operation updates in real-time
   - `BATCH_EXECUTION_COMPLETED` - All operations completed

**Benefits:**
- Plan multiple dispense operations before execution
- Sequential execution ensures controlled dispensing
- Real-time monitoring via WebSocket
- Track batch status (PLANNED → EXECUTING → COMPLETED/FAILED)
- FBS (100%, 3000 μL)
- Penicillin-Streptomycin (100X, 1500 μL)

**Operations:**
- 5 sample dispense operations with various statuses

## Testing

### Backend Tests

Run all tests:
```bash
cd backend
mvn test
```

Test coverage:
- Repository layer: `@DataJpaTest`
- Service layer: `@ExtendWith(MockitoExtension.class)`
- Controller layer: `@WebMvcTest`

### Frontend Tests

Run all tests:
```bash
cd frontend
npm test
```

Run tests in headless mode:
```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

Run with coverage:
```bash
npm test -- --code-coverage
```

## Development Workflow

1. **Backend Development:**
   - Create/modify entities in `entity/` package
   - Add repository methods in `repository/` package
   - Implement business logic in `service/` package
   - Create REST endpoints in `controller/` package
   - Add DTOs in `dto/` package

2. **Frontend Development:**
   - Define models in `shared/models/`
   - Create services in `core/services/`
   - Build components in feature modules
   - Update routing in `app-routing.module.ts`

3. **Real-time Features:**
   - Backend: Use `SimpMessagingTemplate` to send messages
   - Frontend: Subscribe to WebSocket topics via `WebSocketService`

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# H2 Database
spring.datasource.url=jdbc:h2:mem:reagentdb
spring.h2.console.enabled=true

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
```

### Frontend Configuration

Edit `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  wsUrl: 'http://localhost:8080/ws'
};
```

## Known Limitations

- **No Authentication:** This version does not implement user authentication or authorization
- **No Hardware Integration:** The application does not connect to physical dispenser hardware
- **In-Memory Database:** Data is lost when the application restarts (H2 in-memory mode)
- **Version EOL:** Spring Boot 2.5.5 and Angular 14.2 are past their end-of-life dates

## Future Enhancements

- Add user authentication and role-based access control
- Implement hardware integration for physical dispensers
- Add persistent database (PostgreSQL, MySQL)
- Create feature modules for plates, reagents, and dispense operations
- Add data visualization (charts, graphs)
- Implement audit logging
- Support for different plate formats (384-well, custom)
- Add batch operation templates for common workflows

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**H2 Console not accessible:**
- Verify `spring.h2.console.enabled=true` in application.properties
- Access at http://localhost:8080/h2-console

### Frontend Issues

**npm install fails:**
```bash
# Clear npm cache
npm cache clean --force
npm install
```

**CORS errors:**
- Verify backend is running on port 8080
- Check `@CrossOrigin` annotation in controllers

**WebSocket not connecting:**
- Ensure backend WebSocket endpoint is configured
- Check browser console for connection errors
- Verify SockJS fallback is working

## Contributing

This application is intended as a demonstration project. For production use, consider:
- Upgrading to Spring Boot 3.x and Angular 17+
- Adding comprehensive integration tests
- Implementing CI/CD pipeline
- Adding API documentation (Swagger/OpenAPI)
- Implementing security best practices

## License

This project is for educational and demonstration purposes.

## Support

For issues or questions, refer to:
- Spring Boot Documentation: https://docs.spring.io/spring-boot/docs/2.5.x/reference/html/
- Angular Documentation: https://v14.angular.io/docs
- Project guidelines: `.github/copilot-instructions.md`

---

**Version:** 0.1.0  
**Last Updated:** January 15, 2026  
**Spring Boot:** 2.5.5 | **Angular:** 14.2
