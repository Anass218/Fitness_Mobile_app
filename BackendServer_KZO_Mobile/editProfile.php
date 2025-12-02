<?php
header('Content-Type: application/json');
include 'database.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $data = json_decode(file_get_contents("php://input"), true);
    $id = $data['id'] ?? null;
    $name = $data['name'] ?? null;
    $email = $data['email'] ?? null;
    $weight = $data['weight'] ?? null;
    $height = $data['height'] ?? null;

    // Check required fields
    if (!$id || !$name || !$email || $weight === null || $height === null) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "All fields are required."]);
        $conn->close();
        exit;
    }

    // Validate email format
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Invalid email format."]);
        $conn->close();
        exit;
    }

    // Validate numeric values
    if (!is_numeric($weight) || floatval($weight) <= 0) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Weight must be a positive number."]);
        $conn->close();
        exit;
    }
    if (!is_numeric($height) || floatval($height) <= 0) {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Height must be a positive number."]);
        $conn->close();
        exit;
    }

    // Check for existing email (exclude current user)
    $stmt = $conn->prepare("SELECT id FROM user WHERE email = ? AND id != ?");
    $stmt->bind_param("si", $email, $id);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Email already exists."]);
        $stmt->close();
        $conn->close();
        exit;
    }
    $stmt->close();

    // Check for existing username (exclude current user)
    $stmt = $conn->prepare("SELECT id FROM user WHERE username = ? AND id != ?");
    $stmt->bind_param("si", $name, $id);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Username already in use."]);
        $stmt->close();
        $conn->close();
        exit;
    }
    $stmt->close();

    $sql = "UPDATE user SET username=?, email=?, weight=?, height=? WHERE id=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssddi", $name, $email, $weight, $height, $id);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Profile updated."]);
    } else {
        echo json_encode(["status" => "error", "message" => "Update failed: " . $stmt->error]);
    }
    $stmt->close();
}
$conn->close();
?>
