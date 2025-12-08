# 🎮 Video Game Discovery Platform

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green) ![React](https://img.shields.io/badge/React-18-blue) ![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3%20%7C%20RDS-232F3E) ![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1)

> **A full-stack application designed to help users discover, curate, and receive personalized video game recommendations.**

[**View Live Demo**](http://henry-videogamedata-frontend-2025.s3-website.us-east-2.amazonaws.com/)

---

## 📖 About The Project

The **Video Game Discovery Platform** is a modern web application built to solve the problem of "choice paralysis" in gaming. By leveraging the massive IGDB database, this application allows users to search thousands of games, filter by genre and platform, and curate a personal "Top 5" list.

Based on these selections, the backend utilizes a custom recommendation algorithm to suggest new titles the user might love, providing a seamless and interactive discovery experience.

### ✨ Key Features (Current)
* **🚀 High-Performance Backend:** Built with **Java Spring Boot**, utilizing efficient caching (In-Memory) to handle high-traffic endpoints.
* **🔍 Advanced Search & Filtering:** Custom **MySQL Full-Text Indexing** allows for rapid searching, sorting, and filtering by genre or platform.
* **📱 Responsive Frontend:** A **React + Vite** interface styled with **Tailwind CSS**, optimized for both mobile and desktop experiences.
* **⚡ Infinite Scroll:** Seamless data loading using custom pagination logic to handle large datasets.
* **🤖 Smart Recommendations:** A logic engine that analyzes user favorites to generate curated game suggestions.
* **☁️ Cloud Native:** Fully deployed on **AWS** using EC2 (Backend), S3 (Frontend), and RDS (Database).

---

## 🛠️ Tech Stack

### Frontend
* **Framework:** React.js (Vite)
* **Styling:** Tailwind CSS, Framer Motion (for animations)
* **State Management:** React Hooks (`useState`, `useEffect`, `useRef`)
* **Deployment:** AWS S3 (Static Website Hosting)

### Backend
* **Language:** Java 21
* **Framework:** Spring Boot 3
* **Database Interaction:** Spring Data JPA / Hibernate
* **API Integration:** IGDB (Twitch API)
* **Deployment:** AWS EC2 (Amazon Linux 2023)

### Database & DevOps
* **Database:** MySQL (AWS RDS)
* **CI/CD:** Manual deployment scripts (Shell scripting)
* **Security:** CORS configuration, Environment Variable management

---

## 🚀 Roadmap & Upcoming Features

This project is actively evolving. The next phase of development focuses on transforming the platform from a discovery tool into a **social hub for gamers** (similar to a "Letterboxd for Video Games").

### 🔐 User Accounts & Authentication
* **Secure Login/Signup:** Implementation of JWT (JSON Web Tokens) and Spring Security.
* **Persistent Profiles:** Allow users to save their lists permanently across devices.

### 📝 The "Letterboxd" Aspect (Game Journal)
* **Game Diaries:** Users can log games they have played, are currently playing, or want to play (Backlog).
* **Reviews & Ratings:** Ability to leave star ratings (1-5) and written reviews for specific titles.
* **Lists:** Create custom collections (e.g., "Best Horror Games of 2024", "Co-op favorites").

### 🤝 Social Features
* **Follow System:** Follow friends and influencers to see their activity feeds.
* **Compare Tastes:** Compare "Top 5" lists with friends to see compatibility.
* **Activity Feed:** A timeline showing friends' recent ratings, reviews, and "Want to Play" additions.

---

## 🏗️ Architecture

```mermaid
graph TD
    User[User / Browser] -->|HTTP Request| CloudFront[AWS CloudFront / S3]
    CloudFront -->|API Calls| EC2[AWS EC2 (Spring Boot)]
    EC2 -->|SQL Queries| RDS[(AWS RDS MySQL)]
    EC2 -->|External API| IGDB[IGDB / Twitch API]