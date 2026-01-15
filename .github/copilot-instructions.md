# Copilot Instructions: Lab Reagent Dispenser Application

## Project Overview

This application provides an interface for managing lab reagent dispensing operations with a focus on 96-well plate (8×12 format) management. The system uses a Spring Boot 2.5.5 backend with REST and WebSocket APIs, and an Angular 14.2 TypeScript frontend for real-time monitoring and control.

**Key Features:**
- Manage laboratory plates with unique barcode identification
- Track wells within plates using standard notation (A1-H12 for 96-well plates)
- Manage reagent inventory and properties
- Execute and monitor dispense operations in real-time
- Track operation status lifecycle (PENDING → IN_PROGRESS → COMPLETED/FAILED)
- Volume measurements in microliters (μL)

**Architecture:**
- **Backend**: Spring Boot 2.5.5 (Java 17) with Maven, JPA/Hibernate, H2 Database
- **Frontend**: Angular 14.2 with TypeScript 4.7, RxJS 7.5
- **Communication**: RESTful API for CRUD operations, WebSocket (STOMP over SockJS) for real-time updates
- **Testing**: JUnit 5 + Mockito (backend), Jasmine + Karma (frontend)

**No Authentication**: This version does not implement user authentication or authorization.

**No Hardware Integration**: The application does not connect to physical dispenser hardware; it's a simulation/interface layer.

---

## Domain Model

### Entity Relationships

```
Plate (1) ──< (N) Well
Reagent (1) ──< (N) DispenseOperation
DispenseOperation (N) >── (1) Well
DispenseOperation (N) >── (1) Plate
```

### Plate Entity
- **Purpose**: Represents a physical laboratory plate container
- **Key Fields**:
  - `id`: Long (Primary Key, auto-generated)
  - `barcode`: String (Unique, NOT NULL, indexed) - Unique identifier for physical plates
  - `rows`: Integer (Default: 8) - Number of rows
  - `columns`: Integer (Default: 12) - Number of columns
  - `plateType`: Enum (96_WELL, 384_WELL, etc.)
  - `createdDate`: LocalDateTime (Timestamp)
  - `wells`: List<Well> (OneToMany, CascadeType.ALL, orphanRemoval = true)
- **Validation**: 
  - Barcode must be unique and not empty
  - Rows and columns must be positive integers

### Well Entity
- **Purpose**: Represents an individual well position within a plate
- **Key Fields**:
  - `id`: Long (Primary Key, auto-generated)
  - `position`: String (Format: "A1" to "H12" for 96-well plates) - Row letter + Column number
  - `plate`: Plate (ManyToOne)
  - `volume`: Double (Current volume in μL, nullable)
  - `maxVolume`: Double (Maximum capacity in μL)
- **Validation**: 
  - Position must match pattern: `^[A-H][1-9]|1[0-2]$` for 96-well plates
  - Position must be unique within a plate
- **Constraints**: Add `@Column` unique constraint on (plate_id, position)

### Reagent Entity
- **Purpose**: Represents a chemical reagent available for dispensing
- **Key Fields**:
  - `id`: Long (Primary Key, auto-generated)
  - `name`: String (NOT NULL)
  - `description`: String (Optional)
  - `concentration`: String (e.g., "10 mM", "1 mg/mL")
  - `stockVolume`: Double (Available volume in μL)
  - `unit`: String (Default: "μL")
- **Validation**: Name must not be empty

### DispenseOperation Entity
- **Purpose**: Records a dispense operation from a reagent to a specific well
- **Key Fields**:
  - `id`: Long (Primary Key, auto-generated)
  - `plate`: Plate (ManyToOne)
  - `well`: Well (ManyToOne)
  - `reagent`: Reagent (ManyToOne)
  - `volumeDispensed`: Double (Volume in μL)
  - `status`: Enum (PENDING, IN_PROGRESS, COMPLETED, FAILED)
  - `createdDate`: LocalDateTime
  - `completedDate`: LocalDateTime (Nullable)
  - `errorMessage`: String (Nullable, populated if status = FAILED)
