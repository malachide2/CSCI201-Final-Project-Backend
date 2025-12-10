Prompt 1:

"Can you turn our design document’s database schema (Users, Hikes, Reviews, Photos, Friends) into SQL and structure the file so we can load it in MySQL Workbench?"


Chat GPT code (excerpt):
CREATE DATABASE IF NOT EXISTS trail_app;
USE trail_app;

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

Prompt 2 (Fix / Enhancement):
“This isn’t valid for MySQL Workbench — we’re using MySQL locally like in earlier CSCI 201 labs. Can you rewrite the schema using correct MySQL syntax (AUTO_INCREMENT, ON UPDATE CURRENT_TIMESTAMP, etc.) and match our actual database name?”

Improved ChatGPT Code (Excerpt):
DROP DATABASE IF EXISTS hike_app;
CREATE DATABASE hike_app;
USE hike_app;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


Explanation:
This helped me convert the schema into proper MySQL syntax, which matches the environment we actually use in CSCI 201. I learned the differences between PostgreSQL and MySQL, including using AUTO_INCREMENT instead of SERIAL, and how MySQL handles timestamps, foreign keys, and database-selection commands. This also helped me understand how to structure the file so my teammates could simply run it locally in MySQL Workbench.
