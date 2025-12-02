<?php
header('Content-Type: application/json');
require_once 'database.php';

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Method not allowed']);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
$email = $data['email'] ?? null;
$password = $data['password'] ?? null;

if (!$email || !$password) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Email and password are required']);
    exit;
}

$stmt = $conn->prepare("SELECT id, username as name, email, password, height, weight FROM user WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $user = $result->fetch_assoc();
    if (password_verify($password, $user['password'])) {
        // Generate a session token
        $token = bin2hex(random_bytes(32));
        // Store token in session_keys
        $insert = $conn->prepare("INSERT INTO session_keys (user_id, session_key) VALUES (?, ?)");
        $insert->bind_param("is", $user['id'], $token);
        $insert->execute();
        $insert->close();

        echo json_encode([
            "status" => "success",
            "token" => $token,
            "user" => [
                "id" => $user['id'],
                "name" => $user['name'],
                "email" => $user['email'],
                "height" => $user['height'],
                "weight" => $user['weight']
            ],
            "message" => "Login successful"
        ]);
    } else {
        http_response_code(401);
        echo json_encode(["status" => "error", "message" => "Invalid credentials"]);
    }
} else {
    http_response_code(401);
    echo json_encode(["status" => "error", "message" => "Invalid credentials"]);
}

$stmt->close();
$conn->close();
?>