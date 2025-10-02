<?php
session_start();

if (!file_exists('includes/db_connect.php')) {
    die('Error: db_connect.php file not found.');
}

include 'includes/db_connect.php'; // Include your database connection file

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $first_name = trim($_POST['first_name']);
    $last_name = trim($_POST['last_name']);
    $sex = trim($_POST['sex']);
    $birthdate = trim($_POST['birthdate']);
    $phone_number = trim($_POST['phone_number']);
    $street = trim($_POST['street']);
    $district = trim($_POST['district']);
    $city = trim($_POST['city']);
    $postal_code = trim($_POST['postal_code']);
    $state = trim($_POST['state']);
    $country = trim($_POST['country']);
    $email = trim($_POST['email']);
    $password = trim($_POST['password']);
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Check if the email already exists
    $stmt = $pdo->prepare('SELECT user_id FROM users WHERE email = ?');
    $stmt->execute([$email]);

    if ($stmt->rowCount() > 0) {
        $_SESSION['error'] = 'Email already registered.';
        header('Location: register.php');
        exit();
    }

    // Insert the user into the database
    $stmt = $pdo->prepare('INSERT INTO users (first_name, last_name, sex, birthdate, phone_number, street, district, city, postal_code, state, country, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)');
    $stmt->execute([$first_name, $last_name, $sex, $birthdate, $phone_number, $street, $district, $city, $postal_code, $state, $country, $email, $hashed_password]);

    // Retrieve the user_id of the newly inserted user
    $user_id = $pdo->lastInsertId();

    $_SESSION['user_id'] = $user_id;
    $_SESSION['success'] = 'Registration successful. Please log in.';
    header('Location: homepage.php');
    exit();
} else {
    // Redirect to the registration page if the request method is not POST
    header('Location: register.php');
    exit();
}
?>
