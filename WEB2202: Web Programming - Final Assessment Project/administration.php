<?php
session_start();

// Check if the user is an administrator
if (!isset($_SESSION['is_admin']) || !$_SESSION['is_admin']) {
    header('Location: login.php');
    exit();
}

// Include the database connection file
include('includes/db_connect.php');

// Initialise notification message
$notification = '';

// Handle adding a new item
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['add_item'])) {
    $item_name = $_POST['item_name'];
    $category = $_POST['category'];
    $price = $_POST['price'];
    $image = $_POST['image'];
    $quantity_in_stock = $_POST['quantity_in_stock'];

    // Insert the new item into the database
    $query = "INSERT INTO items (item_name, category, price, image, quantity_in_stock) VALUES (?, ?, ?, ?, ?)";
    $stmt = $pdo->prepare($query);
    $stmt->execute([$item_name, $category, $price, $image, $quantity_in_stock]);

    // Set notification message
    $notification = 'Item added successfully!';

    // Redirect back to the administration page
    header('Location: administration.php?notification=' . urlencode($notification));
    exit();
}

// Handle removing an item
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['remove_item'])) {
    // Get the item name from the form submission
    $item_name = htmlspecialchars($_POST['item_name']);

    // Delete the item from the database
    $sql = "DELETE FROM items WHERE item_name = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$item_name]);

    // Set notification message
    $notification = 'Item removed successfully!';

    // Redirect back to the administration page
    header('Location: administration.php?notification=' . urlencode($notification));
    exit();
}

// Handle editing a user
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['edit_user'])) {
    $user_id = $_POST['user_id'];
    $first_name = $_POST['first_name'];
    $last_name = $_POST['last_name'];
    $role = $_POST['role'];
    $registration_date = $_POST['registration_date'];
    $point_balance = $_POST['point_balance'];

    // Update the user information in the database
    $query = "UPDATE users SET first_name = ?, last_name = ?, role = ?, registration_date = ?, point_balance = ? WHERE user_id = ?";
    $stmt = $pdo->prepare($query);
    $stmt->execute([$first_name, $last_name, $role, $registration_date, $point_balance, $user_id]);

    // Set notification message
    $notification = 'Information updated successfully!';

    // Redirect back to the administration page
    header('Location: administration.php?notification=' . urlencode($notification));
    exit();
}

// Handle deleting a user
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['delete_user'])) {
    $user_id = $_POST['user_id'];

    // Delete the user from the database
    $query = "DELETE FROM users WHERE user_id = ?";
    $stmt = $pdo->prepare($query);
    $stmt->execute([$user_id]);

    // Set notification message
    $notification = 'User deleted successfully!';

    // Redirect back to the administration page
    header('Location: administration.php?notification=' . urlencode($notification));
    exit();
}

// New block for updating order status
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['update_order'])) {
    // Retrieve order_id and status from the POST request
    $order_id = $_POST['order_id'];
    $status = $_POST['status'];

    // Update the order's status in the database
    $query = "UPDATE orders SET status = ? WHERE order_id = ?";
    $stmt = $pdo->prepare($query);
    $stmt->execute([$status, $order_id]);

    // Set notification message
    $notification = 'Order updated successfully!';

    // Redirect back to the administration page
    header('Location: administration.php?notification=' . urlencode($notification));
    exit();
}

// Fetch users from the database
$query = "SELECT user_id, first_name, last_name, role, registration_date, point_balance FROM users";
$stmt = $pdo->prepare($query);
$stmt->execute();
$users = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Fetch orders from the database
$query = "SELECT order_id, user_id, details, order_date, total_price, shipping_address, status FROM orders";
$stmt = $pdo->prepare($query);
$stmt->execute();
$orders = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Initialise search variables for items
$search_name = isset($_GET['name']) ? $_GET['name'] : '';
$search_category = isset($_GET['category']) ? $_GET['category'] : '';
$search_min_price = isset($_GET['min_price']) ? $_GET['min_price'] : '';
$search_max_price = isset($_GET['max_price']) ? $_GET['max_price'] : '';

// Build the SQL query with search filters using PHP Data Objects (PDO) prepared statements
$query = "SELECT * FROM items WHERE 1=1";
$params = [];

if ($search_name) {
    $query .= " AND item_name LIKE ?";
    $params[] = '%' . $search_name . '%';
}

