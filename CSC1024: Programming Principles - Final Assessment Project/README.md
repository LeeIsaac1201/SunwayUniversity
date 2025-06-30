# Personal Book Management System

This project is a Python-based application designed to help book enthusiasts manage their personal library. Developed as a final assessment project for CSC1024: Programming Principles at Sunway University, the system demonstrates file processing, user interaction, and efficient code organisation.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Installation and Running Instructions](#installation-and-running-instructions)
4. [Usage](#usage)
5. [File Format](#file-format)
6. [Example](#example)
7. [Notes](#notes)
9. [Credits](#credits)
9. [License](#license)

---

## Project Overview

This project provides a simple yet effective system to keep track of a personal book collection. Users can add, delete, update, display, and search for book records. The book information is stored in a text file (`books.txt`), which acts as a basic database. The application leverages Python fundamentals such as file processing, lists, conditional statements, loops, and user-defined functions.

---

## Features

- **Add Book Records:** Enter one or multiple book entries with complete details.
- **Delete Book Records:** Remove one or more book entries from the system.
- **Update/Edit Book Records:** Modify book details by searching with ISBN, author, or title.
- **Display Books:** View all current book records in a neatly formatted table with headings. Includes sorting by ISBN, author, title, genre, and more.
- **Search for Books:** Locate specific book details based on ISBN, author, or title.
- **Error Handling:** Provides clear error messages and options to retry or return to the main menu.
- **Exit:** Save all changes to the text file and exit the application gracefully.
- **Sorting:** Sort books by various categories (e.g., ISBN, author, title) in ascending or descending order.

---

## Installation and Running Instructions

### Prerequisites

- **Python 3.6** must be installed. For optimal performance and compatibility, it is recommended to install the latest version of Python. You can check your current Python version by running:
    ```bash
    python --version
    ```
- The project uses the following Python modules:
  - `os` (for clearing the screen and system interactions)
  - `datetime` (for date manipulation)

### Installation Steps

1. **Download the Project:** Extract the contents of the project folder. The core files include:
   - `Personal_Book_Management_System.py` – Main program source code.
   - `books.txt` – Text file containing the book records.
2. **Ensure File Placement:** Place `books.txt` in the same directory as `Personal_Book_Management_System.py`.
3. **Open a Terminal:** Navigate to the project directory.
4. **Run the Application:** Execute the following command:
    ```bash
    python Personal_Book_Management_System.py
    ```

---

## Usage

Once the program runs, a menu is displayed with the following options:
- **Display All Book Records:** View all books in the database in a formatted table.
- **Search for a Book:** Locate a specific book using search criteria such as ISBN, author, or title.
- **Add Book Record(s):** Input one or multiple book entries.
- **Update/Edit Book Record(s):** Modify existing book details by searching with ISBN, author, or title.
- **Delete Book Record(s):** Remove specified book records.
- **Show Team Background:** View details about the team members and their contributions.
- **Exit:** Save all changes back to `books.txt` and close the application.

Follow the on-screen instructions for each menu option to interact with the system.

---

## File Format

The `books.txt` file acts as the database for the application. Each book record is stored in the following format:

ISBN|Author|Title|Publisher|Genre|Year Published|Date Purchased|Status

---

## Example
Here is an example of how the `books.txt` file might look:
```plaintext
9781668026038|Hannah Grace|Icebreaker|Simon & Schuster|Romance|2022|01/01/2023|completed
9780063052734|Danya Kukafka|Notes on an Execution|HarperCollins|Thriller|2022|15/03/2023|to-read
9780143127741|Anthony Doerr|All the Light We Cannot See|Scribner|Historical Fiction|2014|10/05/2020|completed
```

---

## Notes
- **ISBN:** Must be exactly thirteen digits.
- **Date Purchased:** Must follow the `DD/MM/YYYY` format.
- **Status:** Can be one of the following:
  - `wishlist`
  - `to-read`
  - `reading`
  - `completed`
- **Special Characters:** The `|` character is not allowed in any field as it is used as a delimiter in the database.

---

## Credits

This project was made successful by the contributions of the following team members:
1. **[Darrance Beh Heng Shek (Group Leader)](https://github.com/darrancebeh)**  
   - Managed the team and project timeline.  
   - Developed the update book functionality, auxiliary functions, and utility functions.  
   - Designed the user interface (UI)/user experience (UX) and compiled the final program.  
   - Wrote the final report and flowchart.

2. **[Deron Ho Wen Harn](https://github.com/dyhaaa)**  
   - Developed the display book functionality.  
   - Wrote the report section and flowchart for the display feature.

3. **[Izzat Zulqarnain bin Izaiddin](https://github.com/ozen27)**  
   - Developed the search book functionality.  
   - Wrote the report section and flowchart for the search feature.

4. **[Lee Ming Hui Isaac](https://github.com/LeeIsaac1201)**  
   - Developed the add book functionality.  
   - Wrote the report section and flowchart for the add feature.

5. **[Tan Ho Chen](https://github.com/Alexanderthcgreat)**  
   - Contributed to the delete book functionality.  
   - Wrote the report section and flowchart for the delete feature.

---

## License

© 2023-2025 Darrance Beh Heng Shek, Deron Ho Wen Harn, Izzat Zulqarnain bin Izaiddin, Lee Ming Hui Isaac, and Tan Ho Chen. All rights reserved.

This code and its documentation are proprietary. You may not copy, modify, distribute, or otherwise use this software without express written permission from the copyright holder.
