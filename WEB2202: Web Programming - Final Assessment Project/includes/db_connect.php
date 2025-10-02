<?php
// includes/db_connect.php

// Database connection parameters
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "simply_fresh_foods";
$charset = 'utf8mb4';

// Data Source Name (DSN) string that contains the information required to connect to the database
$dsn = "mysql:host=$servername;dbname=$dbname;charset=$charset";

// PDO options to configure the PDO connection
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    // Attempt to create a new PDO instance with the provided DSN and options
    $pdo = new PDO($dsn, $username, $password, $options);
} catch (PDOException $e) {
    // If the connection fails, catch the PDOException and display an error message
    die("Connection failed: " . $e->getMessage());
}
?>  
