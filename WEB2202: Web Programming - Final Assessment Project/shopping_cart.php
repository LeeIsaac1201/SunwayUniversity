<?php
session_start();

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Get the action from the form
    $action = isset($_POST['action']) ? $_POST['action'] : '';

    // Get the item details from the form
    $item_id = isset($_POST['item_id']) ? $_POST['item_id'] : '';
    $item_name = isset($_POST['item_name']) ? $_POST['item_name'] : '';
    $item_price = isset($_POST['item_price']) ? $_POST['item_price'] : '';
    $item_image = isset($_POST['item_image']) ? $_POST['item_image'] : '';
    $item_quantity = isset($_POST['item_quantity']) ? $_POST['item_quantity'] : 1;

    // Initialise the cart if it doesn't exist
    if (!isset($_SESSION['cart'])) {
        $_SESSION['cart'] = [];
    }

    if ($action == 'add') {
        // Check if the item is already in the cart
        $item_exists = false;
        foreach ($_SESSION['cart'] as &$cart_item) {
            if ($cart_item['item_id'] == $item_id) {
                // Update the quantity if the item already exists in the cart
                $cart_item['item_quantity'] += $item_quantity;
                $item_exists = true;
                break;
            }
        }

        // Add the item to the cart if it doesn't exist
        if (!$item_exists) {
            $_SESSION['cart'][] = [
                'item_id' => $item_id,
                'item_name' => $item_name,
                'item_price' => $item_price,
                'item_image' => $item_image,
                'item_quantity' => $item_quantity
            ];
        }
    } elseif ($action == 'update') {
        // Update the quantity of the item in the cart
        foreach ($_SESSION['cart'] as &$cart_item) {
            if ($cart_item['item_id'] == $item_id) {
                $cart_item['item_quantity'] = $item_quantity;
                break;
            }
        }
    } elseif ($action == 'remove') {
        // Remove the item from the cart
        foreach ($_SESSION['cart'] as $key => $cart_item) {
            if ($cart_item['item_id'] == $item_id) {
                unset($_SESSION['cart'][$key]);
                break;
            }
        }
    }

    // Return a JSON response indicating success
    echo json_encode(['success' => true]);
    exit();
}

// Display the cart items
$cart_items = isset($_SESSION['cart']) ? $_SESSION['cart'] : [];

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shopping Cart - Simply Fresh Foods</title>
    <link rel="stylesheet" href="shopping_cart.css"> <!-- Link to your CSS file -->
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> <!-- Font Awesome CDN -->
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
    
    <!-- Main content area -->
    <main class="main-content">
        <h2 class="cart-title">Shopping Cart</h2>
        <div class="cart-container">
            <?php if (count($cart_items) > 0): ?>
                <div class="item-grid">
                    <?php foreach ($cart_items as $cart_item): ?>
                        <div class="cart-item">
                            <img src="<?php echo $cart_item['item_image']; ?>" alt="<?php echo $cart_item['item_name']; ?>" class="cart-item-image">
                            <div class="cart-item-details">
                                <h3 class="item-name"><?php echo $cart_item['item_name']; ?></h3>
                                <p class="item-price">Price: RM<?php echo number_format($cart_item['item_price'], 2); ?></p>
                                <div class="quantity-controls">
                                    <form action="shopping_cart.php" method="post" class="update-form">
                                        <input type="hidden" name="item_id" value="<?php echo $cart_item['item_id']; ?>">
                                        <input type="hidden" name="action" value="update">
                                        <button type="button" class="quantity-btn" onclick="decreaseQuantity(this)">-</button>
                                        <input type="number" name="item_quantity" class="quantity-input" value="<?php echo $cart_item['item_quantity']; ?>" min="1" readonly>
                                        <button type="button" class="quantity-btn" onclick="increaseQuantity(this)">+</button>
                                    </form>
                                </div>
                                <p class="item-subtotal">Subtotal: RM<span><?php echo number_format($cart_item['item_price'] * $cart_item['item_quantity'], 2); ?></span></p>
                                <form onsubmit="removeFromCart(event, this)">
                                    <input type="hidden" name="item_id" value="<?php echo $cart_item['item_id']; ?>">
                                    <input type="hidden" name="action" value="remove">
                                    <button type="submit" class="remove-btn">Remove</button>
                                </form>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php else: ?>
                <p>Your cart is empty.</p>
            <?php endif; ?>
        </div>
        <div class="total-price">
            <h3>Total Price: RM<span id="total-price"><?php echo number_format(array_reduce($cart_items, function($carry, $item) {
                return $carry + ($item['item_price'] * $item['item_quantity']);
            }, 0), 2); ?></span></h3>
        </div>
        <input type="hidden" id="cart-items" value='<?php echo json_encode($cart_items); ?>'>
        <button onclick="proceedToCheckout()" class="checkout-btn">Proceed to Checkout</button>
    </main>
    
    <!-- Footer section -->
    <footer>
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
    
    <!-- Notification element -->
    <div class="notification" id="notification">
        <span id="notification-message"></span>
        <span class="close-btn" onclick="closeNotification()">&times;</span>
    </div>
    
    <script src="shopping_cart.js"></script>
</body>
</html>
