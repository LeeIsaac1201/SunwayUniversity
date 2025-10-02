<?php
session_start();

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "simply_fresh_foods";
$charset = 'utf8mb4';

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch products from the database
$sql = "SELECT item_name, price, image FROM items";
$result = $conn->query($sql);

if (!$result) {
    die("Query failed: " . $conn->error);
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simply Fresh Foods</title>
    <link rel="stylesheet" href="homepage.css">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
</head>
<body>
    <!-- Header Section -->
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
                    <?php if (isset($_SESSION['user_id'])): ?>
                        <li><a href="account.php">My Account</a></li>
                        <li><a href="logout.php">Logout</a></li>
                    <?php else: ?>
                        <li><a href="login.php">Login</a></li>
                        <li><a href="register.php">Join Us!</a></li>
                    <?php endif; ?>
                    <li><a href="shopping_cart.php"><i class="fas fa-shopping-cart"></i></a></li>
                </ul>
            </nav>
        </div>
    </header>

    <!-- Main Content -->
    <main>
        <div class="text-container">
            <!-- Hero Section -->
            <section class="hero">
                <h2>Fresh Groceries Delivered to Your Doorstep</h2>
                <p>Order fresh groceries online and get them delivered to your home.</p>
                <div class="search-container">
                    <form action="display_items.php" method="GET">
                        <input type="text" name="name" id="search-input" placeholder="Search for products...">
                        <button type="submit" id="search-button">Search</button>
                    </form>
                </div>
            </section>

            <!-- Featured Products Section -->
            <section class="featured-products">
                <h2>Featured Products</h2>
                <div class="product-carousel">
                    <div class="product-track">
                        <?php
                        if ($result->num_rows > 0) {
                            while($row = $result->fetch_assoc()) {
                                // Assuming $row["image"] contains a relative path
                                $imagePath = $row["image"];
                                
                                // Construct the full path to the image
                                $fullImagePath = 'http://localhost/codes/' . $imagePath; // Adjusted for your local setup

                                // Debugging output
                                echo '<!-- Debug: Item Name: ' . htmlspecialchars($row["item_name"]) . ' -->';
                                echo '<!-- Debug: Image URL: ' . htmlspecialchars($fullImagePath) . ' -->';
                                echo '<!-- Debug: File exists: ' . (file_exists($imagePath) ? 'Yes' : 'No') . ' -->';
                                
                                // Display the image
                                if (file_exists($imagePath)) {
                                    echo '<div class="product">';
                                    echo '<img src="' . htmlspecialchars($fullImagePath) . '" alt="' . htmlspecialchars($row["item_name"]) . '">';
                                    echo '<h3>' . htmlspecialchars($row["item_name"]) . '</h3>';
                                    echo '<p>RM' . htmlspecialchars($row["price"]) . '</p>';
                                    echo '</div>';
                                } else {
                                    echo '<div class="product">';
                                    echo '<img src="images/default.png" alt="Image not available">';
                                    echo '<h3>' . htmlspecialchars($row["item_name"]) . '</h3>';
                                    echo '<p>RM' . htmlspecialchars($row["price"]) . '</p>';
                                    echo '</div>';
                                }
                            }
                        } else {
                            echo "No products found.";
                        }
                        $conn->close();
                        ?>
                    </div>
                </div>
            </section>
        </div>
    </main>

    <!-- Footer Section -->
    <footer id="footer">
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
    <script src="homepage.js"></script>
</body>
</html>
