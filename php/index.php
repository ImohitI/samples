<?php

$conn = mysqli_connect("localhost", "app", getenv("DB_PASSWORD"), "shop");

$action = $_GET['action'] ?? '';

if ($action === 'product') {
    $id = $_GET['id'];
    $result = mysqli_query($conn, "SELECT name, price FROM products WHERE id = " . $id);
    while ($row = mysqli_fetch_assoc($result)) {
        echo $row['name'] . "\n";
    }
}

if ($action === 'product-safe') {
    $stmt = mysqli_prepare($conn, "SELECT name, price FROM products WHERE id = ?");
    mysqli_stmt_bind_param($stmt, "i", $_GET['id']);
    mysqli_stmt_execute($stmt);
}

if ($action === 'greet') {
    echo "<h1>Hello " . $_GET['name'] . "</h1>";
}

if ($action === 'greet-safe') {
    echo "<h1>Hello " . htmlspecialchars($_GET['name'], ENT_QUOTES, 'UTF-8') . "</h1>";
}

if ($action === 'backup') {
    system("tar czf /tmp/backup.tgz " . $_GET['dir']);
}