- **Status Lifecycle**:
  - `PENDING`: Operation created, awaiting execution
  - `IN_PROGRESS`: Operation currently executing
  - `COMPLETED`: Operation successfully finished
  - `FAILED`: Operation failed (check errorMessage)

---

## Backend Development Standards (Spring Boot 2.5.5)

### Project Structure

```
src/main/java/com/lab/reagentdispenser/
├── ReagentDispenserApplication.java  (Main @SpringBootApplication)
├── config/
│   ├── WebSocketConfig.java          (@EnableWebSocketMessageBroker)
│   └── CorsConfig.java                (CORS configuration)
├── entity/
│   ├── Plate.java                     (@Entity, @Data, @Builder)
│   ├── Well.java
│   ├── Reagent.java
│   └── DispenseOperation.java
├── repository/
│   ├── PlateRepository.java           (extends JpaRepository)
│   ├── WellRepository.java
│   ├── ReagentRepository.java
│   └── DispenseOperationRepository.java
├── service/
│   ├── PlateService.java              (@Service, @Transactional)
│   ├── ReagentService.java
│   └── DispenseService.java
├── controller/
│   ├── PlateController.java           (@RestController, REST endpoints)
│   ├── ReagentController.java
│   ├── DispenseController.java
│   └── DispenseWebSocketController.java (@Controller, @MessageMapping)
└── dto/
    ├── PlateDTO.java
    ├── WellDTO.java
    ├── ReagentDTO.java
    ├── DispenseOperationDTO.java
    ├── DispenseRequestDTO.java
    └── WebSocketMessage.java
```

### Lombok Usage
- Use `@Data` for entities (includes @Getter, @Setter, @ToString, @EqualsAndHashCode)
- Use `@Builder` for fluent object construction
- Use `@NoArgsConstructor` and `@AllArgsConstructor` when needed for JPA
- Use `@Slf4j` for logging in services and controllers

### JPA and Repository Layer
- Repositories extend `JpaRepository<Entity, Long>`
- Use derived query methods: `findByBarcode`, `findByPlate`, `findByStatus`
- Add custom queries with `@Query` annotation when needed
- Always specify cascade types explicitly: `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`
- Use `fetch = FetchType.LAZY` by default, `EAGER` only when necessary

### Service Layer
- Annotate with `@Service`
- Use `@Transactional` on methods that modify data
- Implement business logic and validation
- Convert between entities and DTOs (consider MapStruct or manual mapping)
- Use constructor injection for dependencies (Lombok `@RequiredArgsConstructor`)

### REST Controller Standards
- Annotate with `@RestController`
- Use `@RequestMapping("/api")` as base path
- HTTP Methods:
  - `@GetMapping` for retrieving resources
  - `@PostMapping` for creating resources
  - `@PutMapping` for updating resources
  - `@DeleteMapping` for deleting resources
- Use `@Valid` with request bodies for validation
- Return `ResponseEntity<T>` with appropriate HTTP status codes
- Use DTOs instead of exposing entities directly

### WebSocket Configuration
- **Configuration Class**: `WebSocketConfig implements WebSocketMessageBrokerConfigurer`
  - Use `@EnableWebSocketMessageBroker`
  - Register STOMP endpoint: `registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:4200").withSockJS()`
  - Enable simple broker: `config.enableSimpleBroker("/topic")`
  - Set application destination prefix: `config.setApplicationDestinationPrefixes("/app")`

- **WebSocket Controllers**: Use `@Controller` (not @RestController)
  - `@MessageMapping("/dispense")` for receiving messages from clients
  - `@SendTo("/topic/dispense-status")` for broadcasting to subscribers
  - Use `SimpMessagingTemplate` to send messages programmatically

- **Message Format**: Standardized JSON envelope
  ```json
  {
    "type": "DISPENSE_UPDATE",
    "payload": { ... },
    "timestamp": "2026-01-15T10:30:00"
  }
  ```

