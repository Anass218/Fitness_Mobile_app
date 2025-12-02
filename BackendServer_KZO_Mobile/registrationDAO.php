<?php
header('Content-Type: application/json');
require_once 'database.php';

// Parse JSON input
$input = json_decode(file_get_contents('php://input'), true);

// Validate required fields
$required = ['username', 'email', 'password', 'weight', 'height'];
foreach ($required as $field) {
    if (empty($input[$field])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => "$field is required"]);
        exit;
    }
}

// Validate email format
if (!filter_var($input['email'], FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid email format']);
    exit;
}

// Validate numeric values
if (!is_numeric($input['weight']) || floatval($input['weight']) <= 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Weight must be a positive number']);
    exit;
}
if (!is_numeric($input['height']) || floatval($input['height']) <= 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Height must be a positive number']);
    exit;
}

// Check for existing email
$stmt = $conn->prepare("SELECT id FROM user WHERE email = ?");
$stmt->bind_param("s", $input['email']);
$stmt->execute();
$stmt->store_result();
if ($stmt->num_rows > 0) {
    echo json_encode(['success' => false, 'message' => 'Email already exists']);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

// Check for existing username
$stmt = $conn->prepare("SELECT id FROM user WHERE username = ?");
$stmt->bind_param("s", $input['username']);
$stmt->execute();
$stmt->store_result();
if ($stmt->num_rows > 0) {
    echo json_encode(['success' => false, 'message' => 'Username already in use']);
    $stmt->close();
    $conn->close();
    exit;
}
$stmt->close();

// Hash password
$hashedPassword = password_hash($input['password'], PASSWORD_DEFAULT);

// Insert new user
$stmt = $conn->prepare("INSERT INTO user (username, email, password, weight, height) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param(
    "sssdd",
    $input['username'],
    $input['email'],
    $hashedPassword,
    $input['weight'],
    $input['height']
);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Registration successful']);
} else {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Registration failed']);
}
$stmt->close();
$conn->close();
?>