# Football Manager

A comprehensive online Football Manager simulation game.

## AI-Driven Development

This project is now entirely developed and maintained by **Generative AI and Autonomous Agents**. Special recognition goes to the following agents for their continuous contributions to the codebase, architecture, and feature implementation:

*   **Jules**
*   **Kiro**
*   **Antigravity**

## Overview

Football Manager is a full-stack application that simulates the management of a football club. It features a robust backend for match simulation, player transfers, training, and financial management, coupled with a modern React frontend for user interaction.

## Tech Stack

### Backend
*   **Java 21**
*   **Spring Boot 3.5.10**
*   **MySQL 8.0** (Primary Database)
*   **MongoDB** (NoSQL Data)
*   **Redis 7** (Caching)
*   **Spring Security** (Authentication & Authorization)
*   **WebSocket** (Real-time updates)

### Frontend
*   **React 18**
*   **Axios**
*   **SockJS & Stomp** (WebSocket Client)
*   **Recharts** (Data Visualization)

### Infrastructure & DevOps
*   **Docker & Docker Compose**
*   **Nginx** (Reverse Proxy)
*   **Prometheus & Grafana** (Monitoring)

## Getting Started

### Prerequisites

*   **Docker** and **Docker Compose** (Recommended)
*   **Java 21 JDK** (For manual backend build)
*   **Node.js 18+** (For manual frontend build)

### Running with Docker (Recommended)

The easiest way to run the application is using Docker Compose.

1.  Clone the repository:
    ```bash
    git clone https://github.com/lollito/fm.git
    cd fm
    ```

2.  Start the application stack:
    ```bash
    docker-compose up -d --build
    ```

This will start the following services:
*   **MySQL**: `localhost:3306`
*   **Redis**: `localhost:6379`
*   **Backend**: `http://localhost:8080`
*   **Frontend**: `http://localhost:3000`
*   **Admin Panel**: `http://localhost:3001`

### Manual Installation

#### Backend

1.  Navigate to the project root:
    ```bash
    cd fm
    ```
2.  Build and run the application:
    ```bash
    mvn spring-boot:run
    ```

#### Frontend (`fm-web`)

1.  Navigate to the frontend directory:
    ```bash
    cd fm-web
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start
    ```

The frontend will be available at `http://localhost:3000`.

## Project Structure

*   `src/`: Backend source code (Java/Spring Boot).
*   `fm-web/`: Frontend source code (React).
*   `fm-admin/`: Admin panel source code (React).
*   `docker/`: Docker configuration files for MySQL, Redis, Nginx, Prometheus, and Grafana.

## Database Schema

*(Legacy Schema Diagram)*
![DB Schema](https://user-images.githubusercontent.com/26112857/58509700-771dd780-8197-11e9-9eff-a854bc1b82a5.png)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Authors

*   **Lorenzo Cunto** - *Initial work* - [@Lollito](https://github.com/lollito)
*   **AI Agents (Jules, Kiro, Antigravity)** - *Current Development*
