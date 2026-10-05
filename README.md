# Internship Tracker

A full-stack internship application tracker built with **Java, Spring Boot, Thymeleaf, Spring Security, JPA, MySQL, Docker, and Railway**.

The app gives students a private workspace to track internship applications from initial application through interview, offer, or rejection, while preserving stage history, follow-up reminders, and pipeline analytics.

**Live demo:** https://internship-tracker-production-6156.up.railway.app

---

## Features

- Secure signup and login with **Spring Security**
- Password hashing with **BCrypt**
- Per-user data isolation: users can only access their own applications
- Create, view, edit, and delete internship applications
- Search applications by company or role
- Filter by application stage
- Server-side pagination
- Form validation with user-friendly error messages
- Stage tracking:
  - Applied
  - Interview
  - Offer
  - Rejected
- Automatic stage-change history
- Timeline showing application progression
- Scheduled follow-up detection after 7 days of inactivity
- Dashboard with:
  - total applications
  - stage counts
  - offer count
  - follow-up count
  - interview rate
- Custom 403, 404, and 500 pages
- JUnit 5 and Mockito service-layer tests
- Dockerized production build
- Railway deployment with MySQL
- Spring Boot Actuator health endpoint

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Spring Security
- Bean Validation
- Spring Scheduling
- Spring Boot Actuator

### Frontend
- Thymeleaf
- HTML
- CSS

### Database
- MySQL

### Testing
- JUnit 5
- Mockito
- AssertJ
- Spring Security Test

### Deployment
- Docker
- Railway
- Railway MySQL

---

## Application Flow

1. A student creates an account.
2. The password is hashed with BCrypt before storage.
3. After login, the user sees a private dashboard.
4. The user can add internship applications and update them as the process progresses.
5. Every stage transition is written to the stage-history table.
6. A scheduled job checks for inactive applications and flags eligible applications for follow-up.
7. Dashboard statistics are calculated only from the currently authenticated user's applications.

---

## Security Design

Security is one of the main parts of the project.

Authentication is handled by Spring Security using email and password credentials. Passwords are never stored in plaintext; signup uses BCrypt hashing.

Application ownership is enforced in the service/repository layer. Instead of fetching an application by ID and checking ownership afterward, protected operations query using both:

```text
application ID + authenticated user ID
```

For example:

```java
findByIdAndOwnerId(applicationId, ownerId)
```

This pattern is used for sensitive read, edit, and delete operations so one user cannot retrieve another user's application by changing an ID in the URL.

For privacy, a request for another user's application behaves like a missing resource rather than revealing that the record exists.

---

## Data Model

### User

Stores:
- name
- email
- BCrypt-hashed password
- creation timestamp

A user owns many internship applications.

### InternshipApplication

Stores:
- company
- role
- location
- job URL
- applied date
- current stage
- notes
- follow-up flag
- creation timestamp
- last-updated timestamp
- owner

### StageHistory

Stores:
- previous stage
- new stage
- change timestamp
- application reference

This provides an audit trail for each application's progression.

---

## Stage History

When an application is created, an initial history event is recorded.

When the stage changes, for example:

```text
APPLIED -> INTERVIEW -> OFFER
```

a new history entry is created.

Editing unrelated fields such as notes or location does **not** create a fake stage-history entry.

---

## Follow-Up Scheduler

The application includes a scheduled Spring job that checks for applications that have not been updated for a configurable period.

Default:

```text
7 days
```

Applications in active stages such as `APPLIED` and `INTERVIEW` can be marked as needing a follow-up.

Terminal stages such as `OFFER` and `REJECTED` are excluded.

The settings can be changed through environment variables:

```text
FOLLOW_UP_DAYS
FOLLOW_UP_CRON
```

---

## Dashboard Metrics

The dashboard is scoped to the authenticated user.

It shows:
- total applications
- applied count
- interview count
- offer count
- rejected count
- applications needing follow-up
- interview rate

For this project, interview rate is defined as:

