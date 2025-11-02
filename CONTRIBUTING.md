# Contributing Guidelines

Welcome to our project!
This document explains how we work together, contribute code, and maintain quality and consistency.
Please read it before starting new work or opening a Merge Request (MR).

## 🧭 Project Workflow Overview

We follow a feature-branch workflow with Merge Requests and peer reviews before merging into main.
No one commits directly to main.

Typical flow:

1. Pick or create an issue → assign yourself.

2. Create a branch for your work.

3. Commit changes using our conventions.

4. Open a Merge Request (MR) into main.

5. Request review from at least one teammate.

6. Merge once approved and checks (build/tests) pass.

## 🌿 Branch Naming Convention

Branches should clearly reflect the purpose of the work.

| Type | Prefix | Example |
|---|---|---|
| New feature |feature/ | feature/import-csv|
| Bug fix | bugfix/ | bugfix/invalid-login|
| Documentation | docs/ | /contributing-guide|
| Refactor / cleanup | refactor/ | refactor/api-layer|
| Experiment / prototype | experiment/  | experiment/theme-switch|

Format:

- `<prefix>/<short-kebab-case-description>`

Avoid including issue numbers in the branch name — GitLab automatically links MRs to issues.

## 🧩 Commit Messages

We use Conventional Commits to keep history readable and meaningful.

Format:

- `<type>(<scope>): <short description>`

### Examples

- `feat(import): add CSV parser`

- `fix(auth): handle expired tokens`

- `refactor(ui): simplify dashboard layout`

- `docs(readme): add setup instructions`

- `chore(build): update dependencies`

### Common types

- feat: new feature

- fix: bug fix

- refactor: code restructuring (no behavior change)

- docs: documentation updates

- chore: maintenance, config, build scripts, etc.

### Guidelines

- Use imperative mood: “add” not “added” or “adds”.

- Keep subject under 72 characters.

- Add extra details in the body if needed (after a blank line).

## 🧾 Merge Request (MR) Process

1. Title & Description

   - MR title should match your main commit or clearly describe the change.

   - Reference related issue(s): Closes #12 or Fixes #34.

2. Requirements

   - The branch must be up to date with main before merging.

   - All builds/tests must pass.

   - No leftover console.log, unused imports, or commented-out code.

3. Review Rules

   - At least one peer review required.

   - Reviewers should:

      - Check functionality and code clarity.

      - Ensure consistent naming and formatting.

      - Leave constructive comments (not personal criticism).

   - Merge only after approval and after resolving all discussions.

## 🧱 Code Style & Formatting

We maintain consistent style across the frontend and backend.

### Frontend (React)

- Follow ESLint and Prettier rules.

- Tabs = 4 spaces.

- Use functional components and hooks.

- Keep files small and components reusable.

- Use clear, descriptive names (e.g., UserDashboard, not UDash).

- Before committing:

  - `npm run lint`

  - `npm run format`

### Backend (Java / Spring Boot)

- Follow standard Google Java Style.

- Tabs = 4 spaces.

- Each class and method should have a short Javadoc comment.

- Use meaningful names and avoid large classes.

- Handle exceptions gracefully; log errors properly.

- Before committing:

  - `./mvnw clean verify`

### Database (MySQL)

- Use snake_case for table and column names.

- Always include an id primary key.

- Keep migrations/versioning files organized if applicable (e.g., Flyway or Liquibase).

## 🧾 Testing

- Write unit or integration tests for new features when possible.

- Don’t merge code that breaks existing functionality.

- Backend tests: JUnit

- Frontend tests: React Testing Library or Jest

## 📄 Documentation

Each major feature or module should include:

- A brief description (in code comments or Markdown file).

- API endpoints documented in /docs/api.md or Swagger.

- Update README.md when setup or environment variables change.

## 💬 Communication

- Discuss major design or workflow changes with the team before implementing.

- Assign yourself to issues to avoid overlap.

- Use comments in GitLab or team chat to clarify decisions.

- If blocked, ask early — collaboration is key.

## ✅ Summary

|Topic | Rule|
|---|---|
|Branch naming | feature/, bugfix/, docs/, etc.|
|Commit style | Conventional commits|
|MR approval | Minimum 1 reviewer|
|Direct commits to main | ❌ Never|
|Code formatting |ESLint + Prettier + Java formatter|
|Tabs |4 spaces|
|Documentation |Update when changing features|

## 🧩 Example Workflow

Create a new branch

- `git checkout -b feature/user-login`

Make changes & commit

- `git add .`

- `git commit -m "feat(auth): implement login with JWT"`

Push branch

- `git push origin feature/user-login`

Open MR on GitLab

- Add reviewers

- Reference issue: Closes #5
