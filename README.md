# Sunway University

This repository contains the source code for academic projects completed at Sunway University.

## Projects

1. **Personal Book Management System** 📚  
   A Python-based application developed as a final assessment project for CSC1024: Programming Principles at Sunway University. Manage your personal book collection with add, delete, update, search, and display functionalities via a menu-driven interface. All data is stored in `Books.txt`.

   **Key Features**:
    - Add, delete, update, and search book records.  
    - View records in a formatted, sortable table.
    - Error handling for invalid inputs.
    - Text-based data storage (`Books.txt`).


2. **Pokémon Ga-Olé Arcade Game**  🕹️   
   A Java console simulation of the Pokémon Ga-Olé “battle-and-catch” arcade game, built as a group project for PRG1203: Object-Oriented Programming Fundamentals at Sunway University. The program demonstrates core object-oriented programming (OOP) principles (e.g., encapsulation, inheritance, polymorphism), simple file-based persistence, and console-friendly approximations of Ga-Olé mechanics for teaching and demonstration purposes.

   **Key features**:
    - Single-player, text-driven interface (no graphics or sound).
    - One JavaScript Object Notation (JSON) save per trainer in `pokemon/saves/`.
    - Japanese yen (`¥`) shown in the user interface (UI); save files use the yen key (legacy `coins` supported).
    - *Get by Battle*, *Get Now* (quick ball), and *Trainer & Battle* (simplified).
    - Evolution Chance, Grade Up, Mega Evolution prompts, and Z-Move simulation adapted for console.
    - Simplified two-versus-two (2v2) battles, basic move framework, and a console reaction mini-game.
    - Lightweight, human-readable saves; recommend using a JSON library for production.
    - Modular classes, collections usage, file input/output (I/O), and Unified Modelling Language (UML)-driven design suitable for learning OOP.

3. **Group Website Development** 🌐   
   A responsive website built using Hypertext Markup Language (HTML), Cascading Style Sheets (CSS), and JavaScript (JS) as a final assessment project for WEB1201: Web Fundamentals at Sunway University. Collaboratively designed and implemented semantic layouts, responsive navigation, and JS-driven form validation.

   **Key Features**:
    - Semantic HTML structure.
    - CSS styling and cohesive colour schemes.
    - Contact form with JS validation and user-friendly error messages.
    - Navigation menu with hover effects.
    - Sign-up and login user interface flow demonstration.

4. **Grocery E-commerce Platform**  🛒   
   A browser-based grocery e-commerce website prototype developed as a final assessment for WEB2202: Web Programming at Sunway University. The project demonstrates secure user authentication, session management, database-backed persistence, and a basic rewards system, all within a modular PHP/MySQL codebase suitable for local deployment with XAMPP.

   **Key features**:
    - Secure user registration, login, and profile management with password hashing.
    - Role-based access control for regular users and administrators.
    - Session-based shopping cart and checkout flow.
    - Administrator dashboard for product and user management.
    - MySQL database schema for users, products, orders, rewards, and notifications.
    - Order confirmation and notification emails (PHP `mail()` by default).
    - Server- and client-side form validation for all critical inputs.
    - Rewards/points system with bonus rules for healthy or organic products.
    - Product browsing by category, search, and detailed product pages.
    - XAMPP-ready structure with importable SQL schema for quick setup.
    - Sample administrator account and seed data included for testing.

5. **Nutritional Information Database** 🥘  
   A desktop application for managing food and nutrition data, developed in Scala with a ScalaFX graphical user interface as a final assessment for PRG2104: Object-Oriented Programming at Sunway University. The project demonstrates object-oriented design, modular model-view-controller (MVC) structure, FXML-based UI, file-based persistence, and user-friendly dialogs for a streamlined nutrition manager.

   **Key features**:
    - Add, view, edit, and delete food items with full create, read, update and delete (CRUD) support and inline input validation.
    - Read-only, modal detail dialogs with image previews for each food item.
    - User profile management with personal and health parameters, persisted to a file.
    - Plain-text, human-editable file storage for food items and user profiles.
    - In-memory database wrapper for fast UI updates and safe file synchronisation.
    - FXML-based UI with Scala controller classes for clear separation of concerns.
    - Robust input validation and user-friendly error handling throughout.
    - Image handling with support for bundled or external images and graceful fallbacks.
    - Persistence-safe delete operations and atomic file updates.
    - Extensible, idiomatic Scala data model using case classes and pattern matching.
    - Developer-friendly build and run flow with sbt and IntelliJ support; clear error messages for troubleshooting.
