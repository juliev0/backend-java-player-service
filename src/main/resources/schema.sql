DROP TABLE IF EXISTS PLAYERS;

-- Create a table from the csv
CREATE TABLE PLAYERS AS SELECT * FROM CSVREAD('Player.csv');

-- Add a column to hold the generated nickname for each player
ALTER TABLE PLAYERS ADD COLUMN NICKNAME VARCHAR(255);