### Exception Handling
- Use `@RestControllerAdvice` for global exception handling
- Return consistent error response DTOs
- Map exceptions to appropriate HTTP status codes:
  - 400 Bad Request: Validation errors
  - 404 Not Found: Resource not found
  - 409 Conflict: Duplicate resources (e.g., barcode)
  - 500 Internal Server Error: Unexpected errors

### Database Configuration (H2)
- **application.properties**:
  ```properties
  spring.datasource.url=jdbc:h2:mem:reagentdb
  spring.datasource.driverClassName=org.h2.Driver
  spring.datasource.username=sa
  spring.datasource.password=
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  spring.h2.console.enabled=true
  spring.h2.console.path=/h2-console
  spring.jpa.hibernate.ddl-auto=none
  spring.sql.init.mode=always
  ```
- Use `schema.sql` for DDL (CREATE TABLE statements)
- Use `data.sql` for initial data seeding (INSERT statements)

---

## Frontend Development Standards (Angular 14.2)

### Project Structure

```
src/app/
├── app.module.ts                    (Root module)
├── app.component.ts/html/css        (Root component)
├── app-routing.module.ts            (Routing configuration)
├── core/
│   ├── services/
│   │   ├── websocket.service.ts     (WebSocket connection manager)
│   │   ├── api.service.ts           (HTTP base service)
│   │   └── error-handler.service.ts
│   └── interceptors/
│       └── http-error.interceptor.ts
├── shared/
│   ├── models/
│   │   ├── plate.model.ts
│   │   ├── well.model.ts
│   │   ├── reagent.model.ts
│   │   ├── dispense-operation.model.ts
│   │   └── websocket-message.model.ts
│   ├── components/                  (Reusable UI components)
│   └── shared.module.ts
├── features/
│   ├── plates/
│   │   ├── plates.module.ts
│   │   ├── components/
│   │   │   ├── plate-list/
│   │   │   ├── plate-detail/
│   │   │   └── plate-form/
│   │   └── services/
│   │       └── plate.service.ts
│   ├── reagents/
│   │   ├── reagents.module.ts
│   │   ├── components/
│   │   └── services/
│   └── dispense/
│       ├── dispense.module.ts
│       ├── components/
│       │   ├── dispense-control/
│       │   └── dispense-monitor/
│       └── services/
└── environments/
    ├── environment.ts               (apiUrl, wsUrl)
    └── environment.prod.ts
```

### TypeScript Standards
- Enable strict mode in `tsconfig.json`: `"strict": true`
- Use explicit types for all variables, parameters, and return values
- Prefer `interface` over `type` for object shapes
- Use enums for fixed sets of values (e.g., `OperationStatus`, `PlateType`)
- Avoid `any`; use `unknown` when type is genuinely unknown

### Component Standards
- Use `OnPush` change detection strategy where possible
- Implement `OnInit` for initialization logic
- Implement `OnDestroy` and unsubscribe from observables
- Keep templates simple; move complex logic to component class or services
- Use `async` pipe in templates to automatically handle subscriptions

### Service Standards
- Annotate with `@Injectable({ providedIn: 'root' })`
- Use HttpClient for REST API calls
- Return Observables from service methods
- Use RxJS operators: `map`, `catchError`, `retry`, `shareReplay`, `distinctUntilChanged`
- Implement error handling with `catchError`

### WebSocket Service Pattern
```typescript
@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private stompClient: Client;
  private connectionState$ = new BehaviorSubject<boolean>(false);
  
  connect(): void {
    // Initialize @stomp/stompjs Client with SockJS
    // Handle connection lifecycle
  }
  
  subscribe<T>(destination: string): Observable<T> {
    // Return Observable wrapping STOMP subscription
  }
  
  send(destination: string, body: any): void {
    // Send message to server
  }
  
  disconnect(): void {
    // Clean up connection
  }
}
```

