# Nutritional Information Database (Scala/ScalaFX)
This project is a **Scala** desktop application with a **ScalaFX** graphical user interface, developed as an individual final assignment for **PRG2104: Object-Oriented Programming** at the Faculty of Engineering and Technology, Sunway University. The system demonstrates object-oriented design principles, such as encapsulation, inheritance, polymorphism, and composition, while emphasising Unified Modelling Language (UML)-driven planning, modular class structure, FXML-based user interface (UI) design with controllers, file-based persistence, and basic image handling for a user-friendly nutritional information manager.

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

## Project Overview 📘
This project provides a simple and effective Scala desktop application with a ScalaFX/FXML graphical user interface for managing nutritional information. Users can add, view (read-only detail dialogues with images), edit, and delete food items; create and manage user profiles; and persist application data such as food item records and user profiles to plain-text files (for example, food_items.txt and user_profiles.txt), which act as a lightweight database. The application leverages object-oriented programming (OOP) fundamentals taught, such as classes and case classes, encapsulation, inheritance, polymorphism, and composition, while demonstrating practical skills in collections, file input/output (I/O), FXML-backed controller wiring, input validation, basic image handling, and modular design separating model, view, and controller layers.

---

## Disclaimer ⚠️
This application was developed as an individual final assessment project for **PRG2104: Object-Oriented Programming** and reflects design choices made to satisfy assignment requirements and demonstrate OOP concepts. It is **not** a production-grade nutrition platform. The feature set, UI, and data formats were implemented for clarity, maintainability, and evaluation purposes; they may be simplified compared with commercial nutrition software.

The project may receive further updates after submission (bug fixes, user experience (UX) improvements, additional validation, extra features like export/import, or format changes to persistence files). If you plan to build upon or distribute this project, expect that future commits could change file formats, application programming interfaces (APIs), or resource locations; review the repository changelog and README before upgrading.

---

## Features ✨
1. **Full food item CRUD with guided forms:** A complete Create/Read/Update/Delete flow for food items implemented via FXML forms and Scala controllers. The Add/Edit dialogs validate input (required fields, numeric ranges for calories/protein/fat/carbs, and serving size formatting) and show inline error indicators so the user can correct mistakes before saving. Created and updated items immediately appear in the table view, with changes persisted to storage so they survive restarts.
2. **Rich, read-only detail dialog:** Double-clicking a table row (or selecting and clicking *View*) opens a modal, read-only dialog that presents every data field for the selected `FoodItem` along with a preview image (if an image path was supplied). The dialog is intentionally modal and only dismissible via explicit UI controls (Close/Edit/Delete), demonstrating careful UX choices for information confirmation and preventing accidental window closures.
3. **User profile management with persistence:** Users can create and maintain profiles that store personal metadata (username, display name, email) and fitness/health parameters (height, weight, activity level, goal, target weight). The Add/Edit profile dialog enforces basic validation (email format, sensible height/weight ranges) and saves profiles to file storage through `UserProfileStorage`, enabling multiple profiles to be kept and reloaded across application runs.
4. **File-based persistence with load/save utilities:** Food items and user profiles are stored as plain-text files (e.g., `food_items.txt`, `user_profiles.txt`) using project-specific parsing and serialisation utilities in `FoodItemStorage` and `UserProfileStorage`. The storage layer focuses on simple, human-editable formats that make debugging and grading straightforward, while also handling malformed lines, optional fields, and basic recovery strategies during loading.
5. **In-memory database wrapper for fast UI operations:** The UI works against an in-memory `DatabaseProvider.db`, which exposes collections of `FoodItem` and `UserProfile` instances. This pattern keeps the UI responsive (fast table updates, filtering, selection handling) and isolates file I/O to explicit load/save operations so controllers remain focused on presentation logic rather than low-level persistence details.
6. **FXML and controller separation (model-view-controller (MVC)-style, testable controllers):** The graphical user interface (GUI) is defined in FXML resource files and wired to Scala controller classes that encapsulate event handling and validation logic. This separation encourages modular design, makes views easier to prototype or replace, and allows controllers to be unit-tested independently of the UI layout.
7. **Robust input validation and user-friendly error handling:** All user-editable forms include multi-layer validation (required-field checks, numeric parsing, sensible range checks, and image-path existence checks). Validation failures produce clear, actionable messages and prevent invalid states from being saved. Controllers also catch and report I/O exceptions (e.g., saving failures) in dialogs so users and graders can diagnose problems quickly.
8. **Image handling and resource management:** Food items may reference local image files (paths relative to `src/main/resources/images/` or absolute paths). The GUI safely attempts to load images for the detail dialog and form previews, gracefully falling back to a placeholder when files are missing or unreadable. The image-loading code includes simple scaling to keep UI layouts consistent and avoids crashing if image resources are invalid.
9. **Persistence-safe delete and synchronisation logic:** Delete operations update both the in-memory collection and the underlying storage file using careful write semantics (read-modify-write or rewrite strategies implemented in `FoodItemStorage.delete(...)`) to keep the file and UI in sync. The code includes basic locking or atomic-replace patterns (where available) to reduce the risk of partial writes or data corruption during save/delete cycles.
10. **Extensible, idiomatic Scala data model:** Domain objects are implemented as Scala `case class`es (for example, `FoodItem`, `UserProfile`), which provides immutability by default, convenient `copy()` semantics for edits, and pattern-matching friendliness. This design makes it easy to add fields (e.g., micronutrients, tags, source attribution) or swap the storage format (comma-separated values (CSV)/JavaScript Object Notation (JSON)) with minimal changes to controllers.
11. **Developer-friendly build and run flow:** The project is configured to run from sbt or an integrated development environment (IDE) (IntelliJ), with a predictable resource layout for FXML and images to simplify development and testing. A build.sbt template (or instructions) is included so graders and maintainers can run sbt run out-of-the-box; error messages and logging are intentionally clear to speed up debugging and iterative improvements.

