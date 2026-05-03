# Internship Assignment Optimization System

This project is a full-stack web application developed in the ASPD course (University of Passau) to support internship planning for teacher education.

It replaces manual assignment workflows with a two-phase optimization process that assigns:

- teachers to internship slots (Phase 1)
- students to active internship slots (Phase 2)

The system supports semester reoptimization with baseline preservation and provides CRUD management, validation, and audit visibility through a web UI.

## Project Context

- Domain: Teaching internship planning (GS/MS)
- Client context: University internship office workflow
- Scale described in report: hundreds of placements across many schools
- Optimization style: hard constraints for feasibility, soft constraints for solution quality

See detailed documentation in [report/main.tex](report/main.tex).

## Repository Structure

```text
implementation/
|- backend/      Spring Boot API + OptaPlanner optimization engine
|- frontend/     React + TypeScript (Vite) UI
|- report/       LaTeX final report and references
|- docker-compose.yml  Local MySQL + phpMyAdmin
`- LICENSE       Proprietary read-only license
```

## Technology Stack

### Backend

- Java 17
- Spring Boot 3.5.x
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- OptaPlanner 9.44 (two-solver setup)
- Maven
- MySQL (runtime), H2 (tests)
- Apache POI (Excel import/export)

### Frontend

- React 19 + TypeScript
- Vite
- React Router
- Vitest

### Dev Infrastructure

- Docker Compose (MySQL + phpMyAdmin)
- GitLab CI pipeline for test/build

## Key Functionalities

- Master data management: students, teachers, schools, courses, internship configuration
- Two-phase optimization:
    - Phase 1: slot activation + teacher assignment
    - Phase 2: student assignment to active slots
- Reoptimization workflow across semesters with assignment preservation
- Async optimization jobs with polling/job status
- Validation services for manual changes
- JWT authentication and role/permission-based authorization

## Optimization Workflow

1. Data preparation (demand generation, validations, geocoding support)
2. Phase 1 optimize teacher/slot planning
3. Phase 2 optimize student placement
4. Optional reoptimization using prior semester baseline

Primary optimization endpoints include:

- `POST /api/internships/phase1/optimize-async`
- `POST /api/internships/phase2/optimize-async`
- `POST /api/reoptimization/optimize-async`
- `GET /api/jobs/{jobId}` for async progress polling

Authentication endpoint:

- `POST /auth/login`

## Local Development Setup

## Prerequisites

- Java 17+
- Node.js 20+
- npm
- Docker + Docker Compose

## 1) Start Database

From repository root:

```bash
docker-compose up -d
```

Default local services:

- MySQL: `localhost:3307`
- phpMyAdmin: `http://localhost:8081`

Configured DB credentials (development):

- database: `aspd_db`
- user: `aspd_user`
- password: `aspd_team3`

## 2) Start Backend

Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Backend base URL: `http://localhost:8080`

Default seeded admin user (created by bootstrap):

- email: `admin@school.com`
- password: `admin123`

## 3) Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend default URL: `http://localhost:5173`

The frontend uses `VITE_API_URL` if defined, otherwise defaults to `http://localhost:8080`.

## Testing and Build

### Backend

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm test
npm run build
```

## Report

Project analysis, requirements, architecture, implementation details, and development process are documented in the report:

- [report/main.tex](report/main.tex)
- [report/README.md](report/README.md)

## License

This repository is distributed under a proprietary read-only license.

Read full terms in [LICENSE](LICENSE).
