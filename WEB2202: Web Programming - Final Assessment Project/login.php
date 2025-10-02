<?php
session_start();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page - Simply Fresh Foods</title>
    <!-- Link to external CSS file with cache busting -->
    <link rel="stylesheet" href="login.css?v=2">
    <!-- Google Fonts for custom font -->
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
</head>
<body>
<header>
    <div class="container">
        <div class="header-container">
            <div class="logo-title">
                <img src="images/groceries.png" alt="Simply Fresh Foods Logo" class="logo">
                <h1>Simply Fresh Foods</h1>
            </div>
            <nav>
                <ul>
                    <li><a href="homepage.php">Home</a></li>
                    <li><a href="display_items.php">Products</a></li>
                    <li><a href="register.php">Join Us!</a></li>
                </ul>
            </nav>
        </div>
    </div>
</header>

<main class="main-content">
    <section class="login">
        <h2>Login</h2>
        <?php
        // Display error message if set
        if (isset($_SESSION['error'])) {
            echo '<p style="color:red;">' . $_SESSION['error'] . '</p>';
            unset($_SESSION['error']);
        }
        ?>
        <!-- Login form -->
        <form action="login_process.php" method="post">
            <label for="email">Email address:</label>
            <input type="email" id="email" name="email" required>
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
            <button type="submit">Login</button>
        </form>
        <p class="login-assistance">
            If you have forgotten your login details, please send a message to 
            <a href="mailto:22057301@imail.sunway.edu.my">22057301@imail.sunway.edu.my</a> for assistance.
        </p>
    </section>
</main>
<footer id="footer">
    <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
</footer>
</body>
</html>
