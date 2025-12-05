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