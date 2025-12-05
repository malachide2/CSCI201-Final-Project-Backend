Prompt: Implement the backend logic for a user authentication login system including password verification and session management. The code should accept JSON credentials, verify them against a database, and establish a secure session.

Issues: Standard Servlet request.getParameter methods do not handle raw JSON bodies sent by modern frontends. Additionally, returning JWTs in the response body allows frontend JavaScript to access them, which poses an XSS vulnerability risk. Password verification needs to handle hashed values, not plain text. 

Fix: Implemented LoginServlet using the Gson library to parse the HttpServletRequest reader directly into a JSON object. Integrated BCrypt.checkpw to verify the raw password against the stored hash retrieved via DBConnect. Instead of returning the token in the JSON body, the JWT is encapsulated in a Set-Cookie header with flags HttpOnly, Secure, and SameSite=Strict, ensuring the browser manages the session securely.

Prompt: Create a database connector capable of handling user lookups and managing temporary verification codes for the password reset feature. The storage mechanism for codes must handle cases where a user requests a code multiple times before the first one expires. 

Issues: A simple INSERT statement for storing verification codes would fail if a primary key (email) already exists in the table. We need a way to update the existing code and expiration time if the user requests a resend, rather than creating duplicate rows or throwing SQL errors. 

Fix: Developed the DBConnect class with specific methods for user retrieval (getPasswordHash, getUserId) and code storage. Implemented storeSecurityCode using the SQL ON DUPLICATE KEY UPDATE syntax. This allows the system to seamlessly "upsert" (update or insert) the verification code and reset the 15-minute expiration timer without requiring complex logic to check for existence first.

Prompt: Develop a system to generate, store, and email time-sensitive verification codes for users attempting to reset their passwords.

Issues: The system needs to generate a cryptographically secure code to prevent prediction attacks. The email functionality needs to be isolated from the servlet logic to allow for reuse and cleaner code structure. The email transport needs to handle TLS negotiation with the SMTP provider. 

Fix: Created CodeSendingServlet which utilizes SecureRandom to generate a 6-digit integer. This servlet orchestrates the flow by calling DBConnect to save the code and then delegating the actual transmission to a new PassResetEmail class. PassResetEmail was built using jakarta.mail, configured with Gmail's SMTP settings (port 587, STARTTLS), and formats the message as HTML for better user experience.


Explanation: This implementation establishes the login system for a website. The use of Gson ensures compatibility with the frontend's JSON-based API calls, while BCrypt and SecureRandom take care of the credential handling. It uses HttpOnly cookies for JWT storage to ensure the site is secure by making the session token inaccessible to potential XSS exploits. Logic in the database layer ensures the password reset keeps the people from resetting to often.
