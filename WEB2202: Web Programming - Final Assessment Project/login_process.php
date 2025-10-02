<?php
session_start();

// Check if the database connection file exists
if (!file_exists('includes/db_connect.php')) {
    die('Error: db_connect.php file not found.');
}

include 'includes/db_connect.php';

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = trim($_POST['email']);
    $password = trim($_POST['password']);

    // Validate input fields
    if (empty($email) || empty($password)) {
        $_SESSION['error'] = 'Please fill in both fields.';
        header('Location: login.php');
        exit();
    }

    // Prepare and execute the query using PHP Data Objects (PDO)
    $stmt = $pdo->prepare('SELECT user_id, password, role FROM users WHERE email = ?');
    $stmt->execute([$email]);

    // Check if a user with the provided email exists
    if ($stmt->rowCount() > 0) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        // Verify the password
        if (password_verify($password, $user['password'])) {
            $_SESSION['user_id'] = $user['user_id'];
            $_SESSION['role'] = $user['role'];

            // Check if the user is an administrator
            if ($user['role'] === 'admin') {
                $_SESSION['is_admin'] = true;
                header('Location: administration.php');
            } else {
                $_SESSION['is_admin'] = false;
                // Redirect to the originally requested page or homepage
                $redirect_to = isset($_SESSION['redirect_to']) ? $_SESSION['redirect_to'] : 'homepage.php';
                unset($_SESSION['redirect_to']);
                header('Location: ' . $redirect_to);
            }
            exit();
        } else {
            $_SESSION['error'] = 'Invalid password.';
        }
    } else {
        $_SESSION['error'] = 'No user found with that email address.';
    }

    // Redirect back to the login page with an error message
    header('Location: login.php');
    exit();
} else {
    // Redirect to the login page if the request method is not POST
    header('Location: login.php');
    exit();
}
?>