---

## Installation and Running Instructions 🛠️
1. **Requirements**
- Java Development Kit (JDK) 11 or later installed and on your PATH (`java -version`).
- sbt (recommended) or an IDE that supports sbt/Scala (IntelliJ IDEA with the Scala plugin).
- JavaFX software development kit SDK (matching your JDK) only if you plan to run the app outside an IDE that already supplies JavaFX.
2. **Using sbt (recommended):** Compile and run from project root:
    ```
    # Compile sources and fetch dependencies.
    sbt compile

    # Run the main application (SBT will prompt/select the main class if there are multiples).
    sbt run
    ```
   If your Main object accepts command-line arguments, you can pass them with sbt "run arg1 arg2".
3. **Run an assembled Java Archive (JAR) (optional):** Build a fat JAR, then run with java:
- Add `sbt-assembly` to `project/plugins.sbt` if not present, then:
    ```
    sbt assembly
    # Example assembled jar path (depends on your sbt/scala version)
    java -jar target/scala-2.13/nutritional-info-db-assembly-0.1.0.jar
    ```
  If your assembled JAR **does not** bundle JavaFX, run with the JavaFX SDK on the module-path (replace path accordingly):

  macOS / Linux:
    ```
    export PATH_TO_FX=/path/to/javafx-sdk-<version>/lib
    java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target/scala-2.13/<your-jar>.jar
    ```
  Windows (PowerShell):
    ```
    $env:PATH_TO_FX = "C:\path\to\javafx-sdk-<version>\lib"
    java --module-path $env:PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target\scala-2.13\<your-jar>.jar
    ```
4. **Run from an IDE (IntelliJ)**
- Import the project as an SBT project.
- Ensure the Scala SDK is configured.
- Create a Run Configuration for the `Main` object (application entry point).
- If IntelliJ doesn’t provide JavaFX, add virtual machine (VM) options to the run configuration:
```
--module-path /path/to/javafx-sdk-<version>/lib --add-modules javafx.controls,javafx.fxml
```
5. **Quick compile and run with scalac/scala (not recommended for sbt projects)**     
   If you prefer a simple, quick test (small projects only), you can compile .scala files directly, but classpath/resource handling is manual:
    ```
    # Compile (may need to adjust file list)
    scalac -d out $(find src/main/scala -name '*.scala')
    
    # Run (use the fully-qualified Main class name)
    scala -cp out Main
    ```
   (Use this only for small demos: sbt handles dependencies and resources more reliably.)
6. **Notes and troubleshooting**
- `NoClassDefFoundError: javafx/application/Application` → JavaFX not on module-path. Fix by installing JavaFX and using the `--module-path`/`--add-modules` flags above or run from an IDE that bundles JavaFX.
- Images/FXML not found → ensure `src/main/resources/` contains `fxml/` and `images/` and that working directory is project root when running.
- If file I/O fails (e.g., saving `food_items.txt`), check that the process has write permission for the data folder (consider using a `data/` folder under the project root).

---

