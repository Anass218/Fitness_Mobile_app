<?php
header('Content-Type: application/json');
require_once 'database.php';

// Get all running records for a user
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = $_GET['user_id'] ?? null;
    if (!$user_id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'user_id required']);
        exit;
    }
    $stmt = $conn->prepare("SELECT * FROM running WHERE user_id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $runs = [];
    while ($row = $result->fetch_assoc()) {
        $runs[] = $row;
    }
    echo json_encode(['success' => true, 'data' => $runs]);
    $stmt->close();
    $conn->close();
    exit;
}

// Add a running record
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);
    $required = ['user_id', 'duration_minutes', 'distance_km'];
    foreach ($required as $field) {
        if (empty($input[$field])) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => "$field is required"]);
            exit;
        }
    }
    $stmt = $conn->prepare("INSERT INTO running (user_id, duration_minutes, distance_km, calorie_burned_kcal) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("iidd", $input['user_id'], $input['duration_minutes'], $input['distance_km'], $input['calorie_burned_kcal']);
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Running record added']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Insert failed']);
    }
    $stmt->close();
    $conn->close();
    exit;
}

// Delete a running record by id
if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $input = json_decode(file_get_contents('php://input'), true);
    $id = $input['id'] ?? null;
    if (!$id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'id required']);
        exit;
    }
    $stmt = $conn->prepare("DELETE FROM running WHERE id = ?");
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Deleted']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Delete failed']);
    }
    $stmt->close();
    $conn->close();
    exit;
}

echo json_encode(['success' => false, 'message' => 'Invalid request']);
$conn->close();
?>