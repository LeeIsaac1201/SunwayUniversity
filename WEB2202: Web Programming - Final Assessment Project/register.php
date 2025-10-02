<?php
session_start();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simply Fresh Foods - Registration</title>
    <link rel="stylesheet" href="register.css?v=1">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
</head>
<body>
    <!-- Header section with site logo, title, and navigation -->
    <header>
        <div class="header-container">
            <div class="logo-title">
                <img src="images/groceries.png" alt="Simply Fresh Foods Logo" class="logo">
                <h1>Simply Fresh Foods</h1>
            </div>
            <nav>
                <ul>
                    <li><a href="homepage.php">Home</a></li>
                    <li><a href="display_items.php">Products</a></li>
                    <li><a href="login.php">Login</a></li>
                </ul>
            </nav>
        </div>
    </header>
    
    <!-- Main content area -->
    <main>
        <section class="register">
            <h2>Registration</h2>
            <?php
            // Display error message if set
            if (isset($_SESSION['error'])) {
                echo '<p style="color:red;">' . $_SESSION['error'] . '</p>';
                unset($_SESSION['error']);
            }
            ?>
            <!-- Registration form -->
            <form action="register_process.php" method="post">
                <fieldset>
                    <legend>Personal Information</legend>
                    
                    <label for="first_name">First Name:</label>
                    <input type="text" id="first_name" name="first_name" required><br>
                    
                    <label for="last_name">Last Name:</label>
                    <input type="text" id="last_name" name="last_name" required><br>
                    
                    <label for="sex">Sex:</label>
                    <select id="sex" name="sex" required>
                        <option value="male">Male</option>
                        <option value="female">Female</option>
                    </select><br>
                    
                    <label for="birthdate">Birthdate:</label>
                    <input type="date" id="birthdate" name="birthdate" required><br>
                    
                    <label for="phone_number">Phone Number:</label>
                    <input type="text" id="phone_number" name="phone_number"><br>
                </fieldset>
                
                <fieldset>
                    <legend>Address Information</legend>
                    
                    <label for="street">Street:</label>
                    <input type="text" id="street" name="street"><br>
                    
                    <label for="district">District:</label>
                    <input type="text" id="district" name="district"><br>
                    
                    <label for="city">City:</label>
                    <input type="text" id="city" name="city"><br>
                    
                    <label for="postal_code">Postal Code:</label>
                    <input type="text" id="postal_code" name="postal_code"><br>
                    
                    <label for="state">State:</label>
                    <input type="text" id="state" name="state"><br>
                    
                    <label for="country">Country:</label>
                    <input type="text" id="country" name="country"><br>
                </fieldset>
                
                <fieldset>
                    <legend>Account Information</legend>
                    
                    <label for="email">Email Address:</label>
                    <input type="email" id="email" name="email" required><br>
                    
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required><br>
                </fieldset>
                
                <button type="submit">Register</button>
            </form>
        </section>
    </main>
    
    <!-- Footer section -->
    <footer id="footer">
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
</body>
</html>
