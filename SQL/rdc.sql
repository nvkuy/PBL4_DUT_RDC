-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 16, 2023 at 11:49 AM
-- Server version: 10.4.27-MariaDB
-- PHP Version: 8.2.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `rdc`
--

-- --------------------------------------------------------

--
-- Table structure for table `_admin_comp`
--

CREATE TABLE `_admin_comp` (
  `IPComp` varchar(22) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `_admin_comp`
--

INSERT INTO `_admin_comp` (`IPComp`) VALUES
('192.168.23.88');

-- --------------------------------------------------------

--
-- Table structure for table `_comp`
--

CREATE TABLE `_comp` (
  `CompID` varchar(69) NOT NULL,
  `PublicKey` varchar(333) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `_comp`
--

INSERT INTO `_comp` (`CompID`, `PublicKey`) VALUES
('admin1', 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZQgziSQiBlv0nG+BXwVa2tgtWOEe4SNgGP8Ap9QLtCwUeKY1fZjND0Kw3yGkQQ9HbaG2FF/d2W8+f3bZ/tyoF0JEKadQ7XrlrR0H/QhJKSteOWEM2i7mRc6A+3MQoA/gSCt5S6alB65uFENFzPMEqHyl75b2S3Hn+0mKK1uY7BQIDAQAB'),
('employee1', 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCM+ybApYu5AOl6OnxaZc0LlZgDCGKCEHqzVGTZyh77ZZVthc0e33KtXF9V6Pk6nPgM/8SR/kzNGMP5lcL4BuXavgKeSU4djWacuMmqroUlrwxzgAmRjFUFsngRQjgN5I8K9bvSqNB6HhVup6OuTh0jjQLEKD+WIIRIqLDCm9wJCQIDAQAB'),
('root', 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6prCDkPMqob5gp/R8/ZFGgM4RT/tOXoIgXJLTEw6CWKSECRTiMH/RqJSI2I92MNqQXnPM8DW8ixcne8OUtWt49CJiAbj6FdlzbY343WNy3sUlNbjypOXYK2Kwwt978lj1k3pYB2CUeJK2CG64XpOVKac5yV6oNiiPVmSGRLODvQIDAQAB');

-- --------------------------------------------------------

--
-- Table structure for table `_comp_app_history`
--

CREATE TABLE `_comp_app_history` (
  `CompID` varchar(69) NOT NULL,
  `TimeID` varchar(88) NOT NULL,
  `AppName` varchar(96) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `_comp_info`
--

CREATE TABLE `_comp_info` (
  `CompID` varchar(69) NOT NULL,
  `EmployeeID` varchar(69) DEFAULT NULL,
  `EmployeeName` varchar(96) DEFAULT NULL,
  `Mail` varchar(96) DEFAULT NULL,
  `EmployeeImage` varchar(88) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `_not_allow_app`
--

CREATE TABLE `_not_allow_app` (
  `AppName` varchar(96) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `_admin_comp`
--
ALTER TABLE `_admin_comp`
  ADD PRIMARY KEY (`IPComp`);

--
-- Indexes for table `_comp`
--
ALTER TABLE `_comp`
  ADD PRIMARY KEY (`CompID`);

--
-- Indexes for table `_comp_app_history`
--
ALTER TABLE `_comp_app_history`
  ADD PRIMARY KEY (`CompID`,`TimeID`,`AppName`),
  ADD KEY `IDX_APP_HISTORY1` (`CompID`),
  ADD KEY `IDX_APP_HISTORY2` (`CompID`,`TimeID`);

--
-- Indexes for table `_comp_info`
--
ALTER TABLE `_comp_info`
  ADD PRIMARY KEY (`CompID`);

--
-- Indexes for table `_not_allow_app`
--
ALTER TABLE `_not_allow_app`
  ADD PRIMARY KEY (`AppName`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `_comp_app_history`
--
ALTER TABLE `_comp_app_history`
  ADD CONSTRAINT `_comp_app_history_ibfk_1` FOREIGN KEY (`CompID`) REFERENCES `_comp_info` (`CompID`);

--
-- Constraints for table `_comp_info`
--
ALTER TABLE `_comp_info`
  ADD CONSTRAINT `_comp_info_ibfk_1` FOREIGN KEY (`CompID`) REFERENCES `_comp` (`CompID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