## Usage 🕹️
1. **Launch the application**
    - From sbt/IDE: Run the `Main` application object (e.g. `sbt run` or run the `Main` run-configuration in IntelliJ).
    - From a packaged JAR: `java -jar nutritional-info-db-<version>.jar` (when packaged as a runnable JAR; include JavaFX module flags if required).
2. **Initial load**
    - On startup, the application attempts to load persisted data files (by default, the app looks for `data/food_items.txt` and `data/user_profiles.txt` under the project root; if missing, the application will start with an empty dataset).
    - Loaded items appear in the main table view; loaded profiles are available in the profile management UI.
3. **Selecting/browsing items**
    - The main window shows a sortable table of `FoodItem` records. Click a column header to sort or use any search/filter controls implemented in the UI to narrow results.
    - Single-click selects a row for quick actions; double-click a row (or select and press View) to open the read-only detail dialog.
4. **View details (read-only dialog)**
    - The detail dialog displays every field for the selected item (name, calories, protein, fat, carbs, serving size, source notes, etc.) plus an image preview if an image path is present.
    - The dialog is modal and closes only via the provided UI buttons (Close/Edit/Delete), preventing accidental closure via the window "X" button.
5. **Add a new food item**
    - Click Add Food to open the Add dialog (implemented in `AddFoodDialog.fxml`/`AddFoodController.scala`).
    - Fill in required fields, choose or type an image path (optional), then click **Save**. The form validates input (required fields, numeric parsing and sensible ranges) and displays inline errors if needed. After saving, the new item appears immediately in the table and is persisted to storage.
6. **Edit an existing item**
    - From the detail dialog, click Edit, or from the main table select an item and click Edit to open the Edit dialog.
7. **Delete an item**
    - Use the **Delete** button in the detail dialog or the main UI. A confirmation prompt appears to prevent accidental deletion. Confirming removes the item from the in-memory database and updates the underlying data file so the deletion survives restarts.
8. **Manage user profiles**
    - Open the profile manager (Add/Edit profile) to create or edit user profiles stored via `UserProfileStorage`. Profiles capture metadata and health parameters (height, weight, activity level, goal, target weight). Save profile changes to persist them to `data/user_profiles.txt`.
9. **Image handling**
    - When adding/editing a food item, you may enter an image path relative to `src/main/resources/images/` or an absolute system path. The UI attempts to load and scale the image for previews; if the image is missing a placeholder is shown. Use relative resource paths if you want the images bundled with the project.
10. **Saving and data safety**
    - Most changes are saved when the user confirms Save/OK in dialogs. The `FoodItemStorage` and `UserProfileStorage` utilities handle serialisation and file writes (read-modify-write or rewrite strategies). If an I/O error occurs the UI reports it so you can retry or inspect file permissions.
11. **Exit and resume**
    - On exit, any saved records remain on disk; reopening the application reloads them from the data files so users can resume where they left off.

---

## File Format 📄

- **Where files live:** By default, the project stores its food-item data under `data/food_items.txt` at the project root. Images intended to be bundled with the app should live under `src/main/resources/images/` and be referenced by relative paths (for example, images/banana.png). All files are UTF-8 encoded.

- **Food items** (`data/food_items.txt`)
    - Exact line format (the project’s `toLine` behaviour)  
      Each `FoodItem` is serialised to a single line with five pipe-separated fields:
        ```
        <name>|<category_label>|<servingSize>|<nutrients_string>|<image_path_optional>
        ```
      Concretely, the project builds the line roughly as:
        ```
        val nutrientStr = nutrients.map(Nutrient.format).mkString(", ")
        val imageStr = imagePath.getOrElse("")
        s"$name|${category.label}|$servingSize|$nutrientStr|$imageStr"
        ```
    - **Fields (in order)**
        1. `name` → String (food item name, e.g. `Banana`)
        2. `category_label` → String (category label from the Category enum/object, e.g. Fruit)
        3. `servingSize` → Numeric or printable representation of the serving size (e.g. 100.0) as written by the model’s toString (the project uses a numeric value in practice)
        4. `nutrients_string` → A comma+space separated list of nutrient tokens produced by `Nutrient.format`, for example:
            ```
            Macronutrient: Protein: 1.1 g, Macronutrient: Fat: 0.3 g, Macronutrient: Carbohydrate: 23 g
            ```
           (Tokens may include macronutrients like Protein/Fat/Carbohydrate, fibre entries, or micronutrients using the project’s `Nutrient.format` conventions.)
        5. `image_path_optional` → Optional string path to an image (relative resource path such as images/banana.png or an absolute filesystem path). If no image is provided, this field is an empty string.
    - **Example line (pipe-separated)**
        ```
        Banana|Fruit|100.0|Macronutrient: Protein: 1.1 g, Macronutrient: Fat: 0.3 g, Macronutrient: Carbohydrate: 23 g|images/banana.png
        ```
    - **Parser behaviour and recommendations**
        - The `FoodItemStorage` loader expects the above structure and builds `FoodItem` + `Nutrient` objects by parsing `nutrients_string` according to the project’s `Nutrient` parsing/format rules.
        - If the image field is empty, the app treats the item as having no image and will use a placeholder in the UI.
        - Because `nutrients_string` itself uses commas, the storage format relies on the fixed five-field, pipe-separated layout to avoid ambiguity. Avoid inserting unescaped pipe characters (|) into the name or category fields.
        - For human edits, prefer using the same `Nutrient.format` style so the loader can parse nutrients reliably; if you plan to change the file schema later, consider migrating to JSON/CSV with a library for safer parsing/serialisation.

