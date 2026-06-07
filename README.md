# 🚀 Institute Resource Planner (IRP)

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-4.0.3-success)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![License](https://img.shields.io/badge/License-MIT-green)

---

**A comprehensive Institute Resource Planner (IRP) system designed to streamline campus asset management, real-time inventory tracking, and automated communication.**

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Running the App](#running-the-app)
- [Sample Data](#sample-data)
- [Frontend](#frontend)
- [Booking Flow](#booking-flow)
- [Screenshots](#screenshots)

---

## ✨ Key Features <a name="features"></a>

### 🔐 User & Security Management
* **Role-Based Access Control (RBAC):** Distinct dashboards for Admin and Users with secure session management.
* **Authentication:** Secure login and registration portal.

### 📦 Inventory & Resource Control
* **Real-Time Monitoring:** Dynamic status tracking of assets (Available, Allocated, or Under Maintenance).
* **Resource Booking System:** Seamless interface for users to reserve and book institutional resources.
* **Return Management:** Streamlined process for returning allocated resources back to the inventory.
* **Fine & Penalty System:** Automated calculation and tracking of fines for late resource returns.
* **Centralized Management:** Intuitive interface to add, update, or remove assets.

### 🔔 Automated Communication
* **Email Integration:** Automated confirmations and return reminders via `JavaMail API`.
* **SMS Alerts:** Integrated `Twilio API` for real-time booking updates, return reminders, and fine notifications.

### 📄 Analytics & Reporting
* **PDF Documentation:** Automated generation of inventory reports, allocation history, and fine receipts using `OpenPDF`.
* **Inventory Audits:** Comprehensive data tracking for institutional assets.mprehensive data tracking for institutional assets.
---

## 🛠️ Tech Stack <a name="tech-stack"></a>

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 25 | Backend Development |
| **Spring Boot** | 4.0.3 | Web Framework |
| **Hibernate/JPA** | 7.2.4 | ORM/Database Layer |
| **MySQL** | 8.0.41 | Database |
| **Thymeleaf** | 3.1.3 | Server-side Rendering |
| **JavaMail API** | 2.1.5 | Automated Email Notifications |
| **Twilio SDK** | 10.1.0 | SMS Notification System |
---

##  📁 Project Structure  <a name="project-structure"></a>


```text
InstituteResourcePlanner/
├── src/
│   └── main/
│       ├── java/com/example/irp/
│       │   ├── InstituteResourcePlannerApplication.java          # Main entry point
│       │   |
│       │   |
│       │   ├── Controller/
│       │   │   ├── AdminController.java
│       │   │   ├── AdminVerificationController.java
│       │   │   ├── AuthController.java
│       │   │   ├── FineSchedulerController.java
│       │   │   ├── ResourceController.java
│       │   │   ├── SearchController.java
|       |   |   ├── UserController.java
│       │   │   └── UserVerificationController.java
│       │   |
│       │   |
│       │   ├── Entity/
│       │   │   ├── Allocation.java
│       │   │   ├── Resource.java
│       │   |   └── User.java
│       │   |
│       │   |
│       │   ├── Repository/
│       │   │   ├── AllocationRepository.java
│       │   │   ├── ResourceRepository.java
│       │   │   ├── SearchRepository.java
│       │   │   └── UserRepository.java
│       │   |
│       │   └── Service/
│       │       ├── AllocationSchedulerService/
│       │       ├── EmailService/
│       │       ├── PdfReportService/
│       │       └── SmsService/
│       │
│       └── resources/
|           ├── application.properties/
|           ├── application-dev.properties/
│           └── application-prod.properties/
│
└── pom.xml
```
----
## 🗄️  Database Schema <a name="database-schema"></a>

```text
User (user_id, name, email, password, role)
  └──< Allocation (id, start_time, end_time, fine_amount, status, user_id, resource_id)
        └──< Resource (resource_id, resource_name, type, location, quantity, status)

```
---
Table Details

| Table | Primary Key | Foreign Key | Description |
| :--- | :--- | :--- | :--- |
| **Users** | user_id | - | Stores user credentials, roles, and profile info. |
| **Resources** | resource_id | - | Manages inventory details (name, type, status). |
| **Allocation** | id | user_id, resource_id | Links users to resources for booking/returning. |

---

## Entity Relationships

| Relationship          | Type         |
|-----------------------|--------------|
| User → Allocation     | One-to-Many  |
| Resource → Allocation | One-to-Many  |
| User ↔ Resource       | Many-to-Many |


---

## 🌐 API Reference <a name="api-reference"></a>

All API endpoints use the `application/json` format for request and response payloads.

---

### 🔐 Authentication

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Registers a new user account. |
| **POST** | `/api/auth/login` | Authenticates a user and returns a JWT/Session token. |

---

### 📦 Resource Management (Admin)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/resources` | Retrieves a list of all available resources. |
| **POST** | `/api/resources/add` | Adds a new resource to the inventory. |
| **PUT** | `/api/resources/update/{id}` | Updates resource details such as name, type, or status. |
| **DELETE** | `/api/resources/delete/{id}` | Removes a resource from the inventory. |

---

### 📅 Booking & Allocation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/allocation/book` | Creates a new resource booking request. |
| **GET** | `/api/allocation/history/{userId}` | Retrieves booking history for a specific user. |
| **PUT** | `/api/allocation/return/{id}` | Marks a resource as returned and updates allocation status. |

---

### 🔔 Notifications & Analytics

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/api/reports/fine/{userId}` | Retrieves pending fine details for a user. |
| **GET** | `/api/reports/download/{id}` | Downloads an allocation receipt in PDF format. |
| **POST** | `/api/notifications/send` | Triggers manual SMS or Email notifications. |

---

## 💡 Example API Response

### Request

```http
GET /api/resources
```

### Response

```json
[
  {
    "resourceId": 1,
    "resourceName": "Projector-A",
    "status": "AVAILABLE",
    "type": "Electronics"
  },
  {
    "resourceId": 2,
    "resourceName": "Lab-Table-05",
    "status": "ALLOCATED",
    "type": "Furniture"
  }
]
```

## 🚀 Getting Started <a name="getting-started"></a>

### Prerequisites

Make sure you have the following installed:

- Java 21+ (Project uses Java 25)
- Maven 3.x
- MySQL 8.0+
- Git



### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/Shevendra77/Institute-Resource-Planner.git

```



#### 2. Create MySQL Database

```sql
CREATE DATABASE institute;
```



#### 3. Configure `application.properties`

```properties
# src/main/resources/application-dev.properties

# ===============================
# SERVER CONFIG
# ===============================
server.port=8080

# ===============================
# DATABASE CONFIG
# ===============================
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/institute}
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASS:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===============================
# JPA / HIBERNATE CONFIG
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# ===============================
# THYMELEAF CONFIG
# ===============================
spring.thymeleaf.cache=false
spring.jpa.open-in-view=false
# ===============================
# EMAIL CONFIG
# ===============================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USER:shevendrachandel@gmail.com}
spring.mail.password=${EMAIL_PASS}

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ===============================
# SESSION & TWILIO CONFIG
# ===============================
server.servlet.session.cookie.name=IRP_SESSION

twilio.account.sid=${TWILIO_SID}
twilio.auth.token=${TWILIO_TOKEN}
twilio.phone.number=${TWILIO_PHONE:+12546553613}


```


#### 4. Build the Project

```bash
mvn clean install
```

#### 5. Run the Application

```bash
mvn spring-boot:run
```


### Server

✅ Application will start at:

```
http://localhost:8080/
```

---

## ▶️ Running the App <a name="running-the-app"></a>


---

###  Using Maven

```bash
mvn spring-boot:run
```


## Using JAR File

#### Build the JAR

```bash
mvn clean package
```

#### Run the JAR

```bash
java -jar target/InstituteResourcePlanner 0.0.1-SNAPSHOT
```


## Using IntelliJ IDEA

1. Open the project in IntelliJ IDEA  
2. Navigate to `InstituteResourcePlannerApplication.java`  
3. Click **Run ▶️**


## Verify Application is Running

```bash
curl http://localhost:8080/home
```
---

## 🌱 Sample Data

Import the SQL file to load real institutional inventory and user data:

```bash
mysql -u root -p IRP < irp_setup_final.sql
```

### 📊 Dataset Includes

- **5 Departments**
  - CS Lab
  - Mechanical Workshop
  - Seminar Hall
  - Library Hub
  - Main Admin Block

- **15 Resources**
  - Smart Classrooms
  - Projectors
  - High-End Laptops
  - Speakers
  - Lab Kits
  - And more

- **12 Accounts**
  - Pre-configured test accounts for:
    - Administrator
    - Staff Members
    - Students

- **4 Resource Statuses**
  - AVAILABLE
  - NOT_AVAILABLE
  - MAINTENANCE
  - APPROVED_PENDING_DELIVERY

- **50+ Allocations**
  - Real-time sample resource requests
  - Complete reservation logs

- **24 History Logs**
  - Approved and rejected booking records
  - User activity tracking

- **10 Mock Emails**
  - Ready-to-test email confirmation triggers
  - Database-integrated notification samples
 
---

## 🖥️ Frontend <a name="frontend"></a>


This backend is connected to a **plain HTML, CSS, and JavaScript frontend**.

---

### 📁 Project Structure

```text
bookmyshow-ui/
├── index.html                  # Main HTML page
├── css/
│   └── style.css               # Stylesheet
├── js/
│   └── script.js               # Frontend logic
├── assets/                     # Images, posters, icons
└── api/
    └── api.js                  # AJAX / fetch calls to backend APIs
```

---
### ▶️ Run Frontend

 Open `index.html` in your browser  

---

### 🌐 Access Application

```
http://localhost:3306
```

---


## 📊 Resource Allocation Flow

The following workflow illustrates how resources are requested, approved, and allocated within the Institute Resource Planner (IRP) system.

### 1️⃣ Select Resource Category

The user browses and selects a resource category.

⬇️

### 2️⃣ Check Resource Availability

```http
GET /resources/available
```

⬇️

### 3️⃣ Submit Allocation Request

The user submits a resource request by specifying the quantity, duration, and purpose.

```http
POST /user/request/submit
```

**Request Body**

```json
{
  "userId": 12,
  "resourceId": 3,
  "quantity": 1,
  "startTime": "2026-06-10T10:00:00",
  "reason": "Lab Session"
}
```

⬇️

### 4️⃣ Request Appears in Admin Control Center

The submitted request is displayed in the administrator dashboard for review.

⬇️

### 5️⃣ Admin Reviews and Approves Request

```http
POST /admin/request/approve?id={id}
```

The request status changes to:

```text
APPROVED_PENDING_DELIVERY
```

⬇️

### 6️⃣ Automated Email Notification

✅ Approval confirmation email is automatically sent to the user.

⬇️

### 7️⃣ User Tracks Request Status

Users can monitor the status of their requests through the dashboard.

```http
GET /user/dashboard
```

⬇️

### 8️⃣ Reject or Cancel Request (Optional)

If necessary, administrators can reject or cancel an allocation request.

```http
POST /admin/request/reject?id={id}
```

---

### 🔄 Workflow Summary

```text
User
  │
  ▼
Check Availability
  │
  ▼
Submit Request
  │
  ▼
Admin Review
  │
  ├── Approve ──► Email Notification ──► User Dashboard
  │
  └── Reject ──► Email Notification ──► User Dashboard
```
---

## 👨‍💻 Author

**Shevendra77 / Shevendra Singh**

- 🌐 GitHub: [https://github.com/Shevendra77/Institute-Resource-Planner](https://github.com/Shevendra77/Institute-Resource-Planner.git)
- 📧 Email: shevendrachandel@gmail.com
---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](./LICENSE) file for details.
--
## 🙏 Acknowledgements

- Inspired by [**Enterprise Resource Planning (ERP)**](https://github.com/Shevendra77/Institute-Resource-Planner) 
- Built as a **full-stack learning project** with Spring Boot  

- --


                                             ⭐ **Star this repo** if you found it helpful!  

                                                      Made with ❤️ **in India** 🇮🇳




