-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : ven. 26 mai 2023 à 19:14
-- Version du serveur : 10.4.27-MariaDB
-- Version de PHP : 8.1.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `chat_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `message`
--

CREATE TABLE `message` (
  `IDMessage` int(11) NOT NULL,
  `Message` varchar(100) NOT NULL,
  `Sender` varchar(100) NOT NULL,
  `Reciever` varchar(100) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Déchargement des données de la table `message`
--

INSERT INTO `message` (`IDMessage`, `Message`, `Sender`, `Reciever`) VALUES
(102, 'bonsoir', 'abir', 'omar	'),
(104, '', 'hajarelamri', 'nada');

-- --------------------------------------------------------

--
-- Structure de la table `user_tb`
--

CREATE TABLE `user_tb` (
  `name` varchar(30) NOT NULL,
  `pass` varchar(45) NOT NULL,
  `email` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Déchargement des données de la table `user_tb`
--

INSERT INTO `user_tb` (`name`, `pass`, `email`) VALUES
('elamri', '12345', 'hajar.elamri@etu.uae.ac.ma'),
('hajar', '123', 'hajar.lafkih@etu.uae.ac.ma'),
('haya', '123', 'haya.zaoudi@etu.uae.ac.ma'),
('nada', '123', 'nada.rayadi@etu.uae.ac.ma'),
('zineb', '123', 'zineb.lahmambennani@etu.uae.ac.ma');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `message`
--
ALTER TABLE `message`
  ADD PRIMARY KEY (`IDMessage`);

--
-- Index pour la table `user_tb`
--
ALTER TABLE `user_tb`
  ADD PRIMARY KEY (`name`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `message`
--
ALTER TABLE `message`
  MODIFY `IDMessage` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=109;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
