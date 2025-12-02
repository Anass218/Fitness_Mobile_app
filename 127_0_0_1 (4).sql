-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 25, 2025 at 08:44 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `fitness_kzo`
--
CREATE DATABASE IF NOT EXISTS `fitness_kzo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `fitness_kzo`;

-- --------------------------------------------------------

--
-- Table structure for table `cycling`
--

CREATE TABLE `cycling` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `duration_minutes` float NOT NULL,
  `distance_km` float NOT NULL,
  `speed_kmph` float DEFAULT NULL,
  `calorie_burned_kcal` float DEFAULT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `cycling`
--

INSERT INTO `cycling` (`id`, `user_id`, `duration_minutes`, `distance_km`, `speed_kmph`, `calorie_burned_kcal`, `recorded_at`) VALUES
(2, 2, 0.603117, 0.00396608, 0.394558, 17.0883, '2025-07-22 22:17:16'),
(3, 2, 1.373, 0.577433, 25.2338, 38.9017, '2025-07-23 07:31:24'),
(4, 2, 0.8535, 0.135108, 9.4979, 24.1825, '2025-07-24 10:29:12');

-- --------------------------------------------------------

--
-- Table structure for table `running`
--

CREATE TABLE `running` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `duration_minutes` float NOT NULL,
  `distance_km` float NOT NULL,
  `calorie_burned_kcal` float DEFAULT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `running`
--

INSERT INTO `running` (`id`, `user_id`, `duration_minutes`, `distance_km`, `calorie_burned_kcal`, `recorded_at`) VALUES
(1, 2, 0, 0.0133, 1, '2025-07-18 09:56:37'),
(3, 2, 0, 0.0126, 1, '2025-07-24 10:48:22'),
(4, 2, 0, 0.0077, 1, '2025-07-25 11:13:47');

-- --------------------------------------------------------

--
-- Table structure for table `session_keys`
--

CREATE TABLE `session_keys` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `session_key` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `session_keys`
--

INSERT INTO `session_keys` (`id`, `user_id`, `session_key`) VALUES
(6, 2, '3826abb987ac99196efc72c4ea573054d28a2f7ac3ab6b4ee11dca351971de83'),
(9, 2, 'cdccdf21021ac8d923758bc7a1b8b47971f0197c836de7166e677d33be9b9251'),
(10, 2, '517aee13295dce692dee7068d79b18b8403871c87e670f691519cb449acbbf97'),
(11, 2, '81365d4781affca25b3705685e5102429fc8139ca9d67677b20a92adb246cb7f'),
(12, 2, '273d205477cb224894894b26d7714b9fff8993f86936d160af877c73b70a0085'),
(13, 2, 'f2fed58c1522277e777c3f823453ed74cf91de04558337fb8a2f87e69a734f49'),
(15, 2, '784183f1f00ee0b3299c3ff8f8ebfad3cdaba227381dfce9d4c766c3ee5be9c4'),
(19, 2, '00c3ddaf91cf57a957c774ba440c3459ce73a57686cc68eb2b8dc2722e3699c2');

-- --------------------------------------------------------

--
-- Table structure for table `swimming`
--

CREATE TABLE `swimming` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `duration_minutes` float NOT NULL,
  `stroke_type` varchar(50) DEFAULT NULL,
  `laps` int(11) DEFAULT NULL,
  `calorie_burned_kcal` float DEFAULT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `swimming`
--

INSERT INTO `swimming` (`id`, `user_id`, `duration_minutes`, `stroke_type`, `laps`, `calorie_burned_kcal`, `recorded_at`) VALUES
(1, 2, 0.233333, 'Backstroke', 1, 4.67, '2025-07-18 19:54:57'),
(2, 2, 1.81667, 'Freestyle', 4, 112.39, '2025-07-24 10:36:34');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `token` varchar(255) DEFAULT NULL,
  `weight` decimal(5,2) NOT NULL,
  `height` decimal(5,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `running_target` int(11) DEFAULT 0,
  `cycling_target` int(11) DEFAULT 0,
  `swimming_target` int(11) DEFAULT 0,
  `weightlifting_target` int(11) DEFAULT 0,
  `yoga_target` int(11) DEFAULT 0,
  `target_start_date` datetime DEFAULT NULL,
  `target_end_date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `username`, `email`, `password`, `token`, `weight`, `height`, `created_at`, `running_target`, `cycling_target`, `swimming_target`, `weightlifting_target`, `yoga_target`, `target_start_date`, `target_end_date`) VALUES
