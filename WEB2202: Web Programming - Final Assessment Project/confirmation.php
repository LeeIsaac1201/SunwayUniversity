<?php
// Start the session and set session lifetime parameters
ini_set('session.gc_maxlifetime', 3600);
session_set_cookie_params(3600);
session_start();

// Check if the user is logged in; if not, redirect to the login page
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Include the database connection file
include('includes/db_connect.php');

// Fetch user details from the database using the user_id stored in session
$user_id = $_SESSION['user_id'];
$sql = "SELECT first_name, last_name, street, district, city, postal_code, state, phone_number, email, point_balance FROM users WHERE user_id = ?";
$stmt = $pdo->prepare($sql);
$stmt->execute([$user_id]);
$userDetails = $stmt->fetch(PDO::FETCH_ASSOC);

// Retrieve cart items stored in session, or use an empty array if none exist
$cart_items = isset($_SESSION['cart']) ? $_SESSION['cart'] : [];

// Calculate the total price based on items in the cart
$total_price = 0;
foreach ($cart_items as $item) {
    $total_price += $item['item_price'] * $item['item_quantity'];
}

// If a discounted total price is available in session, use it instead of the calculated total
if (isset($_SESSION['total_price'])) {
    $total_price = $_SESSION['total_price'];
}

// Prepare order details by encoding the cart items to JSON
$order_details = json_encode($cart_items);
// Build the shipping address using user's details
$shipping_address = $userDetails['street'] . ', ' . $userDetails['district'] . ', ' . $userDetails['city'] . ', ' . $userDetails['postal_code'] . ', ' . $userDetails['state'] . ', Malaysia';

// Insert the order into the orders table with a default status of 'pending'
$order_sql = "INSERT INTO orders (user_id, details, total_price, shipping_address, status) VALUES (?, ?, ?, ?, 'pending')";
$order_stmt = $pdo->prepare($order_sql);
$order_stmt->execute([$user_id, $order_details, $total_price, $shipping_address]);

// Fetch the order just inserted using the last inserted order ID
$order_id = $pdo->lastInsertId();
$order_query = "SELECT * FROM orders WHERE order_id = ?";
$order_stmt = $pdo->prepare($order_query);
$order_stmt->execute([$order_id]);
$order = $order_stmt->fetch(PDO::FETCH_ASSOC);

// Clear the cart from session after order confirmation
unset($_SESSION['cart']);

// Update the user's points: 1 point per RM1 spent is added to their existing point balance
$points_earned = $total_price; // 1 point per RM1 spent
$new_point_balance = $userDetails['point_balance'] + $points_earned;
$update_points_sql = "UPDATE users SET point_balance = ? WHERE user_id = ?";
$update_points_stmt = $pdo->prepare($update_points_sql);
$update_points_stmt->execute([$new_point_balance, $user_id]);

// Format the order details for display by decoding the JSON order details
$order_items = json_decode($order['details'], true);
$formatted_details = [];
foreach ($order_items as $index => $item) {
    $quantity = $item['item_quantity'];
    $name = $item['item_name'];
    // Simple pluralisation: if quantity > 1, add an "s" or "es" based on word ending
    if ($quantity > 1) {
        if (preg_match('/(s|sh|ch|x|z)$/', $name)) {
            $name .= 'es';
        } else {
            $name .= 's';
        }
    }
    // Add "and" before the last item if there is more than one item
    if ($index == count($order_items) - 1 && $index != 0) {
        $formatted_details[] = "and $quantity $name";
    } else {
        $formatted_details[] = "$quantity $name";
    }
}
// Combine all formatted details into a single string separated by commas
$formatted_details = implode(', ', $formatted_details);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmation - Simply Fresh Foods</title>
    <link rel="stylesheet" href="confirmation.css">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <header>
        <div class="header-container">
            <!-- Site logo and title -->
            <div class="logo-title">
                <img src="images/groceries.png" alt="Simply Fresh Foods Logo" class="logo">
                <h1>Simply Fresh Foods</h1>
            </div>
            <!-- Navigation links -->
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
    <main class="main-content">
        <!-- Page title -->
        <h2 class="cart-title">Order Confirmation</h2>
        <div class="cart-container">
            <!-- Order summary section -->
            <section class="order-summary">
                <h3>Order Summary</h3>
                <ul class="item-list">
                    <?php
                    // Loop through each item in the cart to display order summary details
                    foreach ($cart_items as $item) {
                        echo '<li class="cart-item">';
                        echo '<img src="' . $item['item_image'] . '" alt="' . $item['item_name'] . '" class="cart-item-image">';
                        echo '<div class="cart-item-details">';
                        echo '<h4 class="item-name">' . $item['item_name'] . '</h4>';
                        echo '<p class="item-price">Price: RM' . number_format($item['item_price'], 2) . '</p>';
                        echo '<p class="item-quantity">Quantity: ' . $item['item_quantity'] . '</p>';
                        echo '<p class="item-subtotal">Subtotal: RM' . number_format($item['item_price'] * $item['item_quantity'], 2) . '</p>';
                        echo '</div>';
                        echo '</li>';
                    }
                    ?>
                </ul>
                <!-- Display total price -->
                <div class="total-price-container">
                    <h4>Total Price: RM<span id="total-price"><?php echo number_format($total_price, 2); ?></span></h4>
                </div>
            </section>
            
            <!-- Combined container for order details and shipping details -->
            <section class="order-shipping-container">
                <!-- Order details section -->
                <div class="order-details">
                    <h3 class="order-details-title">Order Details</h3>
                    <div class="order-details-content">
                        <p><strong>Order ID:</strong> <?php echo $order['order_id']; ?></p>
                        <p><strong>Details:</strong> <?php echo $formatted_details; ?></p>
                        <p><strong>Order Date:</strong> <?php echo $order['order_date']; ?></p>
                        <p><strong>Status:</strong> <?php echo $order['status']; ?></p>
                    </div>
                </div>
                <hr class="divider">
                <!-- Shipping details section -->
                <div class="shipping-details">
                    <h3>Shipping Details</h3>
                    <div class="shipping-details-content">
                        <p><strong>Full Name:</strong> <?php echo $userDetails['first_name'] . ' ' . $userDetails['last_name']; ?></p>
                        <p><strong>Contact Number:</strong> <?php echo $userDetails['phone_number']; ?></p>
                        <p><strong>Address:</strong> <?php echo $userDetails['street'] . ', ' . $userDetails['district']; ?></p>
                        <p><strong>City:</strong> <?php echo $userDetails['city']; ?></p>
                        <p><strong>Postal Code:</strong> <?php echo $userDetails['postal_code']; ?></p>
                        <p><strong>State:</strong> <?php echo $userDetails['state']; ?></p>
                        <p><strong>Country:</strong> Malaysia</p>
                    </div>
                </div>
            </section>
        </div>
    </main>
    <footer>
        <div class="footer-container">
            <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
        </div>
    </footer>
    <!-- Notification for order confirmation -->
    <div class="notification" id="notification">
        <span id="notification-message">Your payment is successful and we have received your order!</span>
        <span class="close-btn" onclick="closeNotification()">&times;</span>
    </div>
    <script src="confirmation.js"></script>
</body>
</html>
