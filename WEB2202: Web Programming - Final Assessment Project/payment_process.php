<?php
session_start();

// Check if user is logged in
if (!isset($_SESSION['user_id'])) {
    header('Location: login.php');
    exit();
}

// Include database connection
include('db_connect.php');

// Function to validate card details (basic validation)
function validateCardDetails($cardName, $cardNumber, $expDate, $cvv) {
    if (empty($cardName) || empty($cardNumber) || empty($expDate) || empty($cvv)) {
        return false;
    }
    if (!preg_match('/^[0-9]{16}$/', $cardNumber)) {
        return false;
    }
    if (!preg_match('/^(0[1-9]|1[0-2])\/[0-9]{2}$/', $expDate)) {
        return false;
    }
    if (!preg_match('/^[0-9]{3,4}$/', $cvv)) {
        return false;
    }
    return true;
}

// Handle form submission
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $cardName = $_POST['card-name'];
    $cardNumber = $_POST['card-number'];
    $expDate = $_POST['exp-date'];
    $cvv = $_POST['cvv'];

    // Validate card details
    if (!validateCardDetails($cardName, $cardNumber, $expDate, $cvv)) {
        $_SESSION['error'] = 'Invalid card details. Please try again.';
        header('Location: ../checkout.php');
        exit();
    }

    // Process payment (dummy processing for example)
    // In a real application, you would integrate with a payment gateway here
    $paymentSuccess = true; // Assume payment is successful

    if ($paymentSuccess) {
        // Clear cart
        unset($_SESSION['cart']);

        // Redirect to confirmation page
        header('Location: ../confirmation.php');
        exit();
    } else {
        $_SESSION['error'] = 'Payment failed. Please try again.';
        header('Location: ../checkout.php');
        exit();
    }
} else {
    // Redirect to checkout page if the request method is not POST
    header('Location: ../checkout.php');
    exit();
}
?>