(1, 'John Doe', 'john@example.com', '$2y$10$8VnWbmRIXFAv8BiX1Kmi3umHYzb7yuwErh5WbLVsh8PqjlY/pFNCK', NULL, 70.00, 175.00, '2025-06-23 05:49:44', 0, 0, 0, 0, 0, NULL, NULL),
(2, 'khin', 'khin@gmail.com', '$2y$10$/Q0WSFD0/jhZYg0jwQ5FOuT5eDugYmru7QsUhZ45TGE600VoDqT9K', NULL, 300.00, 300.00, '2025-06-23 16:35:48', 100, 200, 230, 110, 100, '2025-07-15 00:00:00', '2025-07-31 00:00:00'),
(3, 'khant', 'khant@gmail.com', '$2y$10$YrqWT936MDGQVEAmentJHOgXaWxuuxQUX8ZG5Z9d89Iyugz972Jzq', NULL, 100.00, 140.00, '2025-06-24 07:54:52', 0, 0, 0, 0, 0, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `weightlifting`
--

CREATE TABLE `weightlifting` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `duration_minutes` float NOT NULL,
  `sets` int(11) DEFAULT NULL,
  `reps_per_set` int(11) DEFAULT NULL,
  `weight_kg_per_rep` float DEFAULT NULL,
  `calorie_burned_kcal` float DEFAULT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `weightlifting`
--

INSERT INTO `weightlifting` (`id`, `user_id`, `duration_minutes`, `sets`, `reps_per_set`, `weight_kg_per_rep`, `calorie_burned_kcal`, `recorded_at`) VALUES
(1, 2, 1, 12, 11, 1, 0.693667, '2025-07-23 18:36:14'),
(2, 2, 1, 13, 10, 20, 2.375, '2025-07-23 18:40:22');

-- --------------------------------------------------------

--
-- Table structure for table `yoga`
--

CREATE TABLE `yoga` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `duration_minutes` float NOT NULL,
  `intensity_level` enum('Low','Moderate','High') DEFAULT 'Moderate',
  `calorie_burned_kcal` float DEFAULT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `yoga`
--

INSERT INTO `yoga` (`id`, `user_id`, `duration_minutes`, `intensity_level`, `calorie_burned_kcal`, `recorded_at`) VALUES
(1, 2, 0.84735, '', 677.88, '2025-07-23 19:13:59');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cycling`
--
ALTER TABLE `cycling`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `running`
--
ALTER TABLE `running`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `session_keys`
--
ALTER TABLE `session_keys`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `swimming`
--
ALTER TABLE `swimming`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `weightlifting`
--
ALTER TABLE `weightlifting`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `yoga`
--
ALTER TABLE `yoga`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `cycling`
--
ALTER TABLE `cycling`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `running`
--
ALTER TABLE `running`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `session_keys`
--
ALTER TABLE `session_keys`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `swimming`
--
ALTER TABLE `swimming`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `weightlifting`
--
ALTER TABLE `weightlifting`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `yoga`
--
ALTER TABLE `yoga`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `cycling`
--
ALTER TABLE `cycling`
  ADD CONSTRAINT `cycling_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `running`
--
ALTER TABLE `running`
  ADD CONSTRAINT `running_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `session_keys`
--
ALTER TABLE `session_keys`
  ADD CONSTRAINT `session_keys_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `swimming`
--
ALTER TABLE `swimming`
  ADD CONSTRAINT `swimming_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `weightlifting`
--
ALTER TABLE `weightlifting`
  ADD CONSTRAINT `weightlifting_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `yoga`
--
ALTER TABLE `yoga`
  ADD CONSTRAINT `yoga_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
