-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 26, 2025 at 10:30 AM
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
-- Database: `simply_fresh_foods`
--

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(8,2) NOT NULL,
  `quantity_in_stock` int(11) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `item_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_name`, `category`, `price`, `quantity_in_stock`, `image`, `item_id`) VALUES
('Carrot', 'Vegetable', 3.00, 200, 'images/carrots-7.jpg', 1),
('Broccoli', 'Vegetable', 4.00, 180, 'images/p7_Broccoli_HH1812_gi905351392.jpg', 2),
('Spinach', 'Vegetable', 3.50, 150, 'images/Spinach.jpg', 3),
('Tomato', 'Vegetable', 4.00, 220, 'images/tomato_fruits.jpg', 4),
('Cucumber', 'Vegetable', 3.50, 210, 'images/shutterstock_520879192.jpg', 5),
('Lettuce', 'Vegetable', 3.00, 160, 'images/lettuce.jpg', 6),
('Eggplant', 'Vegetable', 4.50, 140, 'images/Eggplant.jpg', 7),
('Bell Pepper', 'Vegetable', 5.00, 130, 'images/Bell-peppers.jpg', 8),
('Onion', 'Vegetable', 3.00, 250, 'images/Onions-copy-2.jpg', 9),
('Garlic', 'Vegetable', 3.50, 240, 'images/Bulbs-cloves-garlic.jpg', 10),
('Zucchini', 'Vegetable', 4.00, 170, 'images/1800x1200_zucchini.jpg', 11),
('Cauliflower', 'Vegetable', 4.50, 160, 'images/h1018g16207257715328.jpg', 12),
('Green Bean', 'Vegetable', 3.00, 190, 'images/green-beans-1296x728-header.jpg', 13),
('Peas', 'Vegetable', 3.50, 180, 'images/Green-Pea.jpg', 14),
('Corn', 'Vegetable', 4.00, 210, 'images/wouter-supardi-salari-HE_MjmWh9eQ-unsplash-1536x1024.jpg', 15),
('Radish', 'Vegetable', 3.00, 150, 'images/radish.jpg.jpg', 16),
('Potato', 'Vegetable', 3.00, 300, 'images/russet-potato.jpg', 17),
('Mushroom', 'Vegetable', 5.00, 120, 'images/cremini-mushrooms-and-a-cooking-pot-920804676-5aa82a6da9d4f90036c8dfaf.jpg', 18),
('Asparagus', 'Vegetable', 6.00, 100, 'images/ASPARAGUS-768x512.jpeg', 19),
('Cabbage', 'Vegetable', 3.50, 220, 'images/cabbage.png', 20),
('Apple', 'Fruit', 5.00, 100, 'images/apple.jpeg', 21),
('Banana', 'Fruit', 4.50, 120, 'images/554.jpg', 22),
('Mango', 'Fruit', 7.00, 80, 'images/1000_F_187654684_ScVTIc0odtqtWsBLp0oTMprDvqTu9VQR.jpg', 23),
('Orange', 'Fruit', 6.00, 90, 'images/image001.jpg', 24),
('Papaya', 'Fruit', 5.50, 110, 'images/Papaya.jpg', 25),
('Grapes', 'Fruit', 8.00, 75, 'images/grapes-1-q7gkiacnis3gbi4zp0bygg6950mcpwmgv7dpzkrm2w.jpg', 26),
('Pineapple', 'Fruit', 6.50, 60, 'images/sub-buzz-28999-1488921677-1.jpg', 27),
('Strawberry', 'Fruit', 10.00, 40, 'images/p11.jpg', 28),
('Watermelon', 'Fruit', 4.00, 130, 'images/XenViG9cC4EdGupeibtKa5-1280-80.jpg.jpg', 29),
('Peach', 'Fruit', 7.50, 70, 'images/na_62e984dc0e47d.jpg', 30),
('Cherry', 'Fruit', 12.00, 35, 'images/kcMKpzkGi.jpg', 31),
('Plum', 'Fruit', 6.00, 80, 'images/shutterstock-645293734.jpg', 32),
('Guava', 'Fruit', 5.00, 90, 'images/unripe-guava-wju6r0qrd658k1wn.jpg', 33),
('Kiwi', 'Fruit', 9.00, 50, 'images/Kiwi-fruit.jpg', 34),
('Lychee', 'Fruit', 11.00, 40, 'images/product-packshot-Lychee.jpg', 35),
('Apricot', 'Fruit', 8.50, 65, 'images/apricots-768x611.png', 36),
('Cantaloupe', 'Fruit', 5.50, 95, 'images/perfect-cantaloupe.jpg', 37),
('Blackberry', 'Fruit', 13.00, 30, 'images/Blackberry-Fruit-Transparent-PNG.png', 38),
('Raspberry', 'Fruit', 12.50, 30, 'images/berry-2270_1920.jpg', 39),
('Pomegranate', 'Fruit', 6.50, 85, 'images/how-to-eat-pomegranate.jpg', 40),
('Chicken Breast', 'Meat', 20.00, 50, 'images/1-lb-Chicken-Breast-Protein-scaled.jpg', 41),
('Beef Steak', 'Meat', 35.00, 40, 'images/IMG_5495-scaled.jpg', 42),
('Pork Chops', 'Meat', 25.00, 45, 'images/porkchops-768x510.jpg', 43),
('Lamb Rack', 'Meat', 40.00, 30, 'images/Blackwells-Lamb-Rack-2-1000x1000.jpg', 44),
('Turkey', 'Meat', 30.00, 35, 'images/raw-turkey.jpg', 45),
('Duck', 'Meat', 32.00, 25, 'images/duck-meat.jpg', 46),
('Ham', 'Meat', 28.00, 40, 'images/ham-pictures.jpg', 47),
('Sausage', 'Meat', 22.00, 55, 'images/Aussie+Sausages+(2).jpg', 48),
('Bacon', 'Meat', 24.00, 50, 'images/AdobeStock_182196772-600x406.jpeg', 49),
('Veal', 'Meat', 38.00, 30, 'images/veal-1024x680.jpg', 50),
('Ground Beef', 'Meat', 26.00, 60, 'images/ground-beef.jpg', 51),
('Ribs', 'Meat', 35.00, 30, 'images/iStock-917272858-300x225.jpg', 52),
('Meatball', 'Meat', 25.00, 70, 'images/slow-cooker-meatballs-2.jpg', 53),
('Pork Belly', 'Meat', 30.00, 40, 'images/12683970-Raw-pork-belly-with-rind.jpg', 54),
('Salami', 'Meat', 28.00, 35, 'images/what-is-salami.jpg', 55),
('Prosciutto', 'Meat', 32.00, 30, 'images/Slices-of-Italian-prosciutto-crudo-or-jamon.jpg', 56),
('Chicken Thigh', 'Meat', 20.00, 50, 'images/raw-chicken-thigh-isolated-white_176402-2166.jpg', 57),
('Chicken Drumstick', 'Meat', 18.00, 55, 'images/CHICKEN-DRUMSTICKS.jpg', 58),
('Beef Brisket', 'Meat', 36.00, 30, 'images/Porter-and-York-Brisket-Raw-768x576.jpg', 59),
('Corned Beef', 'Meat', 27.00, 40, 'images/Corned-Hash-2-1024x683.jpg', 60),
('Salmon', 'Seafood', 80.00, 30, 'images/intro-1642705828.jpg', 61),
('Shrimp', 'Seafood', 60.00, 40, 'images/Shrimp.jpg', 62),
('Tuna', 'Seafood', 75.00, 35, 'images/Ahi-Tuna-768x512.jpg', 63),
('Lobster', 'Seafood', 100.00, 20, 'images/raw-lobster_1472-28316.jpg', 64),
('Crab', 'Seafood', 90.00, 25, 'images/Stone-Crab.jpg', 65),
('Mackerel', 'Seafood', 65.00, 35, 'images/fresh-raw-mackerel-fish-market-food_194646-1431.jpg', 66),
('Trout', 'Seafood', 85.00, 25, 'images/raw-fresh-trout-fish_89816-18888.jpg', 67),
('Sardine', 'Seafood', 55.00, 40, 'images/depositphotos_24016457-stock-photo-fresh-raw-sardines.jpg', 68),
('Scallops', 'Seafood', 95.00, 20, 'images/Raw-scallop-in-the-shell-on-a-serving-board-on-a-brown-wooden-table-960x641.jpg', 69),
('Mussels', 'Seafood', 50.00, 45, 'images/Aulacomya_atra_-_Cholgas_en_pescaderia_Puerto_Varas_2015_nov.jpg', 70),
('Oysters', 'Seafood', 100.00, 20, 'images/ThinkstockPhotos-180650004-1024x681.jpg', 71),
('Clams', 'Seafood', 90.00, 25, 'images/intro-1672248663.jpg', 72),
('Halibut', 'Seafood', 85.00, 30, 'images/pacific_halibut_custom-0dfcf1a582c77686b8368d21ec6c928bf8976f84-830x622.jpg', 73),
('Sea Bass', 'Seafood', 80.00, 35, 'images/uncooked-raw-sea-bass-fillets-seabass-fish-with-thyme-pink-salt-lemon-dark-background-top-view_89816.jpg', 74),
('Swordfish', 'Seafood', 95.00, 20, 'images/fresh_swordfish.jpg', 75),
('Tilapia', 'Seafood', 60.00, 40, 'images/fresh-raw-tilapia-fish-from-tilapia-farm-tilapia-with-white-plate-with-rosemary-lemon-lime-wooden-ba.jpg', 76),
('Crawfish', 'Seafood', 70.00, 30, 'images/fresh-raw-crayfish-isolated-white-healthy-seafood-fresh-raw-crayfish-isolated-white-healthy-seafood-195082459.jpg', 77),
('Octopus', 'Seafood', 85.00, 25, 'images/raw-octopus-ready-for-cooking-photo.jpg', 78),
('Squid', 'Seafood', 75.00, 30, 'images/Squid-scaled.jpg', 79),
('Cod', 'Seafood', 70.00, 30, 'images/cod-400x400.jpg', 80),
('Basmati Rice', 'Grains', 12.00, 100, 'images/basmati-rice-in-a-bowl-with-a-spoon-519309138-7ca58970c0914bb9b117d43cb09d7dd8.jpg', 81),
('Brown Rice', 'Grains', 10.00, 150, 'images/2093853.jpg', 82),
('Jasmine Rice', 'Grains', 11.50, 120, 'images/raw-jasmine-rice-grain-with-ear-paddy-agricultural-products-food-asian-thai-rice-white-bowl-sack-bac.jpg', 83),
('Quinoa', 'Grains', 20.00, 80, 'images/OIP.jpg', 84),
('Barley', 'Grains', 8.00, 200, 'images/where-is-barley-in-grocery-stores.jpeg', 85),
('Oats', 'Grains', 9.00, 180, 'images/Oats.jpg', 86),
('Millet', 'Grains', 7.50, 140, 'images/millet.jpg', 87),
('Buckwheat', 'Grains', 10.00, 160, 'images/Organic-Japanese-Buckwheat-Bulk__96820.jpg', 88),
('Cornmeal', 'Grains', 7.00, 200, 'images/shutterstock_1925613104-1.jpg', 89),
('Wild Rice', 'Grains', 18.00, 60, 'images/Wild-Rice-100g.jpg', 90),
('Whole Milk', 'Dairy', 5.00, 100, 'images/Great-Value-Whole-Vitamin-D-Milk-Gallon-128-fl-oz_6a7b09b4-f51d-4bea-a01c-85767f1b481a.86876244397d83ce6cdedb030abe6e4a.jpg', 91),
('Skim Milk', 'Dairy', 4.50, 120, 'images/OIP (1).jpg', 92),
('Low-Fat Milk', 'Dairy', 4.75, 110, 'images/4800110097909-tile-2-side.jpg', 93),
('Butter', 'Dairy', 8.00, 80, 'images/When-to-Use-Salted-vs.-Unsalted-Butter_hero_1-edited-scaled.jpg', 94),
('Yogurt', 'Dairy', 5.50, 150, 'images/How-to-Make-Yogurt-7.jpg', 95),
('Cheddar Cheese', 'Dairy', 10.00, 80, 'images/Cheese-cheddar-e1639449389970-1536x1226.jpg', 96),
('Mozzarella Cheese', 'Dairy', 9.50, 85, 'images/Mozzarella-Cheese_9530.jpg', 97),
('Parmesan Cheese', 'Dairy', 12.00, 60, 'images/parmesan_on_wooden_surface.jpg', 98),
('Cream Cheese', 'Dairy', 8.50, 90, 'images/Plain-Cream-Cheese.jpg', 99),
('Evaporated Milk', 'Dairy', 5.50, 100, 'images/edd6fccf-1c48-4f82-ae7f-b5abfb6032ff.90895e2d489ab241524dce8d5beda1c2.jpg', 100),
('Olive Oil', 'Oils', 25.00, 50, 'images/Extra-olive-oil-1l.png', 101),
('Canola Oil', 'Oils', 18.00, 60, 'images/5b92f7d6-8ac0-4630-95ba-8dec7f87edc4_1.ddced08c72faab99679b8d6348dbbee2.jpg', 102),
('Sunflower Oil', 'Oils', 16.00, 70, 'images/61ylb82wLFL._SL1500_.jpg', 103),
('Coconut Oil', 'Oils', 22.00, 40, 'images/EDF.jpg', 104),
('Peanut Oil', 'Oils', 20.00, 55, 'images/PurePeanutOil1L.jpg', 105),
('Soybean Oil', 'Oils', 15.00, 65, 'images/41agMVH16HL.jpg', 106),
('Corn Oil', 'Oils', 14.00, 70, 'images/raf.jpeg', 107),
('Sesame Oil', 'Oils', 28.00, 45, 'images/61fJPSxqyTL._SL1500_.jpg', 108),
('Avocado Oil', 'Oils', 30.00, 40, 'images/Screenshot2021-05-26at11.31.19.jpg', 109),
('Grapeseed Oil', 'Oils', 26.00, 50, 'images/78140_HFARM_9339337003738-1_1024x1024.jpg', 110);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `details` text NOT NULL,
  `order_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_price` decimal(10,2) NOT NULL,
  `shipping_address` text NOT NULL,
  `status` varchar(50) DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `promotional_codes`
