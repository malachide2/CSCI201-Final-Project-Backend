Prompt:
The FriendDao class is throwing "Operation not allowed after ResultSet closed" errors when calling multiple methods in sequence. Here's my current code using try-with-resources for the Connection object.
Issues:
The AI initially used text block syntax (""") for multi-line SQL strings which requires Java 15+, but our project uses Java 11. Had to refactor all SQL queries to use standard string concatenation (format) instead.

Prompt:
The FriendsServlet doPost method is returning 500 errors when trying to add a friend. The JSON request body with the username field isn't being parsed correctly.
Issues:
The initial implementation was built using Spring Boot with @RequestBody annotations that handle JSON automatically. Had to manually add Gson parsing with gson.fromJson(req.getReader(), JsonObject.class) to match the project's current servlet structure post-transition.

Prompt:
The getFriendActivity method in FriendDao is returning null timestamps and causing NullPointerException when calling toLocalDateTime() on the created_at field from the reviews table.
Issues:
The SQL query wasn't handling cases where reviews had NULL values in certain fields. Added null checks before calling timestamp methods and used COALESCE in the SQL query to provide default values.

Prompt:
The FriendActivityServlet is returning a 403 Forbidden error even when the user is following the friend. The isFollowing check in FriendDao seems to always return false.
Issues:
The SQL query had the follower_id and followed_id parameters in the wrong order. The logic inverted/opposite as it was checking if the friend was following the user instead of if the user was following the friend. 

Fix:
Refactored all DAO methods to stop closing the singleton database connection by removing Connection from try-with-resources blocks and only closing statements and result sets, matching the pattern in ReviewDao.
Replaced Spring Boot JSON handling with manual Gson parsing and added proper null checks for request body fields.
Refactored all SQL queries from Java 15 text blocks to standard string concatenation for Java 11 compatibility.
Added null checks for timestamp fields and used COALESCE in SQL queries to handle missing data gracefully.
Correctly swapped the parameter order in isFollowing queries to properly check the follower-to-followed relationship direction.

Explanation:
By troubleshooting each error systematically, I could match the Friends System the project's existing architecture used by other features like Reviews and Search. The FriendDao class follows the same singleton DBConnector pattern as ReviewDao, ensuring that there will be consistent database interaction across the codebase. The servlets also handle CORS headers and JSON parsing identically to existing servlets, maintaining frontend compatibility. The six endpoints (GET/POST/DELETE friends, GET followers, GET activity, GET status) provide full friend management functionality while respecting the one-way follow relationship defined in the database schema. Hardcoding userId = 1 enables testing without authentication, matching the temporary approach used by teammates. The resulting implementation satisfies the functional requirements for the Friends System feature and integrates cleanly with the existing backend.