-- file: 10-create-user-and-db.sql
CREATE DATABASE orders;
CREATE ROLE program WITH PASSWORD 'test';
GRANT ALL PRIVILEGES ON DATABASE orders TO program;
ALTER ROLE program WITH LOGIN;