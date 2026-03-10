# 🛠️ Task Connect API

Task Connect is a RESTful API backend for a two-sided marketplace platform where users can post everyday tasks or find tasks to earn money. The system allows users to act dynamically as both **Requesters** (posting tasks) and **Taskers** (bidding on tasks).

## 💡 Project Logic & Business Rules

* **Role Dualism:** A single user can both post tasks and bid on other people's tasks.
* **Bidding Engine:** Taskers can submit financial offers (bids) for open tasks. 
* **Self-Bidding Protection:** Built-in validation prevents users from submitting bids on their own published tasks.
* **Task Assignment Workflow:** When a Requester accepts a bid:
  * The selected Bid status changes to `ACCEPTED`.
  * The Task status changes from `OPEN` to `ASSIGNED`.
  * All other pending bids for that task are automatically marked as `REJECTED`.
* **Traceability:** Every task is strictly linked to a physical Address and a specific Category.

## 🚀 Technologies Used & Where They Are Applied

### Core Backend
* **Java 21**
* **Spring Boot**
* **Spring Web**: Used to build the REST API endpoints (Controllers) and handle HTTP requests/responses.

### Data & Persistence
* **Spring Data JPA & Hibernate**: Used in the Repository and Entity layers to map Java objects to relational database tables and perform CRUD operations without writing raw SQL.
* **Oracle Database**: The main relational database used to ensure data persistence and transactional integrity.

### Validation & Utilities
* **Spring Boot Validation**: Used in DTOs (`@NotBlank`, `@Email`) to ensure incoming JSON requests meet data integrity rules before reaching the business logic.
* **Jackson (JSON)**: Handled serialization/deserialization, utilizing `@JsonIgnoreProperties` to prevent infinite recursion when mapping bidirectional JPA relationships (e.g., Task <-> Bid).

### Testing
* **JUnit 5 & Mockito**: Used for writing comprehensive Unit Tests for the Service layer, mocking repository interactions.
* **Spring MockMvc (`@WebMvcTest`)**: Used for slicing Controller tests to verify HTTP status codes (200, 201, 400, 404), JSON payloads, and global exception handling without loading the full application context.

### Documentation
* **Swagger (Springdoc OpenAPI)**: Automatically generates interactive API documentation and a UI for manual endpoint testing.

## 🏗️ Architecture
* **Controller-Service-Repository**: Strict separation of concerns. Controllers handle HTTP, Services handle business logic, and Repositories handle database operations.
* **DTO (Data Transfer Object)**: Used `TaskRequestDTO` and `BidRequestDTO` to decouple the database Entities from the API output, improving security and preventing infinite recursion.
* **Global Exception Handling (`@ControllerAdvice`)**: Centralized error handling that intercepts business logic exceptions (e.g., `IllegalArgumentException`, `ResourceNotFoundException`) and translates them into consistent, user-friendly JSON error responses (HTTP 400, 404, 409).

## 🔌 Core API Endpoints

### User Management
* `POST /api/users` - Register a new user.
* `GET /api/users/{id}` - Retrieve user profile and ratings.
* `POST /api/users/{userId}/addresses` - Add a new address to a user profile.

### Task Management
* `POST /api/tasks` - Post a new task (requires category and address links).
* `PATCH /api/tasks/{taskId}/accept-bid/{bidId}` - Accept a specific bid, assign the task, and reject competing bids.

### Bidding System
* `POST /api/tasks/{taskId}/bids` - Allow a Tasker to submit a bid for an open task.
