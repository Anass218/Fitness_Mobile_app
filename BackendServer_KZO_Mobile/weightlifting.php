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
    $stmt = $conn->prepare("SELECT * FROM weightlifting WHERE user_id = ?");
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
    $sets = $input['sets'] ?? null;
    $reps_per_set = $input['reps_per_set'] ?? null;
    $weight_kg_per_rep = $input['weight_kg_per_rep'] ?? null;
    $calorie_burned_kcal = $input['calorie_burned_kcal'] ?? null;
    $stmt = $conn->prepare("INSERT INTO weightlifting (user_id, duration_minutes, sets, reps_per_set, weight_kg_per_rep, calorie_burned_kcal) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->bind_param(
        "idiidd",
        $input['user_id'],
        $input['duration_minutes'],
        $sets,
        $reps_per_set,
        $weight_kg_per_rep,
        $calorie_burned_kcal
    );
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Weightlifting record added']);
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
    $stmt = $conn->prepare("DELETE FROM weightlifting WHERE id = ?");
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