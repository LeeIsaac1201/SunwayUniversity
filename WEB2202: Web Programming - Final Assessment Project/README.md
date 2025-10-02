# Grocery E-commerce Platform

This project is a browser-based grocery e-commerce website prototype that promotes healthy, organic grocery access while demonstrating practical web-programming skills. Built as an individual final assessment for **WEB2202: Web Programming**, the system implements core web application concepts, such as secure user registration/login with role-based access (user and administrator), session management and timeout, PHP Data Objects (PDO) with prepared statements for safe database interaction, server- and client-side form validation, shopping cart and checkout flows, basic rewards/points gamification, and an administrator's dashboard for product and user management.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Disclaimer](#disclaimer)
3. [Features](#features)
4. [Installation and Running Instructions](#installation-and-running-instructions)
5. [Usage](#usage)
6. [File Format](#file-format)
7. [Example](#example)
8. [Notes](#notes)
9. [License](#license)

---

## Project Overview :blue_book:

This project provides a simple and effective PHP/MySQL web application to prototype a grocery e-commerce experience focused on healthy, locally sourced groceries — Simply Fresh Foods. Users can browse product listings, create and manage accounts, add items to a session-persisted shopping cart, and complete checkouts where orders are recorded and basic confirmation emails are sent. Administrators can add, edit, and delete products and manage user accounts through an admin dashboard. A lightweight rewards/points system awards bonuses for qualifying “healthy” or organic purchases.

Application data (products, users, orders, rewards) is persisted in a MySQL database (SQL dump included in `sql/`), while transient state, such as shopping carts, is held in PHP sessions. The codebase is structured as a modular XAMPP-ready project (`htdocs/` server pages, `includes/` utilities, `admin/` management pages), making it straightforward to run locally for evaluation and extension.

The implementation highlights practical web-programming fundamentals: secure user authentication (password hashing and `password_verify`), role-based access control, session management and timeout handling, server- and client-side form validation, PDO with prepared statements for safe database interaction, session-based shopping cart flows, email notifications, and a responsive, accessible frontend built with standard Hypertext Markup Language (HTML)/Cascading Style Sheets (CSS)/JavaScript (JS). The project serves both as an assessment submission for WEB2202: Web Programming and a compact learning example demonstrating end-to-end web application design and common security best practices.

---

## Disclaimer :warning:

This project was developed as an **individual academic submission and is intended primarily for demonstration and assessment purposes**. While the implementation demonstrates realistic e-commerce flows (registration/login, sessioned shopping cart, checkout, administrator Create, read, update, and delete (CRUD), basic rewards), it is **not** a production-grade storefront. Important limitations include simplified payment and shipping models, reliance on PHP `mail()` for email (which may not work in local development without Simple Mail Transfer Protocol (SMTP) configuration), and deliberate scope choices that omit advanced features such as secure payment gateway integration, production-grade authentication hardening, and scalable deployment practices.

The site’s **mechanics, data handling, and security are implemented at a demonstrable academic level** (password hashing, prepared statements with PDO, session timeout) and should **not** be used as-is for real customer transactions or sensitive production environments without further hardening and professional review.

**This project may receive further updates in the future.** For example, improved email/short message service (SMS) delivery via SMTP or third-party providers, payment gateway sandbox integration, additional admin analytics, and usability or accessibility refinements. If you plan to run or extend this code, please back up your database and configuration files before upgrading, and review the changelog or repository for compatibility notes.

---

## Features :sparkles:

1. **User accounts and profiles**
    - Register, log in, log out, and edit profile.
    - Passwords hashed using `password_hash()` and verified with `password_verify()`.
2. **Role-based access**
    - Regular **User** and **Administrator** roles.
    - Administrator-only pages (product/user management) protected by session checks.
3. **Shopping cart and checkout**
    - Session-persisted shopping cart (`$_SESSION['cart']`) with add/update/remove.
    - Checkout flow that creates order records and awards reward points.
4. **Admininistrator dashboard**
    - CRUD for products (add/edit/delete), including product images and categories.
    - User management (list, edit, delete) and basic order oversight.
5. **Database-backed persistence**
    - MySQL schema (SQL dump included in `sql/`) with tables for users, products, orders, points/rewards, and notifications.
    - Database access via PDO and prepared statements for safer queries.
6. **Email and notifications**
    - Order confirmation and notification emails (PHP `mail()` by default; can be swapped for SMTP).
7. **Form validation and security**
    - Server-side validation for all critical inputs plus client-side checks for better user experience (UX).
    - Basic protections: Prepared statements, session timeout, and input sanitisation helpers.
8. **Rewards/points system**
    - Points awarded at checkout; bonus rules available for “healthy” or organic products.
    - Points can be displayed on the user profile and used for simple reward flows.
9. **Search, categories and product listing**
    - Product browsing by category, simple search, and product detail pages.
10. **Local development/deployment**
    - XAMPP-ready project structure (`htdocs/`, `includes/`, `admin/`) with importable SQL schema for quick local setup.
    - Sample administrative account and seed data provided in the SQL dump.

---

## Installation and Running Instructions :hammer_and_wrench:

1. **Requirements**
    - XAMPP (includes Apache, PHP, and MySQL)
    - Web browser (Chrome, Firefox, Edge, etc.)
2. **Setup**
    - Download or clone this project into your XAMPP `htdocs/` directory. Example path:
        ```
        C:\xampp\htdocs\simply-fresh-foods\
        ```
    - Ensure the following folders exist: `includes/`, `admin/`, `sql/`, and any others referenced in the project.
3. **Database**
    - Start **XAMPP Control Panel** and ensure **Apache** and **MySQL** are running.
    - Open **phpMyAdmin** in your browser.
    - Create a new database (e.g., `simply_fresh_db`).
    - Import the SQL schema:
        - Select your new database in phpMyAdmin.
        - Click **Import** and choose the SQL file from the `sql/` directory (e.g., `simply_fresh.sql`).
        - Execute the import.
4. **Configuration**
    - Open the configuration file (commonly `includes/config.php`).
    - Update database connection settings if needed:
    ```
    <?php
    // ...existing code...
    $db_host = 'localhost';
    $db_name = 'simply_fresh_db'; // your database name
    $db_user = 'root';            // default XAMPP user
    $db_pass = '';                // default XAMPP password is empty
    // ...existing code...
    ```
    - Save your changes.
5. **Run the Application**
    - In your browser, go to `http://localhost/simply-fresh-foods/`.
    - Register a new user or log in with the sample admin account (see SQL seed data for credentials).

---

## Usage :joystick:

1. **Access the application**
    - Open your web browser and navigate to http://localhost/simply-fresh-foods/ after completing installation and setup.
2.  **User registration and login**
    - New users can register by providing a unique email, password, and basic profile information.
    - Existing users can log in using their credentials. Passwords are securely hashed and verified.
3. **Browsing and searching products**
    - Browse available grocery products by category or use the search bar to find specific items.
    - Click on a product to view details, including images, descriptions, and nutritional information.
4. **Shopping cart and checkout**
    - Add products to your shopping cart. The cart persists during your session.
    - Update quantities or remove items as needed.
    - Proceed to checkout to review your order, confirm details, and place your order.
    - Upon successful checkout, an order record is created and a confirmation email is sent (if email is configured).
5. **Rewards and points**
    - Earn reward points for each purchase, with bonus points for qualifying “healthy” or organic products.
    - View your current points balance on your profile page.
6. **Profile management**
    - Access your profile to view or update your information and see your order history and rewards.
7. **Administrator features**
    - Log in as an administrator (see SQL seed data for credentials).
    - Access the admin dashboard to manage products (add, edit, delete), view and manage users, and oversee orders.
8. **Session and security**
    - User sessions are managed securely with automatic timeout for inactivity.
    - All sensitive actions require authentication and appropriate user roles.
9. **Logging out**
    - Click the logout button to securely end your session.

---

## File Format :page_facing_up:

- **All persistent data is stored in a MySQL database** (see the SQL dump in the `sql/` directory). The main tables include:
    - `users`
    - `products`
    - `orders`
    - `order_items`
    - `rewards` (or `points`)
    - `notifications`
- **Key table structures and fields:**
    - `users`
        - `id` (INT, primary key, auto-increment)
        - `email` (VARCHAR, unique)
        - `password_hash` (VARCHAR), hashed using PHP’s `password_hash()`
        - `role` (ENUM: 'user', 'admin')
        - `name`, `address`, `created_at`, etc.
    - `products`
        - `id` (INT, primary key)
        - `name` (VARCHAR)
        - `description` (TEXT)
        - `category` (VARCHAR)
        - `price` (DECIMAL)
        - `image` (VARCHAR, filename or path)
        - `is_organic` (BOOLEAN)
        - `is_healthy` (BOOLEAN)
        - `stock` (INT)
    - `orders`
        - `id` (INT, primary key)
        - `user_id` (INT, foreign key)
        - `total` (DECIMAL)
        - `created_at` (DATETIME)
        - `status` (VARCHAR)
    - `order_items`
        - `id` (INT, primary key)
        - `order_id` (INT, foreign key)
        - `product_id` (INT, foreign key)
        - `quantity` (INT)
        - `price` (DECIMAL)
    - `rewards`/`points`
        - `id` (INT, primary key)
        - `user_id` (INT, foreign key)
        - `points` (INT)
        - `awarded_at` (DATETIME)
        - `reason` (VARCHAR)
    - `notifications`
        - `id` (INT, primary key)
        - `user_id` (INT, foreign key)
        - `type` (VARCHAR)
        - `message` (TEXT)
        - `created_at` (DATETIME)
        - `is_read` (BOOLEAN)
- **Session data** (such as the shopping cart) is stored in PHP’s `$_SESSION` superglobal and is not persisted to disk.
- **Product images** are stored in a designated directory (e.g., images), with file paths referenced in the `products` table.

---

## Example :mag_right:

Below is an example of how a user's order and rewards data might appear in the database after a successful checkout.

Example: `users` table
```
| id | email             | password_hash                 | role   | name      | address         | created_at          |
|----|-------------------|-------------------------------|--------|-----------|-----------------|---------------------|
| 1  | alice@email.com   | $2y$10$...                    | user   | Alice Lee | 123 Main St     | 2025-09-30 10:15:00 |
```

Example: `products` table
```
| id | name           | category | price | is_organic | is_healthy | stock | image            |
|----|----------------|----------|-------|------------|------------|-------|------------------|
| 1  | Organic Apple  | Fruit    | 1.99  | 1          | 1          | 100   | apple.jpg        |
| 2  | Whole Wheat    | Bakery   | 2.49  | 0          | 1          | 50    | wheat_bread.jpg  |
```

Example: `orders` table
```
| id | user_id | total | created_at           | status   |
|----|---------|-------|----------------------|----------|
| 1  | 1       | 4.48  | 2025-10-02 14:22:00  | placed   |
```

Example: `order_items` table
```
| id | order_id | product_id | quantity | price |
|----|----------|------------|----------|-------|
| 1  | 1        | 1          | 2        | 1.99  |
| 2  | 1        | 2          | 1        | 2.49  |
```

Example: `rewards` table
```
| id | user_id | points | awarded_at           | reason                |
|----|---------|--------|----------------------|-----------------------|
| 1  | 1       | 10     | 2025-10-02 14:22:01  | Checkout bonus        |
| 2  | 1       | 5      | 2025-10-02 14:22:01  | Organic purchase      |
```

Example: `notifications` table
```
| id | user_id | type      | message                        | created_at           | is_read |
|----|---------|-----------|--------------------------------|----------------------|---------|
| 1  | 1       | order     | Your order #1 has been placed. | 2025-10-02 14:22:02  | 0       |
```

---

## Notes :memo:
 - The rewards/points system is intentionally simple and can be extended with more complex rules or redemption features.
 - Product images must be manually placed in the correct images directory to match database references.
 - Email notifications may not work in local XAMPP setups unless SMTP is configured; for reliable delivery, consider using an SMTP library or service.
 - The administrative dashboard is accessible only to users with the admin role. Change the default administrative credentials after setup.
 - All database interactions use PDO with prepared statements to help prevent SQL injection.
 - If you encounter issues, check your PHP error logs, database connection settings, and ensure all required XAMPP services are running. 

---
## License :scroll:
© 2025 Lee Ming Hui Isaac. All rights reserved.

This code and its documentation are proprietary. You may not copy, modify, distribute, or otherwise use this software without express written permission from the copyright holder. 
