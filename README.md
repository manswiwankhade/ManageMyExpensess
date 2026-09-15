# 💰 ManageMyExpenses

ManageMyExpenses is a feature-rich Android expense management application designed to help users track their daily expenses, manage budgets, monitor spending patterns, and keep their financial information organized.

The application provides a simple and user-friendly interface with authentication, expense tracking, budget management, analytics, alerts, and profile management.

---

## 📱 Application Overview

Managing daily expenses manually can make it difficult to understand where money is being spent. ManageMyExpenses provides a centralized platform where users can record their expenses, monitor their spending, set budgets, and analyze their financial activity.

The application focuses on making expense management simple, organized, and easy to understand.

---

## ✨ Features

### 🔐 User Authentication
- User registration and login
- Email and password authentication
- Phone number verification using OTP
- Firebase Authentication for secure user authentication
- Password reset functionality

### 💸 Expense Management
- Add and manage daily expenses
- Record expense amount and details
- Organize expenses according to categories
- View recorded expenses
- Maintain personal expense records

### 💰 Budget Management
- Set a spending budget
- Monitor budget usage
- Track remaining budget
- Helps users control unnecessary spending

### 🚨 Budget Alerts
- Monitor spending against the set budget
- Alert users when spending approaches the budget limit
- Helps prevent unnecessary overspending

### 📊 Analytics
- Analyze spending patterns
- View expense distribution
- Category-based expense analysis
- Visual representation of financial activity
- Helps users understand where their money is being spent

### 👤 Profile Management
- View user profile
- Update profile name
- Display registered email and mobile number
- Change profile picture
- Change/reset password
- Logout functionality

### ⚙️ Preferences
- Notification settings
- Dark mode option
- User-specific settings stored locally

---

## 🔥 Firebase Integration

ManageMyExpenses uses Firebase services for authentication and cloud data management.

### Firebase Authentication
Firebase Authentication is used for:
- User registration
- Login
- Email/password authentication
- Phone number verification
- Password reset

### Cloud Firestore
Cloud Firestore is used to store and manage user-related application data.

The application uses user-specific documents to keep data organized and associated with the authenticated user.

---

## 🛠️ Technologies Used

| Technology | Purpose |
|------------|---------|
| Java | Application development |
| Android Studio | Development environment |
| XML | User interface design |
| Firebase Authentication | User authentication |
| Cloud Firestore | Cloud database |
| Android SDK | Android application development |
| Git | Version control |
| GitHub | Source code hosting |

---

## 🏗️ Application Structure

The application follows an activity-based Android architecture.

Main sections of the application include:

- Splash Screen
- Login / Registration
- Home
- Expense Management
- Analytics
- Profile
- Budget Management
- Security & Authentication

---

## 🔄 Application Workflow

```text
                    ┌───────────────┐
                    │  Splash Screen│
                    └───────┬───────┘
                            │
                 ┌──────────┴──────────┐
                 │                     │
            User Logged In       New / Logged Out
                 │                     │
                 ▼                     ▼
            Main Activity         Login / Sign Up
                 │                     │
                 │              Authentication
                 │                     │
                 └──────────┬──────────┘
                            ▼
                         Home
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
          Expenses       Analytics       Profile
             │              │              │
             ▼              ▼              ▼
          Budget         Spending       Settings
          & Alerts       Analysis       & Security
