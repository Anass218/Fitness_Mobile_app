<?php
header('Content-Type: application/json');
require_once 'database.php';

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(['status' => 'error', 'message' => 'Method not allowed']);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
$user_id = $data['id'] ?? null;
$token = $data['token'] ?? null;

if (!$user_id || !$token) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'User ID and token are required']);
    exit;
}

$stmt = $conn->prepare("DELETE FROM session_keys WHERE user_id = ? AND session_key = ?");
$stmt->bind_param("is", $user_id, $token);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Logged out."]);
} else {
    echo json_encode(["status" => "error", "message" => "Logout failed."]);
}

$stmt->close();
$conn->close();
?>
