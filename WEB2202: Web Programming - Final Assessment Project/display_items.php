<?php
// Start session management
session_start();

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Include the database connection file (creates the $pdo instance)
include('includes/db_connect.php');

// Check if the user is logged in
$is_logged_in = isset($_SESSION['user_id']);

// Initialise search variables from GET parameters with default empty values
$search_name = isset($_GET['name']) ? $_GET['name'] : '';
$search_category = isset($_GET['category']) ? $_GET['category'] : '';
$search_min_price = isset($_GET['min_price']) ? $_GET['min_price'] : '';
$search_max_price = isset($_GET['max_price']) ? $_GET['max_price'] : '';

// Build the SQL query with search filters using PDO prepared statements
$query = "SELECT * FROM items WHERE 1=1";
$params = [];

// Filter by product name if provided
if ($search_name) {
    $query .= " AND item_name LIKE ?";
    $params[] = '%' . $search_name . '%';
}

// Filter by category if provided
if ($search_category) {
    $query .= " AND category = ?";
    $params[] = $search_category;
}

// Filter by minimum price if provided
if ($search_min_price) {
    $query .= " AND price >= ?";
    $params[] = $search_min_price;
}

// Filter by maximum price if provided
if ($search_max_price) {
    $query .= " AND price <= ?";
    $params[] = $search_max_price;
}

// Prepare and execute the query
$stmt = $pdo->prepare($query);
$stmt->execute($params);
$results = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>All Items - Simply Fresh Foods</title>
    <!-- Link to external CSS file -->
    <link rel="stylesheet" href="display_items.css">
    <!-- Google Fonts and Font Awesome for icons -->
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
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
                    <?php if ($is_logged_in): ?>
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

    <!-- Main content area -->
    <main class="main-content">
        <!-- Sidebar for product search -->
        <aside class="search-container">
            <h2>Search Products</h2>
            <form action="display_items.php" method="get">
                <label for="name">Product Name:</label>
                <input type="text" id="name" name="name" value="<?php echo htmlspecialchars($search_name); ?>">
                
                <label for="category">Category:</label>
                <select id="category" name="category">
                    <option value="">All</option>
                    <option value="vegetable" <?php if ($search_category == 'vegetable') echo 'selected'; ?>>Vegetables</option>
                    <option value="fruit" <?php if ($search_category == 'fruit') echo 'selected'; ?>>Fruits</option>
                    <option value="meat" <?php if ($search_category == 'meat') echo 'selected'; ?>>Meat</option>
                    <option value="seafood" <?php if ($search_category == 'seafood') echo 'selected'; ?>>Seafood</option>
                    <option value="grains" <?php if ($search_category == 'grains') echo 'selected'; ?>>Grains</option>
                    <option value="dairy" <?php if ($search_category == 'dairy') echo 'selected'; ?>>Dairy</option>
                    <option value="oils" <?php if ($search_category == 'oils') echo 'selected'; ?>>Oils</option>
                </select>
                
                <label for="min_price">Min Price:</label>
                <input type="number" id="min_price" name="min_price" step="0.01" value="<?php echo htmlspecialchars($search_min_price); ?>">
                
                <label for="max_price">Max Price:</label>
                <input type="number" id="max_price" name="max_price" step="0.01" value="<?php echo htmlspecialchars($search_max_price); ?>">
                
                <button type="submit">Search</button>
            </form>
        </aside>

        <!-- Section displaying the list of items -->
        <section class="items-container">
            <h2>All Items</h2>
            <div class="item-grid">
                <?php
                // Check if any results were returned from the database
                if ($results && count($results) > 0) {
                    // Loop through each item and display its details
                    foreach ($results as $row) {
                        // Set default values for missing data
                        $id = isset($row['item_id']) ? $row['item_id'] : '';
                        $image = isset($row['image']) ? $row['image'] : 'default_image.jpg';
                        $name = isset($row['item_name']) ? $row['item_name'] : 'Unnamed Item';
                        $price = isset($row['price']) ? $row['price'] : '0.00';

                        // Each item is wrapped in a container with a unique ID based on its name
                        echo '<div class="item" id="' . htmlspecialchars($name) . '">';
                        echo '<img src="' . $image . '" alt="' . $name . '">';
                        echo '<h3>' . $name . '</h3>';
                        echo '<p>RM' . $price . '</p>';
                        // Quantity controls for selecting item amount
                        echo '<div class="quantity-controls">';
                        echo '<button class="quantity-btn" onclick="decreaseQuantity(this)">-</button>';
                        echo '<input type="text" class="quantity-input" value="1" readonly>';
                        echo '<button class="quantity-btn" onclick="increaseQuantity(this)">+</button>';
                        echo '</div>';
                        // Form to add the item to the cart with hidden input fields
                        echo '<form id="add-to-cart-form-' . $id . '" onsubmit="addToCart(event, ' . $id . ')">';
                        echo '<input type="hidden" name="action" value="add">';
                        echo '<input type="hidden" name="item_id" value="' . $id . '">';
                        echo '<input type="hidden" name="item_name" value="' . $name . '">';
                        echo '<input type="hidden" name="item_price" value="' . $price . '">';
                        echo '<input type="hidden" name="item_image" value="' . $image . '">';
                        echo '<input type="hidden" name="item_quantity" class="item-quantity" value="1">';
                        echo '<button type="submit" class="add-to-cart-btn">Add to Cart</button>';
                        echo '</form>';
                        echo '</div>';
                    }
                } else {
                    // Display a message if no items were found
                    echo '<p>No items found.</p>';
                }
                ?>
            </div>
        </section>
    </main>

    <!-- Footer section -->
    <footer>
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>

    <!-- Notification element for displaying messages -->
    <div class="notification" id="notification">
        <span id="notification-message"></span>
        <span class="close-btn" onclick="closeNotification()">&times;</span>
    </div>

    <!-- External JavaScript file -->
    <script src="display_items.js"></script>
</body>
</html>
