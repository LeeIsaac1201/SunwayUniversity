<?php
session_start();

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "simply_fresh_foods";

$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch user details
$user_id = $_SESSION['user_id'];
$user_query = "SELECT * FROM users WHERE user_id = ?";
$user_stmt = $conn->prepare($user_query);
$user_stmt->bind_param("i", $user_id);
$user_stmt->execute();
$user_result = $user_stmt->get_result();
$user = $user_result->fetch_assoc();

// Fetch user orders
$order_query = "SELECT * FROM orders WHERE user_id = ?";
$order_stmt = $conn->prepare($order_query);
$order_stmt->bind_param("i", $user_id);
$order_stmt->execute();
$order_result = $order_stmt->get_result();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Update user details
    $first_name = $_POST['first_name'];
    $last_name = $_POST['last_name'];
    $sex = $_POST['sex'];
    $birthdate = $_POST['birthdate'];
    $email = $_POST['email'];
    $password = $_POST['password'];
    $phone_number = $_POST['phone_number'];
    $street = $_POST['street'];
    $district = $_POST['district'];
    $city = $_POST['city'];
    $postal_code = $_POST['postal_code'];
    $state = $_POST['state'];
    $country = $_POST['country'];

    $update_query = "UPDATE users SET 
        first_name = ?, 
        last_name = ?, 
        sex = ?, 
        birthdate = ?, 
        email = ?, 
        password = ?, 
        phone_number = ?, 
        street = ?, 
        district = ?, 
        city = ?, 
        postal_code = ?, 
        state = ?, 
        country = ? 
        WHERE user_id = ?";
    $update_stmt = $conn->prepare($update_query);
    $update_stmt->bind_param("sssssssssssssi", $first_name, $last_name, $sex, $birthdate, $email, $password, $phone_number, $street, $district, $city, $postal_code, $state, $country, $user_id);
    $update_stmt->execute();
    header("Location: account.php");
    exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Account - Simply Fresh Foods</title>
    <link rel="stylesheet" href="account.css">
    <link href="https://fonts.googleapis.com/css2?family=Pacifico&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css"> <!-- Font Awesome CDN -->
</head>
<body>
    <!-- Header Section -->
    <header>
        <div class="header-container">
            <div class="logo-title">
                <img src="images/groceries.png" alt="Simply Fresh Foods Logo" class="logo">
                <h1 class="pacifico-font">Simply Fresh Foods</h1>
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

    <!-- Main Content Section -->
    <main>
        <h1>Account Details</h1>
        <div class="account-container">
            <!-- Order History Section -->
            <div class="order-history">
                <h3>Your Orders</h3>
                <ul>
                    <?php while ($order = $order_result->fetch_assoc()): ?>
                        <?php
                        // Format order details for display
                        $order_items = json_decode($order['details'], true);
                        $formatted_details = [];
                        foreach ($order_items as $index => $item) {
                            $quantity = $item['item_quantity'];
                            $name = $item['item_name'];
                            if ($quantity > 1) {
                                if (preg_match('/(s|sh|ch|x|z)$/', $name)) {
                                    $name .= 'es';
                                } else {
                                    $name .= 's';
                                }
                            }
                            if ($index == count($order_items) - 1 && $index != 0) {
                                $formatted_details[] = "and $quantity $name";
                            } else {
                                $formatted_details[] = "$quantity $name";
                            }
                        }
                        $formatted_details = implode(', ', $formatted_details);
                        ?>
                        <li>Order ID: <?php echo $order['order_id']; ?> - <?php echo $formatted_details; ?></li>
                    <?php endwhile; ?>
                </ul>
            </div>

            <!-- Account Details Section -->
            <div class="account-details">
                <h3>Personal Information</h3>
                <form method="post" action="account.php">
                    <div class="form-sections">
                        <!-- Basic Information Section -->
                        <div class="form-section">
                            <h4>Basic Information</h4>
                            <div class="form-group">
                                <label>First Name:</label>
                                <input type="text" name="first_name" value="<?php echo $user['first_name']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Last Name:</label>
                                <input type="text" name="last_name" value="<?php echo $user['last_name']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Sex:</label>
                                <select name="sex" required>
                                    <option value="male" <?php if ($user['sex'] == 'male') echo 'selected'; ?>>Male</option>
                                    <option value="female" <?php if ($user['sex'] == 'female') echo 'selected'; ?>>Female</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Birthdate:</label>
                                <input type="date" name="birthdate" value="<?php echo $user['birthdate']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Phone Number:</label>
                                <input type="text" name="phone_number" value="<?php echo $user['phone_number']; ?>" required>
                            </div>
                        </div>

                        <!-- Address Information Section -->
                        <div class="form-section">
                            <h4>Address Information</h4>
                            <div class="form-group">
                                <label>Street:</label>
                                <input type="text" name="street" value="<?php echo $user['street']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>District:</label>
                                <input type="text" name="district" value="<?php echo $user['district']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>City:</label>
                                <input type="text" name="city" value="<?php echo $user['city']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Postal Code:</label>
                                <input type="text" name="postal_code" value="<?php echo $user['postal_code']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>State:</label>
                                <input type="text" name="state" value="<?php echo $user['state']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Country:</label>
                                <input type="text" name="country" value="<?php echo $user['country']; ?>" required>
                            </div>
                        </div>

                        <!-- Account Information Section -->
                        <div class="form-section">
                            <h4>Account Information</h4>
                            <div class="form-group">
                                <label>Email:</label>
                                <input type="email" name="email" value="<?php echo $user['email']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Password:</label>
                                <input type="password" name="password" value="<?php echo $user['password']; ?>" required>
                            </div>
                            <div class="form-group">
                                <label>Point Balance:</label>
                                <input type="text" value="<?php echo $user['point_balance']; ?>" readonly>
                            </div>
                        </div>
                    </div>
                    <button type="submit">Update</button>
                </form>
            </div>
        </div>
    </main>

    <!-- Footer Section -->
    <footer>
        <p>&copy; 2025 Simply Fresh Foods. All rights reserved.</p>
    </footer>
</body>
</html>

<?php
// Close the database connection
$conn->close();
?>
