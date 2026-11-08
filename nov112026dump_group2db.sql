-- MySQL dump 10.13  Distrib 26.7.0, for Win64 (x86_64)
--
-- Host: localhost    Database: clonegroup2db
-- ------------------------------------------------------
-- Server version	26.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `build_items`
--

DROP TABLE IF EXISTS `build_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `build_items` (
  `build_item_id` int NOT NULL AUTO_INCREMENT,
  `build_id` int NOT NULL,
  `product_id` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  `source` varchar(10) NOT NULL DEFAULT 'store',
  PRIMARY KEY (`build_item_id`),
  UNIQUE KEY `build_id` (`build_id`,`product_id`),
  KEY `fk_bi_product` (`product_id`),
  CONSTRAINT `fk_bi_build` FOREIGN KEY (`build_id`) REFERENCES `builds` (`build_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bi_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `build_items`
--

LOCK TABLES `build_items` WRITE;
/*!40000 ALTER TABLE `build_items` DISABLE KEYS */;
INSERT INTO `build_items` VALUES (9,3,23,12500.00,1,'store'),(10,3,25,8200.00,1,'store'),(11,3,27,3200.00,2,'store'),(12,3,29,19500.00,1,'store'),(13,4,32,2900.00,1,'paid'),(14,4,42,6500.00,1,'paid'),(15,4,45,5400.00,1,'paid'),(16,4,49,1100.00,2,'paid'),(17,4,53,8500.00,1,'paid'),(18,4,56,2400.00,1,'paid'),(19,4,60,2600.00,1,'paid'),(20,5,23,12500.00,1,'paid'),(21,5,46,13500.00,1,'paid'),(22,5,48,7800.00,1,'paid'),(23,5,51,15105.00,1,'paid'),(24,5,55,3600.00,1,'paid'),(25,5,59,6800.00,1,'paid'),(26,5,62,4800.00,1,'paid'),(27,6,28,2300.00,2,'paid'),(28,6,32,2900.00,1,'paid'),(29,6,40,5900.00,1,'paid'),(30,6,44,6200.00,1,'paid'),(31,6,55,3600.00,1,'paid'),(32,6,60,2600.00,1,'paid'),(34,7,28,2300.00,1,'paid'),(35,7,29,19500.00,1,'paid'),(36,7,30,4200.00,1,'paid'),(37,7,38,5500.00,1,'paid'),(38,7,41,23500.00,1,'paid'),(39,7,44,6200.00,1,'paid'),(40,7,59,6800.00,1,'paid'),(41,7,65,2300.00,1,'paid'),(49,8,32,2900.00,1,'quote'),(50,8,40,5900.00,1,'quote'),(51,8,44,6200.00,1,'quote'),(52,8,60,2600.00,1,'quote'),(53,8,49,1100.00,2,'quote'),(54,8,55,3600.00,1,'quote'),(56,9,62,4800.00,1,'quote'),(57,9,65,2300.00,1,'quote'),(58,9,23,12500.00,1,'quote'),(59,9,51,15105.00,1,'quote'),(60,9,46,13500.00,1,'quote'),(61,9,59,6800.00,1,'quote'),(62,9,48,7800.00,1,'quote'),(63,9,58,11500.00,1,'quote');
/*!40000 ALTER TABLE `build_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `builds`
--

DROP TABLE IF EXISTS `builds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `builds` (
  `build_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `build_name` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(10) NOT NULL,
  PRIMARY KEY (`build_id`),
  KEY `fk_builds_customer` (`customer_id`),
  CONSTRAINT `fk_builds_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `builds`
--

LOCK TABLES `builds` WRITE;
/*!40000 ALTER TABLE `builds` DISABLE KEYS */;
INSERT INTO `builds` VALUES (3,1,'Budget AM5 Gaming PC','2026-11-08 13:26:02','pending'),(4,62,'Budget AM4 Esports PC','2026-10-18 10:00:00','completed'),(5,63,'AM5 1080p Gaming Rig','2026-11-05 13:00:00','building'),(6,64,'Office PC','2026-11-10 09:30:00','pending'),(7,55,'Streaming PC','2026-10-25 15:45:00','cancelled'),(8,1,'Intel Office Upgrade','2026-11-10 16:20:00','quote'),(9,56,'AM5 Streaming Rig','2026-11-11 10:05:00','quote');
/*!40000 ALTER TABLE `builds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (23,'Case'),(17,'CPU'),(24,'CPU Cooler'),(20,'GPU'),(18,'Motherboard'),(22,'Power Supply'),(19,'RAM'),(21,'Storage');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `u_id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` char(64) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `contact_num` varchar(20) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `u_id` (`u_id`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `fk_customers_user` FOREIGN KEY (`u_id`) REFERENCES `users` (`u_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,4,'johnoblea','joblea','John','Oblea','09123456789','Laguna'),(54,60,'walkin','walkin-no-login','Walk-in','Customer',NULL,NULL),(55,70,'mariasantos','mariasantos','Maria','Santos','09171234501','Quezon City'),(56,71,'josereyes','josereyes','Jose','Reyes','09171234502','Manila'),(57,72,'annagarcia','annagarcia','Anna','Garcia','09171234503','Pasig City'),(58,73,'markbautista','markbautista','Mark','Bautista','09171234504','Makati City'),(59,74,'lizacruz','lizacruz','Liza','Cruz','09171234505','Taguig City'),(60,75,'kevinramos','kevinramos','Kevin','Ramos','09171234506','Caloocan City'),(61,76,'sofiaaquino','sofiaaquino','Sofia','Aquino','09171234507','Antipolo City'),(62,77,'danielvilla','danielvilla','Daniel','Villa','09171234508','Cavite'),(63,78,'carlotorres','carlotorres','Carlo','Torres',NULL,NULL),(64,79,'ellafernandez','ellafernandez','Ella','Fernandez','09171234510','Laguna');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employees`
--

DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `u_id` int NOT NULL,
  `role_name` varchar(10) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` char(64) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'INACTIVE',
  `availability` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `u_id` (`u_id`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `fk_employees_user` FOREIGN KEY (`u_id`) REFERENCES `users` (`u_id`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employees`
--

LOCK TABLES `employees` WRITE;
/*!40000 ALTER TABLE `employees` DISABLE KEYS */;
INSERT INTO `employees` VALUES (1,1,'admin','andreidumlao','asvmdumlao','Andrei','Dumlao','active',NULL),(2,2,'admin','wilsonhalim','whalim','Wilson','Halim','active',NULL),(3,3,'admin','martingonzalez','rmgonzalez','Martin','Gonzalez','active',NULL),(58,61,'cashier','cashier1','cashier1','Carla','Cruz','active',NULL),(59,62,'inventory','inventory1','inventory1','Ivan','Reyes','active',NULL),(60,63,'technician','technician1','technician1','Tomas','Santos','active',NULL),(61,64,'cashier','cashier2','cashier2','Bea','Santos','active',NULL),(62,65,'cashier','cashier3','cashier3','Rico','Lim','active',NULL),(63,66,'cashier','cashier_old','cashier_old','Nina','Villanueva','INACTIVE',NULL),(64,67,'inventory','inventory2','inventory2','Joy','Mendoza','active',NULL),(65,68,'technician','technician2','technician2','Paolo','Reyes','active',NULL),(66,69,'technician','technician3','technician3','Mika','Tan','active',NULL);
/*!40000 ALTER TABLE `employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_orders`
--

DROP TABLE IF EXISTS `job_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_orders` (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `build_id` int NOT NULL,
  `employee_id` int NOT NULL,
  `status` varchar(15) NOT NULL DEFAULT 'Pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`job_id`),
  KEY `fk_jobs_build` (`build_id`),
  KEY `fk_jobs_employee` (`employee_id`),
  CONSTRAINT `fk_jobs_build` FOREIGN KEY (`build_id`) REFERENCES `builds` (`build_id`),
  CONSTRAINT `fk_jobs_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_orders`
--

LOCK TABLES `job_orders` WRITE;
/*!40000 ALTER TABLE `job_orders` DISABLE KEYS */;
INSERT INTO `job_orders` VALUES (3,3,60,'Pending','2026-11-08 13:26:03',NULL),(4,4,65,'Success','2026-10-18 10:00:00','2026-10-20 16:30:00'),(5,5,66,'In Progress','2026-11-05 13:00:00',NULL),(6,6,60,'Pending','2026-11-10 09:30:00',NULL),(7,7,66,'Cancelled','2026-10-25 15:45:00',NULL);
/*!40000 ALTER TABLE `job_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_specs`
--

DROP TABLE IF EXISTS `product_specs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_specs` (
  `spec_id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `spec_name` varchar(50) NOT NULL,
  `spec_value` varchar(100) NOT NULL,
  PRIMARY KEY (`spec_id`),
  KEY `fk_specs_product` (`product_id`),
  CONSTRAINT `fk_specs_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_specs`
--

LOCK TABLES `product_specs` WRITE;
/*!40000 ALTER TABLE `product_specs` DISABLE KEYS */;
INSERT INTO `product_specs` VALUES (1,23,'socket','AM5'),(2,23,'tdp','65W'),(3,23,'igpu','yes'),(4,23,'cooler_included','yes'),(5,24,'socket','LGA1700'),(6,24,'tdp','65W'),(7,24,'igpu','no'),(8,24,'cooler_included','yes'),(9,34,'socket','AM5'),(10,34,'tdp','120W'),(11,34,'igpu','yes'),(12,34,'cooler_included','no'),(13,25,'socket','AM5'),(14,25,'memory_type','DDR5'),(15,25,'form_factor','mATX'),(16,25,'ram_slots','4'),(17,26,'socket','LGA1700'),(18,26,'memory_type','DDR4'),(19,26,'form_factor','mATX'),(20,26,'ram_slots','4'),(21,35,'socket','AM5'),(22,35,'memory_type','DDR5'),(23,35,'form_factor','ATX'),(24,35,'ram_slots','4'),(25,27,'memory_type','DDR5'),(26,27,'capacity','16GB'),(27,28,'memory_type','DDR4'),(28,28,'capacity','16GB'),(29,29,'tdp','115W'),(30,36,'tdp','220W'),(31,30,'capacity','1TB'),(32,30,'interface','NVMe'),(33,31,'wattage','650W'),(34,37,'wattage','450W'),(35,32,'form_factor','mATX'),(36,38,'form_factor','ATX'),(37,39,'form_factor','ITX'),(38,33,'sockets','AM5,AM4,LGA1700,LGA1200'),(39,33,'max_tdp','220W'),(64,40,'socket','LGA1700'),(65,40,'tdp','60W'),(66,40,'igpu','yes'),(67,40,'cooler_included','yes'),(68,41,'socket','LGA1700'),(69,41,'tdp','125W'),(70,41,'igpu','yes'),(71,41,'cooler_included','no'),(72,42,'socket','AM4'),(73,42,'tdp','65W'),(74,42,'igpu','no'),(75,42,'cooler_included','yes'),(76,43,'socket','AM5'),(77,43,'tdp','65W'),(78,43,'igpu','yes'),(79,43,'cooler_included','yes'),(80,44,'socket','LGA1700'),(81,44,'memory_type','DDR4'),(82,44,'form_factor','mATX'),(83,44,'ram_slots','4'),(84,45,'socket','AM4'),(85,45,'memory_type','DDR4'),(86,45,'form_factor','mATX'),(87,45,'ram_slots','4'),(88,46,'socket','AM5'),(89,46,'memory_type','DDR5'),(90,46,'form_factor','ATX'),(91,46,'ram_slots','4'),(92,47,'socket','AM5'),(93,47,'memory_type','DDR5'),(94,47,'form_factor','ITX'),(95,47,'ram_slots','2'),(96,48,'memory_type','DDR5'),(97,48,'modules','2'),(98,48,'capacity','32GB'),(99,49,'memory_type','DDR4'),(100,49,'capacity','8GB'),(101,50,'memory_type','DDR5'),(102,50,'capacity','32GB'),(103,51,'tdp','165W'),(104,52,'tdp','285W'),(105,53,'tdp','75W'),(106,54,'tdp','263W'),(107,55,'capacity','1TB'),(108,55,'interface','NVMe'),(109,56,'capacity','500GB'),(110,56,'interface','SATA'),(111,57,'capacity','2TB'),(112,57,'interface','SATA'),(113,58,'capacity','2TB'),(114,58,'interface','NVMe'),(115,59,'wattage','750W'),(116,60,'wattage','550W'),(117,61,'wattage','1000W'),(118,62,'form_factor','ATX'),(119,63,'form_factor','ITX'),(120,64,'form_factor','mATX'),(121,65,'sockets','AM5,AM4,LGA1700,LGA1200'),(122,65,'max_tdp','260W'),(123,66,'sockets','AM5,AM4,LGA1700,LGA1200'),(124,66,'max_tdp','150W'),(125,67,'sockets','AM5,AM4,LGA1700'),(126,67,'max_tdp','300W');
/*!40000 ALTER TABLE `product_specs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `brand` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT '0',
  `description` varchar(100) DEFAULT NULL,
  `status` varchar(10) NOT NULL,
  `image` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `code` (`code`),
  KEY `fk_products_category` (`category_id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (23,17,'CPU-001','AMD Ryzen 5 7600','AMD',12500.00,15,'AM5, 6 cores','active','CPU-001.jpg'),(24,17,'CPU-002','Intel Core i5-12400F','Intel',8900.00,3,'LGA1700','active','CPU-002.jpg'),(25,18,'MB-001','ASRock B650M Pro RS','ASRock',8200.00,8,'AM5, DDR5','active','MB-001.jpg'),(26,18,'MB-002','MSI PRO B660M-A DDR4','MSI',6900.00,4,'LGA1700, DDR4','active','MB-002.jpg'),(27,19,'RAM-001','Kingston Fury 16GB DDR5-5600','Kingston',3200.00,20,'DDR5','active','RAM-001.jpg'),(28,19,'RAM-002','Corsair Vengeance 16GB DDR4-3200','Corsair',2300.00,2,'DDR4','active','RAM-002.jpg'),(29,20,'GPU-001','RTX 4060 8GB','NVIDIA',19500.00,6,'8GB GDDR6','active','GPU-001.jpg'),(30,21,'SSD-001','Samsung 980 1TB','Samsung',4200.00,12,'NVMe','active','SSD-001.jpg'),(31,22,'PSU-001','Corsair CV650 650W','Corsair',3800.00,9,'650W Bronze','active','PSU-001.jpg'),(32,23,'CASE-001','Tecware Forge M2 (mATX)','Tecware',2900.00,5,'mATX','active','CASE-001.jpg'),(33,24,'COOL-001','Deepcool AK400','Deepcool',1500.00,10,'Air cooler','active','COOL-001.jpg'),(34,17,'CPU-003','AMD Ryzen 7 7800X3D','AMD',24500.00,4,'AM5, no cooler included','active','CPU-003.jpg'),(35,18,'MB-003','ASUS ROG Strix X670E-E','ASUS',21000.00,3,'AM5, DDR5, ATX','active','MB-003.jpg'),(36,20,'GPU-002','RTX 4070 Super 12GB','NVIDIA',38000.00,3,'12GB GDDR6X','active','GPU-002.jpg'),(37,22,'PSU-002','Corsair CV450 450W','Corsair',3000.00,6,'450W Bronze','active','PSU-002.jpg'),(38,23,'CASE-002','NZXT H5 Flow (ATX)','NZXT',5500.00,4,'ATX','active','CASE-002.jpg'),(39,23,'CASE-003','Cooler Master Elite 110 (ITX)','Cooler Master',3500.00,2,'Mini-ITX','active','CASE-003.jpg'),(40,17,'CPU-004','Intel Core i3-12100','Intel',5900.00,10,'LGA1700, 4 cores','active','CPU-004.jpg'),(41,17,'CPU-005','Intel Core i7-13700K','Intel',23500.00,5,'LGA1700, no cooler','active','CPU-005.jpg'),(42,17,'CPU-006','AMD Ryzen 5 5600','AMD',6500.00,12,'AM4, 6 cores','active','CPU-006.jpg'),(43,17,'CPU-007','AMD Ryzen 7 7700','AMD',17500.00,0,'AM5, 8 cores','outofstock','CPU-007.jpg'),(44,18,'MB-004','Gigabyte B760M DS3H DDR4','Gigabyte',6200.00,7,'LGA1700, DDR4, mATX','active','MB-004.jpg'),(45,18,'MB-005','ASUS PRIME B550M-A','ASUS',5400.00,9,'AM4, DDR4, mATX','active','MB-005.jpg'),(46,18,'MB-006','MSI MAG B650 Tomahawk','MSI',13500.00,4,'AM5, DDR5, ATX','active','MB-006.jpg'),(47,18,'MB-007','ASRock B650I Lightning WiFi','ASRock',14500.00,2,'AM5, DDR5, ITX','active','MB-007.jpg'),(48,19,'RAM-003','G.Skill Trident Z5 32GB (2x16) DDR5','G.Skill',7800.00,8,'DDR5 kit of 2','active','RAM-003.jpg'),(49,19,'RAM-004','TeamGroup Elite 8GB DDR4-3200','TeamGroup',1100.00,25,'DDR4','active','RAM-004.jpg'),(50,19,'RAM-005','Kingston Fury Beast 32GB DDR5','Kingston',6200.00,6,'DDR5','active','RAM-005.jpg'),(51,20,'GPU-003','Radeon RX 7600 8GB','AMD',15900.00,5,'8GB GDDR6','active','GPU-003.jpg'),(52,20,'GPU-004','RTX 4070 Ti Super 16GB','NVIDIA',52000.00,2,'16GB GDDR6X','active','GPU-004.jpg'),(53,20,'GPU-005','GTX 1650 4GB','NVIDIA',8500.00,6,'4GB GDDR6','active','GPU-005.jpg'),(54,20,'GPU-006','Radeon RX 7800 XT 16GB','AMD',31500.00,3,'16GB GDDR6','active','GPU-006.jpg'),(55,21,'SSD-002','WD Blue SN580 1TB','Western Digital',3600.00,15,'NVMe','active','SSD-002.jpg'),(56,21,'SSD-003','Crucial MX500 500GB','Crucial',2400.00,10,'SATA SSD','active','SSD-003.jpg'),(57,21,'HDD-001','Seagate Barracuda 2TB','Seagate',3100.00,8,'7200rpm HDD','active','HDD-001.jpg'),(58,21,'SSD-004','Samsung 990 Pro 2TB','Samsung',11500.00,4,'NVMe Gen4','active','SSD-004.jpg'),(59,22,'PSU-003','Seasonic Focus GX-750','Seasonic',6800.00,5,'750W Gold','active','PSU-003.jpg'),(60,22,'PSU-004','MSI MAG A550BN','MSI',2600.00,7,'550W Bronze','active','PSU-004.jpg'),(61,22,'PSU-005','Corsair RM1000e','Corsair',10500.00,2,'1000W Gold','active','PSU-005.jpg'),(62,23,'CASE-004','Montech Air 903 Max (ATX)','Montech',4800.00,5,'ATX','active','CASE-004.jpg'),(63,23,'CASE-005','Lian Li A4-H2O (ITX)','Lian Li',7900.00,1,'Mini-ITX','active','CASE-005.jpg'),(64,23,'CASE-006','Darkflash DLM21 (mATX)','Darkflash',2500.00,0,'mATX','inactive','CASE-006.jpg'),(65,24,'COOL-002','Thermalright Peerless Assassin 120','Thermalright',2300.00,9,'Dual tower air cooler','active','COOL-002.jpg'),(66,24,'COOL-003','Cooler Master Hyper 212','Cooler Master',1700.00,11,'Air cooler','active','COOL-003.jpg'),(67,24,'COOL-004','Arctic Liquid Freezer III 360','Arctic',6900.00,0,'360mm AIO','pending','COOL-004.jpg');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promos`
--

DROP TABLE IF EXISTS `promos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promos` (
  `promo_id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `created_by` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `percentage` decimal(5,2) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`promo_id`),
  KEY `fk_promos_product` (`product_id`),
  KEY `fk_promos_employee` (`created_by`),
  CONSTRAINT `fk_promos_employee` FOREIGN KEY (`created_by`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `fk_promos_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promos`
--

LOCK TABLES `promos` WRITE;
/*!40000 ALTER TABLE `promos` DISABLE KEYS */;
INSERT INTO `promos` VALUES (3,27,1,'RAM Sale',10.00,NULL,NULL),(4,51,1,'GPU November Deal',5.00,'2026-11-01','2026-11-30'),(5,55,1,'October SSD Sale',15.00,'2026-10-01','2026-10-31');
/*!40000 ALTER TABLE `promos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order_items`
--

DROP TABLE IF EXISTS `purchase_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_items` (
  `po_item_id` int NOT NULL AUTO_INCREMENT,
  `po_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `unit_cost` decimal(10,2) NOT NULL,
  PRIMARY KEY (`po_item_id`),
  UNIQUE KEY `po_id` (`po_id`,`product_id`),
  KEY `fk_poi_product` (`product_id`),
  CONSTRAINT `fk_poi_po` FOREIGN KEY (`po_id`) REFERENCES `purchase_orders` (`po_id`),
  CONSTRAINT `fk_poi_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_items`
--

LOCK TABLES `purchase_order_items` WRITE;
/*!40000 ALTER TABLE `purchase_order_items` DISABLE KEYS */;
INSERT INTO `purchase_order_items` VALUES (1,1,49,10,880.00),(2,1,55,10,2880.00),(4,2,52,3,41600.00),(5,3,39,5,2800.00),(6,3,43,6,14000.00),(7,3,28,5,1840.00),(8,4,67,4,5520.00),(9,4,47,4,11600.00);
/*!40000 ALTER TABLE `purchase_order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_orders`
--

DROP TABLE IF EXISTS `purchase_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_orders` (
  `po_id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `created_by` int NOT NULL,
  `order_date` date NOT NULL,
  `status` varchar(15) NOT NULL,
  PRIMARY KEY (`po_id`),
  KEY `fk_po_supplier` (`supplier_id`),
  KEY `fk_po_employee` (`created_by`),
  CONSTRAINT `fk_po_employee` FOREIGN KEY (`created_by`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `fk_po_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_orders`
--

LOCK TABLES `purchase_orders` WRITE;
/*!40000 ALTER TABLE `purchase_orders` DISABLE KEYS */;
INSERT INTO `purchase_orders` VALUES (1,5,59,'2026-10-01','Received'),(2,6,64,'2026-10-14','Cancelled'),(3,7,59,'2026-11-07','Ordered'),(4,8,64,'2026-11-09','Ordered');
/*!40000 ALTER TABLE `purchase_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refunds`
--

DROP TABLE IF EXISTS `refunds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds` (
  `refund_id` int NOT NULL AUTO_INCREMENT,
  `sale_item_id` int NOT NULL,
  `processed_by` int NOT NULL,
  `refund_amount` decimal(10,2) NOT NULL,
  `reason` varchar(100) DEFAULT NULL,
  `refund_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `quantity` int NOT NULL,
  PRIMARY KEY (`refund_id`),
  KEY `fk_refunds_item` (`sale_item_id`),
  KEY `fk_refunds_employee` (`processed_by`),
  CONSTRAINT `fk_refunds_employee` FOREIGN KEY (`processed_by`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `fk_refunds_item` FOREIGN KEY (`sale_item_id`) REFERENCES `sale_items` (`sale_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refunds`
--

LOCK TABLES `refunds` WRITE;
/*!40000 ALTER TABLE `refunds` DISABLE KEYS */;
INSERT INTO `refunds` VALUES (1,3,61,2880.00,'Wrong speed, customer exchanged','2026-10-09 10:20:00',1),(2,13,58,1500.00,'Bent fins out of the box','2026-10-29 11:00:00',1);
/*!40000 ALTER TABLE `refunds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(10) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'admin'),(2,'cashier'),(5,'customer'),(3,'inventory'),(4,'technician');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sale_items`
--

DROP TABLE IF EXISTS `sale_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sale_items` (
  `sale_item_id` int NOT NULL AUTO_INCREMENT,
  `sale_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`sale_item_id`),
  UNIQUE KEY `sale_id` (`sale_id`,`product_id`),
  KEY `fk_si_product` (`product_id`),
  CONSTRAINT `fk_si_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `fk_si_sale` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sale_items`
--

LOCK TABLES `sale_items` WRITE;
/*!40000 ALTER TABLE `sale_items` DISABLE KEYS */;
INSERT INTO `sale_items` VALUES (1,1,23,1,12500.00),(2,1,25,1,8200.00),(3,2,27,2,2880.00),(4,2,55,1,3060.00),(5,3,29,1,19500.00),(6,3,31,1,3800.00),(7,4,49,2,1100.00),(8,4,56,1,2400.00),(9,5,42,1,6500.00),(10,5,45,1,5400.00),(11,5,49,2,1100.00),(12,5,53,1,8500.00),(13,6,33,1,1500.00),(14,7,51,1,15105.00),(15,7,60,1,2600.00),(16,8,58,1,11500.00),(17,9,65,1,2300.00),(18,9,57,1,3100.00),(19,10,40,1,5900.00),(20,10,44,1,6200.00),(21,10,49,2,1100.00),(22,11,62,1,4800.00),(23,11,59,1,6800.00),(24,12,27,3,2880.00),(25,13,32,1,2900.00),(26,13,42,1,6500.00),(27,13,53,1,8500.00),(28,13,45,1,5400.00),(29,13,60,1,2600.00),(30,13,49,2,1100.00),(31,13,56,1,2400.00),(32,14,62,1,4800.00),(33,14,23,1,12500.00),(34,14,51,1,15105.00),(35,14,46,1,13500.00),(36,14,59,1,6800.00),(37,14,48,1,7800.00),(38,14,55,1,3600.00),(39,15,32,1,2900.00),(40,15,40,1,5900.00),(41,15,44,1,6200.00),(42,15,60,1,2600.00),(43,15,28,2,2300.00),(44,15,55,1,3600.00),(46,16,38,1,5500.00),(47,16,65,1,2300.00),(48,16,41,1,23500.00),(49,16,29,1,19500.00),(50,16,44,1,6200.00),(51,16,59,1,6800.00),(52,16,28,1,2300.00),(53,16,30,1,4200.00);
/*!40000 ALTER TABLE `sale_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales`
--

DROP TABLE IF EXISTS `sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales` (
  `sale_id` int NOT NULL AUTO_INCREMENT,
  `employee_id` int NOT NULL,
  `customer_id` int NOT NULL,
  `sale_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `discount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `total` decimal(10,2) NOT NULL,
  `amount_paid` decimal(10,2) NOT NULL,
  `payment_method` varchar(20) NOT NULL,
  PRIMARY KEY (`sale_id`),
  KEY `fk_sales_employee` (`employee_id`),
  KEY `fk_sales_customer` (`customer_id`),
  CONSTRAINT `fk_sales_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  CONSTRAINT `fk_sales_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales`
--

LOCK TABLES `sales` WRITE;
/*!40000 ALTER TABLE `sales` DISABLE KEYS */;
INSERT INTO `sales` VALUES (1,58,1,'2026-10-03 10:12:00',0.00,20700.00,20700.00,'Cash'),(2,61,55,'2026-10-07 14:30:00',1180.00,8820.00,8820.00,'Card'),(3,58,56,'2026-10-12 11:05:00',0.00,23300.00,23300.00,'Cash'),(4,62,57,'2026-10-15 16:45:00',0.00,4600.00,4600.00,'Cashless'),(5,61,57,'2026-10-21 13:20:00',0.00,22600.00,22600.00,'Cash'),(6,58,60,'2026-10-28 09:40:00',0.00,1500.00,1500.00,'Cash'),(7,62,58,'2026-11-02 15:10:00',795.00,17705.00,17705.00,'Card'),(8,61,59,'2026-11-04 10:55:00',0.00,11500.00,11500.00,'Cash'),(9,58,59,'2026-11-06 17:25:00',0.00,5400.00,5400.00,'Cashless'),(10,62,60,'2026-11-08 12:00:00',0.00,14300.00,14300.00,'Cash'),(11,61,61,'2026-11-09 14:35:00',0.00,11600.00,11600.00,'Card'),(12,58,58,'2026-11-10 11:15:00',960.00,8640.00,8700.00,'Cash'),(13,61,62,'2026-10-18 10:00:00',0.00,30500.00,30500.00,'Card'),(14,58,63,'2026-11-05 13:00:00',795.00,64105.00,64200.00,'Cash'),(15,58,64,'2026-11-10 09:30:00',0.00,25800.00,25800.00,'Cashless'),(16,62,55,'2026-10-25 15:45:00',0.00,70300.00,70300.00,'Card');
/*!40000 ALTER TABLE `sales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `supplier_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `contact` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`supplier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES (5,'TechSource Manila','0917-111-2222'),(6,'PC Parts Hub','0918-333-4444'),(7,'Silicon Valley PH','02-8123-4567'),(8,'DynaQuest PC','0917-555-0101'),(9,'EasyPC Wholesale','0918-555-0202'),(10,'Gilmore IT Supply','02-8765-4321');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `u_id` int NOT NULL AUTO_INCREMENT,
  `role` int NOT NULL,
  PRIMARY KEY (`u_id`),
  KEY `fk_users_role` (`role`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,1),(2,1),(3,1),(61,2),(64,2),(65,2),(66,2),(62,3),(67,3),(63,4),(68,4),(69,4),(4,5),(60,5),(70,5),(71,5),(72,5),(73,5),(74,5),(75,5),(76,5),(77,5),(78,5),(79,5);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-08 18:38:35