---

## Example 🔎
Below are concrete examples that match the project’s current `FoodItem.toLine` serialisation and the loader’s expectations.

**Example** `data/food_items.txt` (each line is one FoodItem)
```
Banana|Fruit|100.0|Macronutrient: Protein: 1.1 g, Macronutrient: Fat: 0.3 g, Macronutrient: Carbohydrate: 23 g|images/banana.png
Chicken Breast|Meat|100.0|Macronutrient: Protein: 31.0 g, Macronutrient: Fat: 3.6 g, Macronutrient: Carbohydrate: 0 g|images/chicken_breast.png
Spinach|Vegetable|30.0|Macronutrient: Protein: 2.9 g, Macronutrient: Fat: 0.4 g, Macronutrient: Carbohydrate: 3.6 g, Fibre: 2.2 g|images/spinach.png

```
- Each line uses the exact five-field pipe-separated layout:
    ```
    <name>|<category_label>|<servingSize>|<nutrients_string>|<image_path_optional>
    ```
- `nutrients_string` is produced by `Nutrient.format` and is a comma-and-space separated list of tokens such as `Macronutrient: Protein: 1.1 g` or `Fibre: 2.2 g`.
- The final field is the image path (empty string `""` if no image).
---

## Notes 📝
1. **Coursework scope:** This application was developed as an individual assignment for PRG2104: Object-Oriented Programming and is intended to demonstrate OOP design, ScalaFX/FXML UI patterns, and simple file-based persistence rather than to be a production-grade nutrition platform.
2. **Simplified data model and format:** Food items are stored as a single pipe-separated line per item (`<name>|<category>|<servingSize>|<nutrients_string>|<image_path>`). Nutrients are serialised using the project’s Nutrient.format conventions (comma-and-space separated tokens). This choice keeps files human-editable and easy to grade, but is less flexible than JSON for schema evolution.
3. **Images and resources:** Images intended to ship with the app should go under `src/main/resources/images/` and be referenced by relative paths (e.g., `images/banana.png`). The UI loads images when available and falls back to a placeholder image if the path is empty or the file cannot be read.
4. **Known gaps/out-of-scope features:** The application intentionally omits advanced nutrition features (e.g., recipe aggregation, comprehensive micronutrient databases, unit conversion helpers, nutrition facts label generation, or cloud sync). Those would be good future extensions, but were not required for the assignment.
5. **UI/UX choices:** The detail dialog is read-only and modal (closeable only via dialog controls) to reduce accidental loss of focus; Add/Edit flows include inline validation to prevent saving clearly invalid data (bad numeric parsing or missing required fields).
6. **Persistence safety and recommendations:** The current storage layer is simple for coursework readability. For more robust usage, consider switching to JSON/CSV with a tested library (circe/play-json), adding atomic-write patterns (write-to-temp + atomic rename), and creating automatic backups before destructive rewrites.
7. **Implementation notes/developer tips:** If you extend the project: keep a `data/README.md` documenting the exact `FoodItem` field order, add unit tests for `FoodItemStorage` and `UserProfileStorage` loaders/savers, and centralise resource-loading via `getResource(...)` to avoid platform-dependent path issues.
8. **Future updates:** The repository may receive updates (bug fixes, UX tweaks, and schema changes), and existing documents will not be accurate.

---

## License 📜
© 2025 Lee Ming Hui Isaac. All rights reserved.

This code and its documentation are proprietary. You may not copy, modify, distribute, or otherwise use this software without express written permission from the copyright holder.
