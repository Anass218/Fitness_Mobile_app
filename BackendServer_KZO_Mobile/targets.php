<?php
header('Content-Type: application/json');
require_once 'database.php';

// Get targets for a user
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = $_GET['user_id'] ?? null;
    if (!$user_id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'user_id required']);
        exit;
    }
    $stmt = $conn->prepare("SELECT running_target, cycling_target, swimming_target, weightlifting_target, yoga_target, target_start_date, target_end_date FROM user WHERE id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $targets = $result->fetch_assoc();
    echo json_encode(['success' => true, 'data' => $targets]);
    $stmt->close();
    $conn->close();
    exit;
}

// Update targets for a user
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);
    $user_id = $input['user_id'] ?? null;
    if (!$user_id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'user_id required']);
        exit;
    }
    $stmt = $conn->prepare("UPDATE user SET running_target=?, cycling_target=?, swimming_target=?, weightlifting_target=?, yoga_target=?, target_start_date=?, target_end_date=? WHERE id=?");
    $stmt->bind_param(
        "iiiiissi",
        $input['running_target'],
        $input['cycling_target'],
        $input['swimming_target'],
        $input['weightlifting_target'],
        $input['yoga_target'],
        $input['target_start_date'],
        $input['target_end_date'],
        $user_id
    );
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Targets updated']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Update failed']);
    }
    $stmt->close();
    $conn->close();
    exit;
}

echo json_encode(['success' => false, 'message' => 'Invalid request']);
$conn->close();
?>