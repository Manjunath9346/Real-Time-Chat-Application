# Real-Time Chat Application

A full-stack real-time communication application built with **Java, Spring Boot, MySQL, JavaScript, WebSocket, STOMP, SockJS, and WebRTC**.

The application provides private messaging, group conversations, file sharing, contact management, search, meeting scheduling, real-time meeting chat, participant management, and screen sharing.

---

## 📌 Project Overview

The **Real-Time Chat Application** is a web-based communication platform designed to provide users with real-time messaging and online meeting capabilities.

Users can:

- Register and log in
- Manage contacts
- Search for users
- Send private messages
- Participate in group conversations
- Share files
- Create and manage groups
- Schedule meetings
- Join meetings using meeting codes
- View meeting participants
- Communicate through real-time meeting chat
- Share their screen
- Receive real-time updates through WebSocket

The backend is developed using **Spring Boot**, the frontend uses **HTML, CSS, and JavaScript**, and **MySQL** is used for persistent data storage.

---

## ✨ Features

### 👤 User Authentication

- User registration
- User login
- Password encryption using BCrypt
- User profile information
- User management

### 💬 Private Messaging

Users can communicate directly with other users.

Features include:

- Real-time private messages
- Sender and receiver identification
- Message persistence
- Text messages
- File messages
- Real-time message delivery using WebSocket

### 👥 Group Chat

Users can communicate inside groups.

Features include:

- Create groups
- Group conversations
- Group members
- Real-time group messages
- Group-specific messaging channels

### 📎 File Sharing

Users can upload and share files through conversations.

Features include:

- File upload
- File storage
- File URL sharing
- Image and file messages
- File access and download

Runtime uploaded files are excluded from the Git repository.

### 🔎 Search

The application provides search functionality for finding users and relevant communication data.

