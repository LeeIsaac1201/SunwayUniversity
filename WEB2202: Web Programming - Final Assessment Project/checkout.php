<?php
// Session management
ini_set('session.gc_maxlifetime', 3600);
session_set_cookie_params(3600);
session_start();

// Check session expiration
if (isset($_SESSION['LAST_ACTIVITY']) && (time() - $_SESSION['LAST_ACTIVITY'] > 3600)) {
    session_unset();
    session_destroy();
    header('Location: login.php');
    exit();
}
$_SESSION['LAST_ACTIVITY'] = time();

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    $_SESSION['redirect_to'] = 'checkout.php';
    header('Location: login.php');
    exit();
}

// Include database connection
include('includes/db_connect.php');

// Fetch user details
$user_id = $_SESSION['user_id'];
$sql = "SELECT first_name, last_name, street, district, city, postal_code, state, phone_number, email FROM users WHERE user_id = ?";
$stmt = $pdo->prepare($sql);
$stmt->execute([$user_id]);
$userDetails = $stmt->fetch(PDO::FETCH_ASSOC);

// Fetch cart items
$cart_items = isset($_SESSION['cart']) ? $_SESSION['cart'] : [];

// Define functions

// Function: addRewardPoints
function addRewardPoints($user_id, $points) {
    global $pdo;
    $sql = "UPDATE users SET point_balance = point_balance + ? WHERE user_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$points, $user_id]);
}

