## 1. POST /api/hikes/add

### Purpose
Create a new hike, optionally store an initial rating and comment, and optionally save an uploaded image to the server's local filesystem. The request must be multipart/form-data because it supports file upload.

### Authentication
The servlet requires that an upstream filter or authenticator sets request attribute "userId" to the authenticated user's ID. If this attribute is missing, the request is rejected with 401 Unauthorized.

### Input
**Content-Type:** `multipart/form-data`

**Form Fields:**
- `name` (required, text): The name of the hike. Duplicate names are not allowed.
- `location` (required, text): The location of the hike.
- `difficulty` (required, number): Difficulty rating between 1.0 and 5.0.
- `distance` (required, number): Distance in miles.
- `elevation` (optional, number): Elevation in feet.
- `rating` (optional, number): Initial rating between 1.0 and 5.0.
- `comment` (optional, text): Initial comment/review.
- `image` (optional, file): Uploaded image file (JPG or PNG).

### Example Request
```javascript
const formData = new FormData();
formData.append('name', 'Mount Baldy Trail');
formData.append('location', 'San Gabriel Mountains');
formData.append('difficulty', '4.5');
formData.append('distance', '11.3');
formData.append('elevation', '10064');
formData.append('rating', '5.0');
formData.append('comment', 'Challenging but rewarding!');
formData.append('image', imageFile);

await fetch('/api/hikes/add', {
  method: 'POST',
  body: formData
});
```

**Example HTML Form:**
```html
<form action="http://localhost:8080/CSCI201-FinalProject/api/hikes/add" 
      method="post" 
      enctype="multipart/form-data">
  <input type="text" name="name" placeholder="Hike Name" required><br>
  <input type="text" name="location" placeholder="Location" required><br>
  <input type="number" step="0.5" name="difficulty" placeholder="Difficulty 1-5" required><br>
  <input type="number" step="0.1" name="distance" placeholder="Distance in miles" required><br>
  <input type="number" name="elevation" placeholder="Elevation (optional)"><br>
  <input type="number" step="0.5" name="rating" placeholder="Initial rating (optional)"><br>
  <textarea name="comment" placeholder="Initial Comment (optional)"></textarea><br>
  <input type="file" name="image"><br><br>
  <button type="submit">Submit Hike</button>
</form>
```

### Response JSON

**Success Response (201 Created):**
```json
{
  "success": true,
  "hikeId": 12,
  "message": "Hike created successfully",
  "imageUrl": "/images/hike_12.jpg"
}
```

**Note:** If no image is uploaded, `imageUrl` will be omitted from the response.

### Error Responses
- **400 Bad Request**: Required fields are missing.
- **400 Bad Request**: The hike name already exists.
- **400 Bad Request**: Rating is invalid (not between 1.0 and 5.0).
- **401 Unauthorized**: No userId attribute is present (user not authenticated).
- **415 Unsupported Media Type**: The uploaded image is not JPG or PNG.
- **500 Internal Server Error**: Database errors or server-side issues.

### File Storage Behavior
If an image is included, it is saved to the `images` folder inside the deployed application folder. The servlet creates this directory if it does not already exist. The stored filename will be `hike_<hikeId>.jpg`.

Images are stored under the webapp's images directory, for example:
```
webapps/CSCI201-FinalProject/images/
```

---------------------------------------------------------
