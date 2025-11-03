# App Skeleton

This repository contains a simple full-stack skeleton using **Spring Boot** and **React with TypeScript**.

## 📂 Project Structure
```

repo-root/
│
├── backend/     → Spring Boot application
│   └── src/...  → contains a simple Hello World API
│
└── frontend/    → React + TypeScript app created with Vite
└── src/...  → displays a simple Hello World page

````

## 🚀 Backend (Spring Boot)
Run the backend server:

```bash
cd backend
./mvnw spring-boot:run
````

Then open [http://localhost:8080/api/hello](http://localhost:8080/api/hello)
You should see:

```
Hello from Spring Boot!
```

## 💻 Frontend (React + TypeScript)

Run the frontend app:

```bash
cd frontend
npm install
npm run dev
```

Then open [http://localhost:5173](http://localhost:5173)
You should see a “Hello from React + TypeScript!” message.

## 🧩 Summary

* **Backend:** Java + Spring Boot
* **Frontend:** React + TypeScript (Vite)
* Both projects run independently for now.
* This is only a base skeleton for starting new features.