// Function: applyPromoCode
function applyPromoCode($promo_code) {
    global $pdo;
    $sql = "SELECT * FROM promotional_codes WHERE code = ? AND expiration_date >= CURDATE() AND (usage_limit > times_used OR usage_limit = 0)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$promo_code]);
    return $stmt->fetch(PDO::FETCH_ASSOC);
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['promo_code'])) {
        $promo_code = $_POST['promo_code'];
        $promo = applyPromoCode($promo_code);
        if ($promo) {
            $discount_percentage = $promo['discount_percentage'];
            $total_price = 0;
            foreach ($cart_items as $item) {
                $total_price += $item['item_price'] * $item['item_quantity'];
            }
            $discount_amount = ($total_price * $discount_percentage) / 100;
            $total_price -= $discount_amount;

            // Update promo code usage
            $update_promo_sql = "UPDATE promotional_codes SET times_used = times_used + 1 WHERE id = ?";
            $update_promo_stmt = $pdo->prepare($update_promo_sql);
            $update_promo_stmt->execute([$promo['id']]);

            $_SESSION['discount_amount'] = $discount_amount;
            $_SESSION['total_price'] = $total_price;
            $_SESSION['promo_code'] = $promo_code;
        } else {
            $_SESSION['promo_error'] = "Invalid or expired promo code.";
        }
    } elseif (isset($_POST['payment_method']) && $_POST['payment_method'] == 'credit_card') {
        $total_amount_spent = $_SESSION['total_price'] ?? 0;
        foreach ($cart_items as $item) {
            $total_amount_spent += $item['item_price'] * $item['item_quantity'];
        }

        $points = $total_amount_spent;
        addRewardPoints($user_id, $points);

        header('Location: confirmation.php');
        exit();
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Document head -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout - Simply Fresh Foods</title>
    <link rel="stylesheet" href="checkout.css">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <!-- Header section -->
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
    <!-- Main content section -->
    <main class="main-content">
        <h2 class="cart-title">Checkout</h2>
        <div class="cart-container">
            <!-- Cart summary section -->
            <section class="cart-summary">
                <h3>Order Summary</h3>
                <ul class="item-list">
                    <?php
                    $total_price = 0;
                    if (count($cart_items) > 0) {
                        foreach ($cart_items as $index => $item) {
                            $total_price += $item['item_price'] * $item['item_quantity'];
                            echo '<li class="cart-item" data-index="' . $index . '">';
                            echo '<img src="' . $item['item_image'] . '" alt="' . $item['item_name'] . '" class="cart-item-image">';
                            echo '<div class="cart-item-details">';
                            echo '<h4 class="item-name">' . $item['item_name'] . '</h4>';
                            echo '<p class="item-price">Price: RM' . number_format($item['item_price'], 2) . '</p>';
                            echo '<div class="quantity-controls">';
                            echo '<form action="checkout.php" method="post" class="update-form">';
                            echo '<input type="hidden" name="item_id" value="' . $item['item_id'] . '">';
                            echo '<input type="hidden" name="action" value="update">';
                            echo '<button type="button" class="quantity-btn" onclick="decreaseQuantity(this, ' . $index . ', ' . $item['item_price'] . ')">-</button>';
                            echo '<input type="number" name="item_quantity" class="quantity-input" value="' . $item['item_quantity'] . '" min="1" readonly>';
                            echo '<button type="button" class="quantity-btn" onclick="increaseQuantity(this, ' . $index . ', ' . $item['item_price'] . ')">+</button>';
                            echo '</form>';
                            echo '</div>';
                            echo '<p class="item-subtotal">Subtotal: RM<span>' . number_format($item['item_price'] * $item['item_quantity'], 2) . '</span></p>';
                            echo '<button class="remove-item-btn" onclick="removeItem(' . $index . ')">Remove</button>';
                            echo '</div>';
                            echo '</li>';
                        }
                    } else {
                        echo '<p>Your cart is empty.</p>';
                    }
                    ?>
                </ul>
            </section>
            <!-- Shipping details section -->
            <section class="shipping-details">
                <h3>Shipping Details</h3>
                <form id="shipping-form">
                    <label for="full-name">Full Name:</label>
                    <input type="text" id="full-name" name="full-name" value="<?php echo $userDetails['first_name'] . ' ' . $userDetails['last_name']; ?>">

                    <label for="contact-number">Contact Number:</label>
                    <input type="text" id="contact-number" name="contact-number" value="<?php echo $userDetails['phone_number']; ?>">
                    
                    <label for="address">Address:</label>
                    <input type="text" id="address" name="address" value="<?php echo $userDetails['street'] . ', ' . $userDetails['district']; ?>">
                    
                    <label for="city">City:</label>
                    <input type="text" id="city" name="city" value="<?php echo $userDetails['city']; ?>">
                    
                    <label for="zip">Postal Code:</label>
                    <input type="text" id="zip" name="zip" value="<?php echo $userDetails['postal_code']; ?>">
                    
                    <label for="state">State:</label>
                    <input type="text" id="state" name="state" value="<?php echo $userDetails['state']; ?>">
                    
                    <label for="country">Country:</label>
                    <input type="text" id="country" name="country" value="Malaysia">
                </form>
            </section>
            <!-- Promo code & payment details section -->
            <div class="promo-payment-container">
                <section class="promo-payment">
                    <h3>Payment Details</h3>
                        <div class="total-price-container">
                            <h5>Total Price: <span id="total-price">RM<?php echo number_format($total_price, 2); ?></span></h5>
                                <?php if (isset($_SESSION['discount_amount'])): ?>
                                    <h5 class="discount-info">Discount Applied: RM<?php echo number_format($_SESSION['discount_amount'], 2); ?></h5>
                                    <h5 class="new-total-price">New Total Price: RM<?php echo number_format($_SESSION['total_price'], 2); ?></h5>
                                <?php endif; ?>
                        </div>
                    <div class="promo-code">
                        <h4>Promo Code</h4>
                        <form id="promo-form" action="checkout.php" method="post">
                            <label for="promo-code">Enter Promo Code:</label>
                            <input type="text" id="promo-code" name="promo_code" placeholder="E.g., SAVE10">
                            <button type="submit">Apply</button>
                        </form>
                        <?php if (isset($_SESSION['promo_error'])): ?>
                            <p class="error"><?php echo $_SESSION['promo_error']; unset($_SESSION['promo_error']); ?></p>
                        <?php endif; ?>
                    </div>
                    <div class="payment-method">
                        <h4>Payment Method</h4>
                        <form action="checkout.php" method="post">
                            <label for="payment_method">Select Payment Method:</label>
                            <select id="payment_method" name="payment_method" required>
                                <option value="credit_card">Credit Card</option>
                            </select>
                            <div class="column">
                                <div class="section">
                                    <h3>Payment Information</h3>
                                    <label for="card-name">Name on Card:</label>
                                    <input type="text" id="card-name" name="card-name" required>
                                    
                                    <label for="card-number">Card Number:</label>
                                    <input type="text" id="card-number" name="card-number" required>
                                    
                                    <label for="exp-date">Expiration Date:</label>
                                    <input type="text" id="exp-date" name="exp-date" placeholder="MM/YY" required>
                                    
                                    <label for="cvv">CVV:</label>
                                    <input type="text" id="cvv" name="cvv" required>
                                </div>
                            </div>
                            <button type="submit">Proceed to Payment</button>
                        </form>
                    </div>
                </section>
            </div>
        </div>
    </main>
    <!-- Footer section -->
    <footer>
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
    <script src="checkout.js"></script>
</body>
</html>