### State Management
- For this application scope, use services with `BehaviorSubject` for state
- No NgRx/Akita needed
- Example: `PlateService` maintains `BehaviorSubject<Plate[]>` for plate list
- Components subscribe to these state streams

### HTTP API Service Pattern
```typescript
@Injectable({ providedIn: 'root' })
export class PlateService {
  private apiUrl = environment.apiUrl + '/api/plates';
  
  constructor(private http: HttpClient) {}
  
  getAll(): Observable<Plate[]> {
    return this.http.get<Plate[]>(this.apiUrl);
  }
  
  getById(id: number): Observable<Plate> {
    return this.http.get<Plate>(`${this.apiUrl}/${id}`);
  }
  
  create(plate: Plate): Observable<Plate> {
    return this.http.post<Plate>(this.apiUrl, plate);
  }
  
  // ... update, delete methods
}
```

### Routing
- Lazy load feature modules: `loadChildren: () => import('./features/plates/plates.module').then(m => m.PlatesModule)`
- Define routes with clear paths: `/plates`, `/reagents`, `/dispense`
- Use route guards if authentication is added later

### UI Component Library
- Recommend Angular Material or PrimeNG for consistent UI components
- Use reactive forms (`ReactiveFormsModule`) over template-driven forms
- Implement form validation with Validators

---

## Testing Guidelines

### Backend Testing (JUnit 5 + Mockito)

**Repository Tests**: Use `@DataJpaTest`
```java
@DataJpaTest
class PlateRepositoryTest {
    @Autowired
    private PlateRepository plateRepository;
    
    @Test
    void shouldFindPlateByBarcode() {
        // Arrange
        Plate plate = Plate.builder().barcode("TEST001").rows(8).columns(12).build();
        plateRepository.save(plate);
        
        // Act
        Optional<Plate> found = plateRepository.findByBarcode("TEST001");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBarcode()).isEqualTo("TEST001");
    }
}
```

**Service Tests**: Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`
```java
@ExtendWith(MockitoExtension.class)
class PlateServiceTest {
    @Mock
    private PlateRepository plateRepository;
    
    @InjectMocks
    private PlateService plateService;
    
    @Test
    void shouldCreatePlateWithWells() {
        // Arrange, Act, Assert pattern
    }
}
```

**Controller Tests**: Use `@WebMvcTest`
```java
@WebMvcTest(PlateController.class)
class PlateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PlateService plateService;
    
    @Test
    void shouldReturnAllPlates() throws Exception {
        // Use mockMvc.perform(get("/api/plates"))
        //   .andExpect(status().isOk())
        //   .andExpect(jsonPath("$", hasSize(2)));
    }
}
```

**Test Data Builders**: Create builder methods for test objects
- Use Lombok `@Builder` pattern
- Create reusable test fixtures

### Frontend Testing (Jasmine + Karma)

**Component Tests**: Use TestBed
```typescript
describe('PlateListComponent', () => {
  let component: PlateListComponent;
  let fixture: ComponentFixture<PlateListComponent>;
  let plateService: jasmine.SpyObj<PlateService>;
  
  beforeEach(async () => {
    const plateServiceSpy = jasmine.createSpyObj('PlateService', ['getAll']);
    
    await TestBed.configureTestingModule({
      declarations: [ PlateListComponent ],
      providers: [
        { provide: PlateService, useValue: plateServiceSpy }
      ]
    }).compileComponents();
    
    plateService = TestBed.inject(PlateService) as jasmine.SpyObj<PlateService>;
  });
  
  it('should display plates', () => {
    // Test implementation
  });
});
```

**Service Tests**: Mock HttpClient with HttpClientTestingModule
```typescript
describe('PlateService', () => {
  let service: PlateService;
  let httpMock: HttpTestingController;
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [ PlateService ]
    });
    
    service = TestBed.inject(PlateService);
    httpMock = TestBed.inject(HttpTestingController);
  });
  
  it('should retrieve all plates', () => {
    // Test with httpMock.expectOne()
  });
});
```

**WebSocket Service Tests**: Mock STOMP client
- Create mock Client from @stomp/stompjs
- Test connection lifecycle
- Test message publishing and subscription

**Async Testing**: Use `fakeAsync` and `tick` for time-based operations

---

## API Conventions

### REST Endpoints (Base: `/api`)

#### Plates
- `GET /api/plates` - List all plates
- `GET /api/plates/{id}` - Get plate by ID
- `GET /api/plates/{id}/wells` - Get wells for a specific plate
- `POST /api/plates` - Create new plate
- `PUT /api/plates/{id}` - Update plate
- `DELETE /api/plates/{id}` - Delete plate

#### Reagents
- `GET /api/reagents` - List all reagents
- `GET /api/reagents/{id}` - Get reagent by ID
- `POST /api/reagents` - Create new reagent
- `PUT /api/reagents/{id}` - Update reagent
- `DELETE /api/reagents/{id}` - Delete reagent

#### Dispense Operations
- `POST /api/dispense` - Execute dispense operation
- `GET /api/dispense/history` - Get operation history (with pagination)
- `GET /api/dispense/{id}` - Get operation details
- `GET /api/dispense/status/{operationId}` - Get current status

### WebSocket Endpoints

**Connection**: `ws://localhost:8080/ws` (SockJS endpoint)

