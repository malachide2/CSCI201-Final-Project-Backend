ACTUAL LOG - Nicolo

Prompt:
I asked the AI to help me implement the “Rating System with Comments” backend for our CSCI final project. I explained that the app uses a Java servlet backend with MySQL and Gson, and my task was to implement endpoints for creating reviews, fetching all reviews for a hike, and toggling upvotes. I also needed guidance on setting up Eclipse with Tomcat, connecting the backend to a local MySQL instance, handling CORS, and preparing the API for the frontend. The endpoints had to handle JSON input, return properly structured JSON responses, and operate correctly without the authentication system finished.

Issues:
My servlets initially failed to deploy because of variable redeclaration errors (duplicate userId), causing Tomcat to continue running stale code.
The DAO layer used a single shared database connection inside try-with-resources blocks, which closed the connection prematurely and triggered the error “Operation not allowed after ResultSet closed.”
The upvote system required authentication, but since no auth system existed yet, every request returned “Not authenticated.”
GET requests always returned "upvotedByCurrentUser": false even after upvoting because no valid user ID was being passed into the DAO.
I also needed to produce clear README-ready API documentation for the frontend team.

Fix:
Removed the duplicate variable declaration and replaced the entire authentication section with a temporary hardcoded userId = 1, allowing POST and upvote requests to succeed during development.
Refactored all DAO methods to stop closing the singleton database connection by removing Connection from try-with-resources blocks and only closing statements and result sets. This eliminated the ResultSet-closed error.
Updated the GET reviews servlet to pass a temporary currentUserId = 1 so that "upvotedByCurrentUser" reflects the correct toggle state.
Corrected CORS headers, cleaned and redeployed the project through Eclipse, restarted Tomcat, and tested each endpoint with curl.
Finally, generated concise, README-formatted endpoint documentation for the frontend team.

Explanation:
These fixes allowed the entire review system—creating reviews, fetching review lists with aggregates, and toggling upvotes—to function correctly in a multithreaded servlet environment. The refactoring ensured safe database interaction despite Tomcat executing requests concurrently. Hardcoding the user ID enabled full endpoint testing even without authentication, and the corrected servlet deployment ensured that modifications were actually executed by Tomcat. The resulting endpoints now accept and return JSON exactly as expected by the frontend, making the system integration-ready. The structured API documentation provides clear guidance for connecting the frontend to the backend. Overall, the completed work now satisfies the functional requirements of the “Rating System with Comments” feature for the final project.



Lucas AI Log

Prompt: 
I asked the AI to help me implement the core Java backend logic for the Search and Filter system (GET /api/hikes) using Servlets, JDBC, and Gson. I provided our database schema,  code snippets I deemed relevant from some of our frontend components (Home.tsx, FilterPanel.tsx), and the project's goal to support search by name/location and filters by difficulty, length, and minimum rating. The final code needed to handle dynamic SQL query construction to prevent SQL injection and return a JSON list of Hike objects.

Issues: 
The AI's initial database logic made assumptions that violated the schema structure, particularly regarding the rating system: the logic required filtering by minRating, but the AI's first drafts assumed a simple rating column existed in the hikes table. The true schema required a more complex join on the reviews table and the use of the AVG(rating) aggregate function within a HAVING clause for filtering. Additionally, the AI failed to fully match the frontend contract. Although the SQL query successfully included a subquery for thumbnail_url, the reponse Hike class initially lacked this field, causing incomplete JSON responses. Finally, the difficulty filter was incorrectly mapped, using strings like 'Medium' instead of the required four specific tiers (Easy, Moderate, Hard, Expert) found in the FilterPanel.tsx component.

Fix: 
I first corrected the database interaction by replacing the assumed single-column filtering with the necessary aggregate logic: dynamically building the SQL query to include the LEFT JOIN and moving the rating comparison into the HAVING clause. I then resolved the incomplete data structure by manually adding the thumbnail_url field to the reponse database.Hike class and updating the mapping in the ResultSet loop. Lastly, I updated the numeric mapping in the Java code to explicitly handle the four difficulty strings (e.g., 'Moderate' <-> 2.0) to ensure precise filtering against the hikes.difficulty decimal column. I also fixed a smaller initial Singleton access error by manually changing the connection call to DBConnector.getInstance().getConnection().

Explanation: 
By manually refactoring the database interaction from simple querying to correct aggregate functions and joins, the SearchServlet accurately calculates and filters data that spans two database tables (hikes and reviews). The resulting code now handles dynamic parameter injection safely, respects the database's numeric difficulty system while communicating using the frontend's string labels, and ensures the JSON response contains all necessary data points, thus fulfilling the contract for the hike grid display.