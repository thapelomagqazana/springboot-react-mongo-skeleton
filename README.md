# SpringBoot-React-Mongo Skeleton

A minimal **Spring Boot + React + MongoDB** boilerplate with JWT authentication and user CRUD operations. This project is designed for building scalable, secure, and extendable modern web applications.

---

## 🚀 Tech Stack

### **Backend (Spring Boot 3 + MongoDB)**
- **Spring Boot 3** - Backend framework
- **Spring Security** - Authentication & Authorization
- **JWT (JSON Web Token)** - Secure API authentication
- **MongoDB** - NoSQL database
- **Spring Data MongoDB** - ORM for MongoDB
- **Lombok** - Boilerplate reduction
- **JUnit + Mockito** - Unit & Integration testing

### **Frontend (React + Vite + TypeScript)**
- **React** - UI Framework
- **Vite** - Fast build tool
- **TypeScript** - Type safety
- **Material UI** - Modern UI components
- **React Router** - Routing
- **React Hook Form + Yup** - Form validation
- **Axios** - API requests
- **JWT Authentication**

---

## 📂 Project Structure

```
springboot-react-mongo-skeleton/
│── backend/             # Spring Boot Backend  
│   ├── src/main/java/com/example/backend
│   │   ├── config/       # CORS, Security config
│   │   ├── controller/   # API Controllers
│   │   ├── model/        # Data models (User)
│   │   ├── repository/   # MongoDB Repository
│   │   ├── service/      # Business logic
│   │   ├── security/     # JWT Auth & Security
│   ├── application.properties # Configuration
│   ├── pom.xml          # Maven Dependencies
│   ├── Dockerfile       # Containerization
│
│── frontend/            # React Frontend
│   ├── src/  
│   │   ├── components/   # UI Components
│   │   ├── pages/        # App pages (Login, Signup, Profile)
│   │   ├── context/      # Auth Context
│   │   ├── utils/        # API & Auth utils
│   ├── package.json      # Dependencies
│   ├── vite.config.ts    # Vite configuration
│   ├── tailwind.config.js # TailwindCSS
│   ├── Dockerfile       # Containerization
│
│── docker-compose.yml   # Containerized setup
│── README.md           # Project Documentation
```

---

## 🖥 Backend Setup (Spring Boot)

### **1️⃣ Clone the Repository**
```sh
git clone https://github.com/thapelomagqazana/springboot-react-mongo-skeleton.git
cd springboot-react-mongo-skeleton/backend
```

### **2️⃣ Set Up Environment Variables**
Create a `.env` file in `backend/`:
```env
SERVER_PORT=8080
DB_TYPE=mongo
MONGO_URI=mongodb://localhost:27017/skeleton_db
DB_NAME=skeleton_db
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION_MS=86400000
FRONTEND_ORIGIN=http://localhost:3000
```

### **3️⃣ Install Dependencies & Run Backend**
```sh
mvn clean install
mvn spring-boot:run
```
The backend will start on **`http://localhost:8080`**

---

## 🌐 Frontend Setup (React + Vite + TypeScript)

### **1️⃣ Navigate to the Frontend Directory**
```sh
cd frontend
```

### **2️⃣ Set Up Environment Variables**
Create a `.env` file in `frontend/`:
```env
VITE_API_URL=http://localhost:8080
VITE_PORT=3000
```

### **3️⃣ Install Dependencies & Run Frontend**
```sh
npm install
npm run dev
```
The frontend will start on **`http://localhost:3000`**

---

## 🔧 API Endpoints

### **Authentication**
| Operation  | API Route | Method |
|------------|----------|--------|
| Sign-up   | `/auth/signup` | `POST` |
| Sign-in   | `/auth/signin` | `POST` |
| Sign-out  | `/auth/signout` | `POST` |

### **User CRUD**
| Operation  | API Route | Method |
|------------|----------|--------|
| Get Users  | `/api/users` | `GET` |
| Get User by ID  | `/api/users/{id}` | `GET` |
| Update User  | `/api/users/{id}` | `PUT` |
| Delete User  | `/api/users/{id}` | `DELETE` |

---

## 🛠 Docker Setup

### **1️⃣ Build & Run Containers**
```sh
docker-compose up --build
```
- **Backend** → `http://localhost:8080`
- **Frontend** → `http://localhost:3000`
- **MongoDB** → `localhost:27017`

---

## ✅ Next Steps

🚀 Enhancements & Features to Add:
- 🔹 Role-Based Access Control (RBAC)
- 🔹 Email Verification
- 🔹 Deploy to Cloud (AWS, Render, Heroku)
- 🔹 Unit & Integration Testing

---

## 💡 Contributing
Pull requests are welcome! If you'd like to contribute, please open an issue.

---

## 📜 License
This project is open-source under the MIT License.

🚀 **Happy Coding!** 🎉