--

CREATE TABLE `promotional_codes` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `discount_percentage` decimal(5,2) NOT NULL,
  `expiration_date` date NOT NULL,
  `usage_limit` int(11) NOT NULL,
  `times_used` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotional_codes`
--

INSERT INTO `promotional_codes` (`id`, `code`, `discount_percentage`, `expiration_date`, `usage_limit`, `times_used`) VALUES
(1, 'PROMO10', 10.00, '2025-12-31', 100, 19),
(2, 'PROMO20', 20.00, '2025-12-31', 50, 0),
(3, 'PROMO30', 30.00, '2025-12-31', 25, 0),
(4, 'PROMO50', 50.00, '2025-12-31', 10, 6);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `sex` enum('male','female') NOT NULL,
  `birthdate` date NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `country` varchar(100) NOT NULL,
  `role` enum('user','admin') NOT NULL DEFAULT 'user',
  `registration_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `point_balance` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `first_name`, `last_name`, `sex`, `birthdate`, `email`, `password`, `phone_number`, `street`, `district`, `city`, `postal_code`, `state`, `country`, `role`, `registration_date`, `point_balance`) VALUES
(1, 'Administrator', 'Testing', 'male', '2004-01-12', 'administrator@email.com', '$2y$10$dFlVj1EW1FprzhsEUN36S.TZpy5NYS.I5BG.Tz4FuFI745CM0na4C', '+60 12-3456789', '1, Testing Street, 12/345', 'Testing District', 'Testing City', '12345', 'Testing State', 'Testing Country', 'admin', '2025-03-26 00:22:21', 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`item_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `fk_orders_user_id` (`user_id`);

--
-- Indexes for table `promotional_codes`
--
ALTER TABLE `promotional_codes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=116;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `promotional_codes`
--
ALTER TABLE `promotional_codes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
