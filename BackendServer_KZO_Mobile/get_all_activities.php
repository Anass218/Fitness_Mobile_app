<?php
header('Content-Type: application/json');
require_once 'database.php';

$user_id = $_GET['user_id'] ?? null;
if (!$user_id) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'user_id required']);
    exit;
}

$all_activities = [];

// Running activities
$stmt = $conn->prepare("SELECT id, user_id, duration_minutes, distance_km, calorie_burned_kcal, recorded_at, 'running' AS type FROM running WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $row['details'] = "Distance: {$row['distance_km']}km";
    $all_activities[] = $row;
}
$stmt->close();

// Cycling activities
$stmt = $conn->prepare("SELECT id, user_id, duration_minutes, distance_km, calorie_burned_kcal, recorded_at, 'cycling' AS type FROM cycling WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $row['details'] = "Distance: {$row['distance_km']}km";
    $all_activities[] = $row;
}
$stmt->close();

// Swimming activities
$stmt = $conn->prepare("SELECT id, user_id, duration_minutes, laps, calorie_burned_kcal, recorded_at, 'swimming' AS type FROM swimming WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $row['details'] = "Laps: {$row['laps']}";
    $all_activities[] = $row;
}
$stmt->close();

// Weightlifting activities
$stmt = $conn->prepare("SELECT id, user_id, duration_minutes, sets, reps_per_set, weight_kg_per_rep, calorie_burned_kcal, recorded_at, 'weightlifting' AS type FROM weightlifting WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $row['details'] = "Sets: {$row['sets']} x {$row['reps_per_set']} @ {$row['weight_kg_per_rep']}kg";
    $all_activities[] = $row;
}
$stmt->close();

// Yoga activities
$stmt = $conn->prepare("SELECT id, user_id, duration_minutes, intensity_level, calorie_burned_kcal, recorded_at, 'yoga' AS type FROM yoga WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();
while ($row = $result->fetch_assoc()) {
    $row['details'] = "Intensity: {$row['intensity_level']}";
    $all_activities[] = $row;
}
$stmt->close();

// Sort by date (newest first)
usort($all_activities, function($a, $b) {
    return strtotime($b['recorded_at']) - strtotime($a['recorded_at']);
});

echo json_encode([
    'success' => true,
    'data' => $all_activities
]);

$conn->close();
?>