**Topics (Subscribe)**:
- `/topic/dispense-status` - Broadcast all dispense operation updates
- `/topic/plate/{plateId}` - Updates for a specific plate
- `/topic/operation/{operationId}` - Updates for a specific operation

**Destinations (Publish)**:
- `/app/dispense` - Client sends dispense requests

**Message Format**:
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

**Message Types**:
- `OPERATION_CREATED` - New operation created
- `OPERATION_STATUS_CHANGE` - Status changed (PENDING → IN_PROGRESS → COMPLETED/FAILED)
- `PLATE_UPDATED` - Plate information updated
- `WELL_VOLUME_CHANGED` - Well volume changed

---

## Naming Conventions

### Java (Backend)
- **Packages**: lowercase, singular (e.g., `entity`, `service`, not `entities`, `services`)
- **Classes**: PascalCase (e.g., `PlateService`, `DispenseOperation`)
- **Interfaces**: PascalCase, often descriptive names (e.g., `PlateRepository`)
- **Methods**: camelCase, verb-based (e.g., `findByBarcode`, `createPlate`)
- **Variables**: camelCase (e.g., `plateRepository`, `operationId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_PLATE_ROWS`)
- **DTOs**: Suffix with `DTO` (e.g., `PlateDTO`)

### TypeScript (Frontend)
- **Files**: kebab-case (e.g., `plate-list.component.ts`, `websocket.service.ts`)
- **Classes**: PascalCase (e.g., `PlateListComponent`, `WebSocketService`)
- **Interfaces**: PascalCase, often without "I" prefix (e.g., `Plate`, `WebSocketMessage`)
- **Methods**: camelCase (e.g., `getPlates`, `connectWebSocket`)
- **Variables**: camelCase (e.g., `plateService`, `operationId`)
- **Constants**: UPPER_SNAKE_CASE or camelCase (e.g., `API_URL` or `apiUrl`)
- **Observables**: Suffix with `$` (e.g., `plates$`, `connectionState$`)

### Database
- **Tables**: snake_case, plural (e.g., `plates`, `dispense_operations`)
- **Columns**: snake_case (e.g., `created_date`, `volume_dispensed`)
- **Foreign Keys**: `{table}_id` (e.g., `plate_id`, `reagent_id`)

---

## Common Patterns and Best Practices

### Error Handling
- Backend: Use `@RestControllerAdvice` to catch exceptions globally
- Frontend: Use HttpInterceptor to handle HTTP errors centrally
- Log errors with appropriate context
- Return user-friendly error messages