```text
(INTERVIEW + OFFER) / TOTAL APPLICATIONS * 100
```

Offers are included because reaching the offer stage implies the application progressed through an interview process.

---

## Search, Filtering, and Pagination

The application list supports:

- search by company name
- search by role
- stage filtering
- combined search + stage filtering
- pagination
- sorting by most recently updated

All filtering remains owner-scoped, so search or pagination cannot cross the authenticated user's data boundary.

---

## Testing

The project contains JUnit 5 and Mockito tests for important service-layer behavior, including:

- correct owner can read an application
- another user cannot read an application
- another user cannot update an application
- another user cannot delete an application
- unauthorized updates never reach the repository save operation
- duplicate signup rejection
- password encoding behavior
- initial stage-history creation
- stage-change history creation
- avoiding fake history entries for normal edits
- follow-up flagging
- dashboard calculations

The production Docker image currently packages with `-DskipTests`, so tests are compiled during the image build but are not executed as part of the deployment build.

To run the full test suite locally:

```bash
mvn test
```

---

## Run Locally with Docker

### Prerequisites

- Docker
- Docker Compose

Clone the repository:

```bash
git clone https://github.com/zzchatgpp/internship-tracker.git
cd internship-tracker
```

Start the application and MySQL:

```bash
docker compose up --build
```

Open:

```text
http://localhost:8080
```

Docker Compose starts both:
- the Spring Boot application
- MySQL

---

## Run Locally without Docker

Requirements:
- Java 21
- Maven
- MySQL

Create a MySQL database or allow the configured URL to create it automatically.

Set the following environment variables if your credentials differ from the defaults:

```bash
DB_URL=jdbc:mysql://localhost:3306/internship_tracker
DB_USERNAME=root
DB_PASSWORD=your_password
```

Run:

```bash
mvn spring-boot:run
```

---

## Environment Variables

| Variable | Purpose | Default |
|---|---|---|
| `PORT` | HTTP port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | local default / `prod` in deployment |
| `DB_URL` | JDBC connection URL | local MySQL URL |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` locally |
| `FOLLOW_UP_DAYS` | Inactivity threshold | `7` |
| `FOLLOW_UP_CRON` | Scheduler cron expression | hourly |

Do not commit real production credentials.

---

## Production Deployment

The live version is deployed on **Railway**.

The production architecture is:

```text
Browser
   |
   v
Railway HTTPS Domain
   |
   v
Spring Boot Docker Container
   |
   v
Railway Private Network
   |
   v
MySQL
```

The Spring Boot service connects to MySQL using Railway environment-variable references rather than hard-coded credentials.

Health checks use:

```text
/actuator/health
```

Live application:

https://internship-tracker-production-6156.up.railway.app

---

## Project Structure

```text
src/
├── main/
│   ├── java/com/naharpurawala/internshiptracker/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   └── resources/
│       ├── static/css/
│       ├── templates/
│       ├── application.properties
│       └── application-prod.properties
└── test/
    └── java/com/naharpurawala/internshiptracker/service/
```

---

## Key Engineering Decisions

### Ownership-aware repository lookups
Authorization is enforced close to the data-access boundary using application ID plus owner ID.

### Separate stage-history table
The current stage stays on the application for efficient filtering while history is stored separately for timeline/audit purposes.

### Persisted follow-up flag
The scheduler updates a persisted flag instead of recalculating every reminder on every page render.

### Environment-based configuration
Database credentials and production settings are supplied at runtime rather than committed to source control.

### Multi-stage Docker build
Maven compiles the application in a build image, while the final container contains only the Java runtime and packaged application.

---

## Future Improvements

Potential next improvements include:

- Flyway or Liquibase database migrations
- email follow-up reminders
- richer charts
- CI workflow that runs tests on every push
- application document/attachment support
- interview scheduling
- export to CSV
- role-based administrative views

---

## Live Demo

**Internship Tracker:**  
https://internship-tracker-production-6156.up.railway.app

Create an account to test the private application-tracking workflow.
