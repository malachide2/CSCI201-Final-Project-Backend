-- USERS TABLE
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HIKES TABLE
CREATE TABLE hikes (
    hike_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location_text VARCHAR(255),
    difficulty DECIMAL(2,1) CHECK (difficulty >= 1 AND difficulty <= 5),
    distance DECIMAL(4,1),
    elevation INT,
    created_by INT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- REVIEWS TABLE
CREATE TABLE reviews (
    review_id SERIAL PRIMARY KEY,
    hike_id INT NOT NULL,
    user_id INT NOT NULL,
    rating DECIMAL(2,1) CHECK (rating >= 1 AND rating <= 5),
    review_body TEXT,
    upvotes_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (hike_id) REFERENCES hikes(hike_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- REVIEW_UPVOTES TABLE
CREATE TABLE review_upvotes (
    review_upvote_id SERIAL PRIMARY KEY,
    review_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(review_id, user_id),
    
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- PHOTOS TABLE
CREATE TABLE photos (
    photo_id SERIAL PRIMARY KEY,
    hike_id INT NOT NULL,
    user_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (hike_id) REFERENCES hikes(hike_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- FRIENDS TABLE
CREATE TABLE friends (
    follower_id INT NOT NULL,
    followed_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY(follower_id, followed_id),
    
    FOREIGN KEY (follower_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users(user_id) ON DELETE CASCADE
);
