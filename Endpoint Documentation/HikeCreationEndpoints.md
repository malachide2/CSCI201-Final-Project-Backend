## Add Hike Endpoint (POST /api/hikes/add)
This endpoint creates a new hike, optionally stores an initial rating and comment, and optionally saves an uploaded image to the server’s local filesystem. The request must be multipart/form-data because it supports file upload.

# Request Requirements:
Content-Type: multipart/form-data
Form fields:
name: required text field. The name of the hike. Duplicate names are not allowed.
location: required text field.
difficulty: required number between 1.0 and 5.0.
distance: required number (miles).
elevation: optional number.
rating: optional number between 1.0 and 5.0.
comment: optional text.
image: optional uploaded file (JPG or PNG).

Authentication:
The servlet requires that an upstream filter or authenticator sets request attribute "userId" to the authenticated user's ID. If this attribute is missing, the request is rejected with 401 Unauthorized.

Example HTML test form:

<form action="http://localhost:8080/CSCI201-FinalProject/api/hikes/add" method="post" enctype="multipart/form-data"> <input type="text" name="name" placeholder="Hike Name" required><br> <input type="text" name="location" placeholder="Location" required><br> <input type="number" step="0.5" name="difficulty" placeholder="Difficulty 1-5" required><br> <input type="number" step="0.1" name="distance" placeholder="Distance in miles" required><br> <input type="number" name="elevation" placeholder="Elevation (optional)"><br> <input type="number" step="0.5" name="rating" placeholder="Initial rating (optional)"><br> <textarea name="comment" placeholder="Initial Comment (optional)"></textarea><br> <input type="file" name="image"><br><br> <button type="submit">Submit Hike</button> </form>

# Responses
Success Response:
Status: 201 Created
Example JSON:
{
"success": true,
"hikeId": 12,
"message": "Hike created successfully",
"imageUrl": "/images/hike_12.jpg"
}
If no image is uploaded, imageUrl will be omitted. Images are stored under the webapp’s images directory, for example:
webapps/CSCI201-FinalProject/images/
Error Responses:
400 Bad Request if required fields are missing.
400 Bad Request if the hike name already exists.
400 Bad Request if rating is invalid.
401 Unauthorized if no userId attribute is present.
415 Unsupported Media Type if the uploaded image is not JPG or PNG.
500 Internal Server Error for database errors.
File Storage Behavior:
If an image is included, it is saved to the images folder inside the deployed application folder. The servlet creates this directory if it does not already exist. The stored filename will be hike_<hikeId>.jpg.