### Logging
- Backend: Use SLF4J with Lombok `@Slf4j`
  - `log.info()` for normal operations
  - `log.warn()` for recoverable issues
  - `log.error()` for exceptions
- Frontend: Use console with appropriate levels
  - Production: Minimize console output

### Validation
- Backend: Use Bean Validation (JSR-380) annotations
  - `@NotNull`, `@NotEmpty`, `@Size`, `@Pattern`, `@Min`, `@Max`
  - Validate DTOs with `@Valid` in controllers
- Frontend: Use Angular Validators in reactive forms
  - `Validators.required`, `Validators.pattern`, custom validators

### Performance Considerations
- Backend: Use pagination for large datasets
- Backend: Optimize N+1 queries with `@EntityGraph` or fetch joins
- Frontend: Use `trackBy` in `*ngFor` loops
- Frontend: Implement virtual scrolling for large lists
- WebSocket: Throttle or debounce rapid updates if needed

### Code Style
- Backend: Follow Google Java Style Guide or Spring conventions
- Frontend: Follow Angular Style Guide
- Use linters: Checkstyle/SpotBugs (backend), ESLint (frontend)
- Format code consistently with IDE formatters

---

## Deployment and Build

### Backend Build
```bash
mvn clean install        # Build and run tests
mvn spring-boot:run      # Run application (port 8080)
mvn test                 # Run tests only
```

### Frontend Build
```bash
npm install              # Install dependencies
ng serve                 # Development server (port 4200)
ng build                 # Production build
ng test                  # Run Karma tests
ng lint                  # Run linter
```

### Environment Configuration
- Backend: Use `application.properties` or `application-{profile}.properties`
- Frontend: Use `environment.ts` and `environment.prod.ts`
- Externalize configuration for different environments (dev, test, prod)

---

## Version Compatibility Notes

**Spring Boot 2.5.5**: Released September 2021, reached EOL August 2023
- No security updates available
- Consider migration to Spring Boot 3.x for production use
- Java 17 supported (LTS version recommended)

**Angular 14.2**: Released August 2022, reached EOL November 2023
- No security updates available
- Consider migration to Angular 17+ for production use
- Node 14/16/18 compatible (Node 16 LTS recommended)

**Dependencies**:
- Ensure compatible versions of @stomp/stompjs (6.1.2), sockjs-client (1.6.1)
- RxJS 7.5.x is stable and widely used

---

## Additional Resources

- Spring Boot Documentation: https://docs.spring.io/spring-boot/docs/2.5.x/reference/html/
- Spring WebSocket Guide: https://spring.io/guides/gs/messaging-stomp-websocket/
- Angular Documentation: https://v14.angular.io/docs
- STOMP Protocol: https://stomp.github.io/
- Lab Automation Standards: SLAS (Society for Laboratory Automation and Screening)

---

## Quick Start Checklist

- [ ] Backend: Update pom.xml with all dependencies
- [ ] Backend: Create entity classes with proper JPA annotations
- [ ] Backend: Create repository interfaces extending JpaRepository
- [ ] Backend: Implement service layer with business logic
- [ ] Backend: Create REST controllers with proper mappings
- [ ] Backend: Configure WebSocket with STOMP endpoints
- [ ] Backend: Write schema.sql and data.sql for H2 initialization
- [ ] Backend: Write unit tests for services and controllers
- [ ] Frontend: Initialize Angular project with dependencies
- [ ] Frontend: Create model interfaces matching backend DTOs
- [ ] Frontend: Implement WebSocket service wrapping @stomp/stompjs
- [ ] Frontend: Create feature modules (plates, reagents, dispense)
- [ ] Frontend: Implement components with proper data binding
- [ ] Frontend: Write unit tests with TestBed and mocks
- [ ] Test REST API endpoints with Postman or curl
- [ ] Test WebSocket connections with browser DevTools
- [ ] Verify end-to-end workflow: create plate → dispense → monitor status

---

*This document should be updated as the project evolves and new patterns emerge.*