Search API:

    /api/search/**

### 👥 Contacts

Users can manage their contacts and communicate with people from their contact list.

Contact API:

    /api/contacts/**

---

# 📅 Meeting System

The application includes an integrated meeting management system.

Users can create and schedule meetings with:

- Meeting title
- Date and time
- Description
- Duration
- Meeting creator
- Optional group association
- Unique meeting code
- Meeting status

### Meeting Status

Meetings can have states such as:

- `UPCOMING`
- `LIVE`
- `COMPLETED`

---

# 🎥 Meeting Room

Each meeting has a dedicated meeting room.

The meeting room provides:

- Meeting title
- Meeting code
- Meeting status
- Participant information
- Real-time participant updates
- Meeting chat
- Screen sharing
- WebRTC signaling
- Join and leave events

---

# 👥 Real-Time Participants

When users join a meeting, the server maintains the participants currently connected to that meeting.

The system broadcasts:

- Participant joined
- Participant left
- Participant information

The meeting creator is also represented in the participant information.

---

# 💬 Meeting Chat

Participants can communicate using the integrated meeting chat.

Messages are delivered in real time using WebSocket and STOMP.

### Message Flow

    Client
       │
       ▼
    /app/meeting.chat
       │
       ▼
    MeetingWebSocketController
       │
       ▼
    /topic/meeting/{meetingId}
       │
       ▼
    All Meeting Participants

---

# 🖥️ Screen Sharing

The meeting room supports browser-based screen sharing.

The application uses browser screen-capture capabilities together with WebRTC signaling.

Users can:

- Start screen sharing
- Stop screen sharing
- Notify other participants about screen-sharing status

---

# 🔌 Real-Time Communication

The application uses:

- WebSocket
- STOMP
- SockJS

for real-time communication.

### WebSocket Endpoint

    /ws

### Application Destination Prefix

    /app

### Topic Prefix

    /topic

---

# 📡 WebSocket Communication

## Private Chat

Client sends:

    /app/chat.private

Messages are delivered through:

    /topic/user/{userId}

---

## Group Chat

Client sends:

    /app/chat.group

Messages are delivered through:

    /topic/group/{groupId}

---

## Meeting Events

Meeting events are delivered through:

    /topic/meeting/{meetingId}

### Join Meeting

    /app/meeting.join

### Leave Meeting

    /app/meeting.leave

### WebRTC Signaling

    /app/meeting.signal

### Meeting Chat

    /app/meeting.chat

### Screen Sharing Events

    /app/meeting.screen

---

# 🏗️ Architecture

The application follows a layered Spring Boot architecture.

    ┌──────────────────────────┐
    │         Frontend         │
    │      HTML / CSS / JS     │
    └────────────┬─────────────┘
                 │
         REST API / WebSocket
                 │
                 ▼
    ┌──────────────────────────┐
    │       Controllers        │
    │                          │
    │ AuthController           │
    │ ChatController           │
    │ ContactController        │
    │ GroupController          │
    │ MeetingController        │
    │ MeetingWebSocketController│
    │ MessageController        │
    │ SearchController         │
    │ UserController           │
    └────────────┬─────────────┘
                 │
                 ▼
    ┌──────────────────────────┐
    │         Services         │
    │                          │
    │ UserService              │
    │ MessageService           │
    │ GroupService             │
    │ MeetingService            │
    └────────────┬─────────────┘
                 │
                 ▼
    ┌──────────────────────────┐
    │    Spring Data JPA       │
    │      Repositories        │
    └────────────┬─────────────┘
                 │
                 ▼
    ┌──────────────────────────┐
    │          MySQL           │
    └──────────────────────────┘

---

# 🛠️ Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring WebSocket
- STOMP
- Hibernate
- Maven

## Frontend

- HTML5
- CSS3
- JavaScript
- SockJS
- STOMP.js
- WebRTC

## Database

- MySQL

## Development Tools

- IntelliJ IDEA / Visual Studio Code
- Git
- GitHub
- Maven
- MySQL

---

# 📁 Project Structure

    Real-Time-Chat-Application/
    │
    ├── .mvn/
    │   └── wrapper/
    │
    ├── src/
    │   │
    │   ├── main/
    │   │   │
    │   │   ├── java/
    │   │   │   └── com/
    │   │   │       └── chatapp/
    │   │   │           └── chatapp/
    │   │   │
    │   │   │               ├── config/
    │   │   │               │   ├── SecurityConfig.java
    │   │   │               │   └── WebSocketConfig.java
    │   │   │               │
    │   │   │               ├── controller/
    │   │   │               │   ├── AuthController.java
    │   │   │               │   ├── ChatController.java
    │   │   │               │   ├── ContactController.java
    │   │   │               │   ├── FileController.java
    │   │   │               │   ├── GroupController.java
    │   │   │               │   ├── MeetingController.java
    │   │   │               │   ├── MeetingWebSocketController.java
    │   │   │               │   ├── MessageController.java
    │   │   │               │   ├── SearchController.java
    │   │   │               │   ├── SendMessageController.java
    │   │   │               │   ├── UploadedFileController.java
    │   │   │               │   └── UserController.java
    │   │   │               │
    │   │   │               ├── entity/
    │   │   │               │   ├── ChatGroup.java
    │   │   │               │   ├── Contact.java
    │   │   │               │   ├── Meeting.java
    │   │   │               │   ├── Message.java
    │   │   │               │   └── User.java
    │   │   │               │
    │   │   │               ├── repository/
    │   │   │               │   ├── ChatGroupRepository.java
    │   │   │               │   ├── ContactRepository.java
    │   │   │               │   ├── MeetingRepository.java
    │   │   │               │   ├── MessageRepository.java
    │   │   │               │   └── UserRepository.java
    │   │   │               │
    │   │   │               └── service/
    │   │   │                   ├── GroupService.java
    │   │   │                   ├── MeetingService.java
    │   │   │                   ├── MessageService.java
    │   │   │                   └── UserService.java
    │   │   │
    │   │   └── resources/
    │   │       │
    │   │       ├── static/
    │   │       │   ├── index.html
    │   │       │   ├── meeting-room.html
    │   │       │   ├── app.js
    │   │       │   └── style.css
    │   │       │
    │   │       └── application.properties
    │   │
    │   └── test/
    │
    ├── test-api/
    │   └── user-api.http
    │
    ├── .gitignore
    ├── .gitattributes
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    └── README.md

---

# 🗄️ Database

The application uses **MySQL** for persistent data storage.

## Main Entities

- User
- Contact
- ChatGroup
- Message
- Meeting

### Meeting Entity

The meeting system stores information including:

- `id`
- `title`
- `dateTime`
- `description`
- `createdBy`
- `groupId`
- `meetingCode`
- `status`
- `durationMinutes`
- `createdAt`

---

# 🔗 REST API

## Authentication

    /api/auth/**

## Users

    /api/users/**

## Contacts

    /api/contacts/**

## Groups

    /api/groups/**

## Messages

    /api/messages/**

## Files

    /api/files/**

## Search

    /api/search/**

## Meetings

    /api/meetings/**

### Create Meeting

    POST /api/meetings

### Get User Meetings

    GET /api/meetings/user/{userId}

### Get Group Meetings

    GET /api/meetings/group/{groupId}

### Get Meeting

    GET /api/meetings/{id}

### Find Meeting by Code

    GET /api/meetings/code/{code}

### Delete Meeting

    DELETE /api/meetings/{id}?requesterId={userId}

### Update Meeting Status

    POST /api/meetings/{id}/status

---

# ⚙️ Local Setup

## Prerequisites

Make sure the following are installed:

- Java 21
- MySQL
- Git
- Maven (optional because Maven Wrapper is included)

---

## 1. Clone the Repository

    git clone https://github.com/Manjunath9346/Real-Time-Chat-Application.git

    cd Real-Time-Chat-Application

---

## 2. Create MySQL Database

Open MySQL and create the database:

    CREATE DATABASE chat_app;

---

## 3. Configure Database

The application uses environment variables for database configuration.

Required environment variables:

    DB_URL
    DB_USERNAME
    DB_PASSWORD

Example:

    DB_URL=jdbc:mysql://localhost:3306/chat_app
    DB_USERNAME=root
    DB_PASSWORD=your_password

**Do not commit database credentials to GitHub.**

---

# ▶️ Run the Application

## Windows

    .\mvnw.cmd spring-boot:run

## Linux / macOS

    ./mvnw spring-boot:run

The application will be available at:

    http://localhost:8080

---

# 🧪 Build the Application

## Windows

    .\mvnw.cmd clean package

## Linux / macOS

    ./mvnw clean package

The generated JAR file will be available inside:

    target/

---

# 🔐 Security

The application uses Spring Security and BCrypt password encoding.

Security considerations:

- Passwords are encrypted using BCrypt.
- Database credentials are stored using environment variables.
- API keys should never be committed to GitHub.
- User-uploaded files are excluded from Git.
- Production deployments should use HTTPS.
- Production CORS configuration should be restricted to trusted domains.
- Production authentication should use stronger session/JWT security controls.

---

# 🌐 Deployment

The application can be deployed as a Spring Boot web service.

Required environment variables include:

    DB_URL
    DB_USERNAME
    DB_PASSWORD
    PORT

The frontend communicates with the backend using relative API and WebSocket paths.

WebSocket endpoint:

    /ws

This allows the same application to serve the frontend and backend.

---

# 📸 Screenshots

Screenshots can be added here to demonstrate the application's interface.

Recommended screenshots:

1. Login / Registration
2. Main Chat Interface
3. Private Chat
4. Group Chat
5. File Sharing
6. Meeting Scheduler
7. Meeting Room
8. Meeting Participants
9. Meeting Chat
10. Screen Sharing

Example:

    ![Chat Interface](screenshots/chat-interface.png)

---

# 🎯 Project Highlights

This project demonstrates practical experience with:

- Java backend development
- Spring Boot
- REST API development
- Spring Data JPA
- MySQL
- Spring Security
- WebSocket
- STOMP
- SockJS
- WebRTC
- Real-time communication
- Frontend development
- File handling
- User management
- Contact management
- Group management
- Meeting management
- Client-server architecture

---

# 🚀 Future Improvements

Potential future improvements include:

- JWT-based authentication
- Message read receipts
- Typing indicators
- Message reactions
- Message editing
- Message deletion
- Push notifications
- Voice calling
- Advanced video calling
- Persistent meeting chat history
- Cloud file storage
- Redis-based WebSocket scaling
- Production-grade WebRTC signaling
- Docker containerization
- CI/CD pipeline
- Automated testing
- Role-based meeting permissions
- Meeting recording
- Improved production security

---

# 👨‍💻 Author

## Manjunath Shankarapu

MCA | Software Development

GitHub:

https://github.com/Manjunath9346

Project Repository:

https://github.com/Manjunath9346/Real-Time-Chat-Application

---

# 📄 License

This project is intended for educational, portfolio, and demonstration purposes.