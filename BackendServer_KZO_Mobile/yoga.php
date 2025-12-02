<?php
header('Content-Type: application/json');
require_once 'database.php';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = $_GET['user_id'] ?? null;
    if (!$user_id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'user_id required']);
        exit;
    }
    $stmt = $conn->prepare("SELECT * FROM yoga WHERE user_id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $data = [];
    while ($row = $result->fetch_assoc()) {
        $data[] = $row;
    }
    echo json_encode(['success' => true, 'data' => $data]);
    $stmt->close();
    $conn->close();
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);
    $required = ['user_id', 'duration_minutes'];
    foreach ($required as $field) {
        if (empty($input[$field])) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => "$field is required"]);
            exit;
        }
    }
    $intensity_level = $input['intensity_level'] ?? 'Moderate';
    $calorie_burned_kcal = $input['calorie_burned_kcal'] ?? null;
    $stmt = $conn->prepare("INSERT INTO yoga (user_id, duration_minutes, intensity_level, calorie_burned_kcal) VALUES (?, ?, ?, ?)");
    $stmt->bind_param(
        "idsd",
        $input['user_id'],
        $input['duration_minutes'],
        $intensity_level,
        $calorie_burned_kcal
    );
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Yoga record added']);
    } else {
        http_response_code(500);
        echo json_encode(['success' => false, 'message' => 'Insert failed']);
    }
    $stmt->close();
    $conn->close();
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $input = json_decode(file_get_contents('php://input'), true);
    $id = $input['id'] ?? null;
    if (!$id) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'id required']);
        exit;
    }
    $stmt = $conn->prepare("DELETE FROM yoga WHERE id = ?");
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