if ($search_category) {
    $query .= " AND category = ?";
    $params[] = $search_category;
}

if ($search_min_price) {
    $query .= " AND price >= ?";
    $params[] = $search_min_price;
}

if ($search_max_price) {
    $query .= " AND price <= ?";
    $params[] = $search_max_price;
}

$stmt = $pdo->prepare($query);
$stmt->execute($params);
$items = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Get notification message from URL
if (isset($_GET['notification'])) {
    $notification = htmlspecialchars($_GET['notification']);
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administrative Panel - Simply Fresh Foods</title>
    <link rel="stylesheet" href="administration.css">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
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
                </ul>
            </nav>
        </div>
    </header>

    <!-- Main Content Section -->
    <main class="main-content">
        <div class="panel-header">
            <h2>Administrator's Panel</h2>
            <div class="button-container">
                <button onclick="showSection('items')">Items</button>
                <button onclick="showSection('orders')">Orders</button>
                <button onclick="showSection('users')">Users</button>
            </div>
        </div>
        <?php if ($notification): ?>
            <div class="notification" id="notification">
                <span id="notification-message"><?php echo $notification; ?></span>
                <span class="close-btn" onclick="closeNotification()">&times;</span>
            </div>
        <?php endif; ?>

        <!-- Users Section -->
        <section id="users-section" class="manage-users" style="display:none;">
            <aside class="manage-users-container">
                <h3>Manage Users</h3>
                <div class="user-form-container">
                    <table class="user-table">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>First Name</th>
                                <th>Last Name</th>
                                <th>Role</th>
                                <th>Registration Date</th>
                                <th>Point Balance</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php
                            if ($users && count($users) > 0) {
                                foreach ($users as $user) {
                                    echo '<form action="administration.php" method="post">';
                                    echo '<tr>';
                                    echo '  <td><input type="text" name="user_id" value="' . htmlspecialchars($user['user_id']) . '" required></td>';
                                    echo '  <td><input type="text" name="first_name" value="' . htmlspecialchars($user['first_name']) . '" required></td>';
                                    echo '  <td><input type="text" name="last_name" value="' . htmlspecialchars($user['last_name']) . '" required></td>';
                                    echo '  <td>';
                                    echo '    <select name="role" required>';
                                    echo '      <option value="user"' . ($user['role'] == 'user' ? ' selected' : '') . '>User</option>';
                                    echo '      <option value="admin"' . ($user['role'] == 'admin' ? ' selected' : '') . '>Admin</option>';
                                    echo '    </select>';
                                    echo '  </td>';
                                    echo '  <td><input type="datetime-local" name="registration_date" value="' . htmlspecialchars(date('Y-m-d\TH:i:s', strtotime($user['registration_date']))) . '" required></td>';
                                    echo '  <td><input type="number" name="point_balance" value="' . htmlspecialchars($user['point_balance']) . '" required></td>';
                                    echo '  <td style="display: flex; justify-content: center;">';
                                    echo '    <button type="submit" name="edit_user">Update</button>';
                                    echo '    <button type="submit" name="delete_user" class="delete" style="margin-left: 5px;">Delete</button>';
                                    echo '  </td>';
                                    echo '</tr>';
                                    echo '</form>';
                                }
                            } else {
                                echo '<tr><td colspan="7">No users found.</td></tr>';
                            }
                            ?>
                        </tbody>
                    </table>
                </div>
            </aside>
        </section>

        <!-- Items Section -->
        <section id="items-section" class="items" style="display:none;">
            <aside class="add-item">
                <div class="add-item-inner">
                    <h3 class="center-text">Add New Item</h3>
                    <form action="administration.php" method="post">
                        <input type="hidden" name="add_item" value="1">
                        <label for="item_name">Item Name:</label>
                        <input type="text" id="item_name" name="item_name" required>

                        <label for="category">Category:</label>
                        <select id="category" name="category" required>
                            <option value="Vegetable">Vegetable</option>
                            <option value="Fruit">Fruit</option>
                            <option value="Meat">Meat</option>
                            <option value="Seafood">Seafood</option>
                            <option value="Grains">Grains</option>
                            <option value="Dairy">Dairy</option>
                            <option value="Oils">Oils</option>
                        </select>

                        <label for="price">Price (RM):</label>
                        <input type="number" id="price" name="price" step="0.01" required>

                        <label for="image">Image URL:</label>
                        <input type="text" id="image" name="image" required placeholder="images/">

                        <label for="quantity_in_stock">Quantity in Stock:</label>
                        <input type="number" id="quantity_in_stock" name="quantity_in_stock" required>

                        <button type="submit">Add Item</button>
                    </form>
                </div>
            </aside>

            <aside class="remove-item-container">
                <h3>Remove Existing Item</h3>
                <!-- Search Form -->
                <aside class="search-container">
                    <h2>Search Items</h2>
                    <form action="administration.php" method="get">
                        <label for="name">Item Name:</label>
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
                <!-- Item List -->
                <div class="item-list">
                    <?php
                    if ($items && count($items) > 0) {
                        foreach ($items as $item) {
                            echo '<div class="item">';
                            echo '<img src="' . htmlspecialchars($item['image']) . '" alt="' . htmlspecialchars($item['item_name']) . '">';
                            echo '<h4>' . htmlspecialchars($item['item_name']) . '</h4>';
                            echo '<p>Category: ' . htmlspecialchars($item['category']) . '</p>';
                            echo '<p>Price: RM' . htmlspecialchars($item['price']) . '</p>';
                            echo '<p>Quantity in Stock: ' . htmlspecialchars($item['quantity_in_stock']) . '</p>';
                            if (isset($item['item_name'])) {
                                echo '<form action="administration.php" method="post">';
                                echo '<input type="hidden" name="remove_item" value="1">';
                                echo '<input type="hidden" name="item_name" value="' . htmlspecialchars($item['item_name']) . '">';
                                echo '<button type="submit">Remove Item</button>';
                                echo '</form>';
                            } else {
                                echo '<p>Error: Item not found.</p>';
                            }
                            echo '</div>';
                        }
                    } else {
                        echo '<p>No items found.</p>';
                    }
                    ?>
                </div>
            </aside>
        </section>

        <!-- Orders Section -->
        <section id="orders-section" class="orders" style="display:none;">
            <aside class="manage-orders-container">
                <h3>Manage Orders</h3>
                <div class="order-form-container">
                    <table class="order-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>User ID</th>
                                <th>Details</th>
                                <th>Order Date</th>
                                <th>Total Price</th>
                                <th>Shipping Address</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php
                            if ($orders && count($orders) > 0) {
                                foreach ($orders as $order) {
                                    echo '<form action="administration.php" method="post">';
                                    echo '<tr>';
                                    echo '<td>' . htmlspecialchars($order['order_id']) . '</td>';
                                    echo '<td>' . htmlspecialchars($order['user_id']) . '</td>';
                                    echo '<td>';
                                    $order_items = json_decode($order['details'], true);
                                    foreach ($order_items as $item) {
                                        echo htmlspecialchars($item['item_quantity']) . ' x ' . htmlspecialchars($item['item_name']) . '<br>';
                                    }
                                    echo '</td>';
                                    echo '<td>' . htmlspecialchars($order['order_date']) . '</td>';
                                    echo '<td>RM' . htmlspecialchars($order['total_price']) . '</td>';
                                    echo '<td>' . htmlspecialchars($order['shipping_address']) . '</td>';
                                    echo '<td>';
                                    echo '<select name="status" required>';
                                    echo '<option value="pending"' . ($order['status'] == 'pending' ? ' selected' : '') . '>Pending</option>';
                                    echo '<option value="shipped"' . ($order['status'] == 'shipped' ? ' selected' : '') . '>Shipped</option>';
                                    echo '<option value="delivered"' . ($order['status'] == 'delivered' ? ' selected' : '') . '>Delivered</option>';
                                    echo '<option value="cancelled"' . ($order['status'] == 'cancelled' ? ' selected' : '') . '>Cancelled</option>';
                                    echo '</select>';
                                    echo '</td>';
                                    echo '<td style="display: flex; justify-content: center;">';
                                    echo '<input type="hidden" name="order_id" value="' . htmlspecialchars($order['order_id']) . '">';
                                    echo '<button type="submit" name="update_order">Update</button>';
                                    echo '</td>';
                                    echo '</tr>';
                                    echo '</form>';
                                }
                            } else {
                                echo '<tr><td colspan="8">No orders found.</td></tr>';
                            }
                            ?>
                        </tbody>
                    </table>
                </div>
            </aside>
        </section>
    </main>

    <!-- Footer Section -->
    <footer>
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
    <script src="administration.js"></script>
</body>
</html>
