## 1. GET /api/hikes

### Purpose
Search and filter hikes with optional query parameters for search, difficulty, length range, and minimum rating.

### Authentication
Not required - public endpoint

### Input
**Query Parameters (all optional):**
- `q` (string): Search query to match hike name or location (case-insensitive)
- `difficulty` (string): Filter by difficulty - "Easy", "Moderate", "Hard", or "Expert" (or "All" to show all)
- `min_length` (number): Minimum distance in miles
- `max_length` (number): Maximum distance in miles
- `min_rating` (number): Minimum average rating (0.0-5.0)

### Example Request
```javascript
// Get all hikes
fetch('/api/hikes')

// Search for hikes
fetch('/api/hikes?q=angels')

// Filter by difficulty
fetch('/api/hikes?difficulty=Hard')

// Filter by length range
fetch('/api/hikes?min_length=2&max_length=10')

// Filter by minimum rating
fetch('/api/hikes?min_rating=4.0')

// Combined filters
fetch('/api/hikes?q=trail&difficulty=Moderate&min_length=1&max_length=5&min_rating=3.5')
```

### Response JSON
**Success Response (200 OK):**
```json
[
  {
    "hike_id": 1,
    "name": "Angels Landing",
    "location_text": "Zion National Park, Utah",
    "description": "A challenging trail with stunning views...",
    "distance": 5.4,
    "difficulty": 4.0,
    "average_rating": 4.7,
    "thumbnail_url": "/images/hikes/1/photo1.jpg"
  }
]
```

**Note:** 
- Results are ordered by average rating (descending), then by creation date (descending)
- `thumbnail_url` is the first photo uploaded for the hike, or null if no photos exist
- `difficulty` is returned as a number: Easy=1.0, Moderate=2.5, Hard=4.0, Expert=5.0

### Error Responses
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 2. GET /api/hikes/{id}

### Purpose
Get detailed information about a specific hike, including all photos, ratings summary, and creator information.

### Authentication
Not required - public endpoint

### Input
**URL Parameter:**
- `id` (number): The ID of the hike

### Example Request
```javascript
fetch('/api/hikes/1')
```

### Response JSON
**Success Response (200 OK):**
```json
{
  "hike_id": 1,
  "name": "Angels Landing",
  "location_text": "Zion National Park, Utah",
  "description": "A challenging trail with stunning views of Zion Canyon...",
  "distance": 5.4,
  "difficulty": "Hard",
  "elevation": 1488,
  "created_by": 1,
  "created_by_username": "alice",
  "created_at": "2025-01-15T10:30:00",
  "average_rating": 4.7,
  "total_ratings": 23,
  "images": [
    "/images/hikes/1/photo1.jpg",
    "/images/hikes/1/photo2.jpg"
  ]
}
```

**Note:**
- `difficulty` is returned as a string: "Easy", "Moderate", "Hard", or "Expert"
- `images` is an array of all photo URLs for the hike
- `elevation` may be null if not provided
- `created_by` may be null if the creator account was deleted

### Error Responses
- **404 Not Found**: Hike with the specified ID does not exist
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 3. POST /api/hikes/add

### Purpose
Create a new hike with multiple images, optionally store an initial rating and comment. The request must be multipart/form-data because it supports file uploads. Images are processed in parallel using multithreading for improved performance.

### Authentication
The servlet requires that an upstream filter or authenticator sets request attribute "userId" to the authenticated user's ID. If this attribute is missing, the request is rejected with 401 Unauthorized.

### Input
**Content-Type:** `multipart/form-data`

**Form Fields:**
- `name` (required, text): The name of the hike. Duplicate names are not allowed.
- `location` (required, text): The location of the hike.
- `description` (required, text): Description of the hike.
- `difficulty` (required, number): Difficulty rating - must be exactly 1.0 (Easy), 2.5 (Moderate), 4.0 (Hard), or 5.0 (Expert).
- `distance` (required, number): Distance in miles. Must be greater than 0.
- `elevation` (optional, number): Elevation in feet.
- `latitude` (optional, number): Latitude coordinate.
- `longitude` (optional, number): Longitude coordinate.
- `initialRating` (optional, number): Initial rating between 1.0 and 5.0 in 0.5 increments.
- `initialComment` (optional, text): Initial comment/review.
- `images` (required, file): One or more uploaded image files. Accepts JPG, JPEG, PNG, or WEBP. At least one image is required.

### Example Request
```javascript
const formData = new FormData();
formData.append('name', 'Mount Baldy Trail');
formData.append('location', 'San Gabriel Mountains');
formData.append('description', 'A challenging trail with stunning views...');
formData.append('difficulty', '4.0'); // Hard
formData.append('distance', '11.3');
formData.append('elevation', '10064');
formData.append('latitude', '34.2892');
formData.append('longitude', '-117.6469');
formData.append('initialRating', '5.0');
formData.append('initialComment', 'Challenging but rewarding!');
// Add multiple images
formData.append('images', imageFile1);
formData.append('images', imageFile2);
formData.append('images', imageFile3);

await fetch('/api/hikes/add', {
  method: 'POST',
  credentials: 'include', // Required for authentication cookie
  body: formData
});
```

**Example HTML Form:**
```html
<form action="http://localhost:8080/CSCI201-Final-Project-Backend/api/hikes/add" 
      method="post" 
      enctype="multipart/form-data">
  <input type="text" name="name" placeholder="Hike Name" required><br>
  <input type="text" name="location" placeholder="Location" required><br>
  <textarea name="description" placeholder="Description" required></textarea><br>
  <select name="difficulty" required>
    <option value="1.0">Easy</option>
    <option value="2.5">Moderate</option>
    <option value="4.0">Hard</option>
    <option value="5.0">Expert</option>
  </select><br>
  <input type="number" step="0.1" name="distance" placeholder="Distance in miles" required><br>
  <input type="number" name="elevation" placeholder="Elevation (optional)"><br>
  <input type="number" step="0.5" name="initialRating" placeholder="Initial rating (optional)"><br>
  <textarea name="initialComment" placeholder="Initial Comment (optional)"></textarea><br>
  <input type="file" name="images" multiple accept="image/*" required><br><br>
  <button type="submit">Submit Hike</button>
</form>
```

### Response JSON

**Success Response (201 Created):**
```json
{
  "success": true,
  "hikeId": 12,
  "imageUrls": [
    "/images/hikes/12/photo1.jpg",
    "/images/hikes/12/photo2.jpg",
    "/images/hikes/12/photo3.jpg"
  ],
  "initialReviewId": 45
}
```

**Note:** 
- `imageUrls` is an array of all uploaded image paths
- `initialReviewId` is only included if `initialRating` was provided
- Images are stored in `/images/hikes/{hikeId}/` directory
- Images are processed in parallel using multithreading for better performance

### Error Responses
- **400 Bad Request**: Required fields are missing (name, location, description, difficulty, distance).
- **400 Bad Request**: The hike name already exists (case-insensitive check).
- **400 Bad Request**: Distance is invalid (must be greater than 0).
- **400 Bad Request**: Difficulty is invalid (must be 1.0, 2.5, 4.0, or 5.0).
- **400 Bad Request**: At least one image is required.
- **400 Bad Request**: Unsupported image type. Allowed: .jpg, .jpeg, .png, .webp.
- **400 Bad Request**: Invalid initialRating (must be between 1.0 and 5.0 in 0.5 increments).
- **401 Unauthorized**: User not authenticated (no valid JWT cookie).
- **500 Internal Server Error**: Database errors, file system errors, or server-side issues.

### File Storage Behavior
- Images are saved to `<projectRoot>/images/hikes/<hikeId>/` directory
- The servlet creates the directory structure if it does not already exist
- Original filenames are sanitized to prevent path traversal attacks
- Multiple images can be uploaded and are processed in parallel using multithreading
- Each image retains its original filename (sanitized) in the storage path

**Image Processing:**
- Images are processed using an ExecutorService with a thread pool (max 5 threads)
- This allows multiple images to be saved to disk concurrently for better performance
- The multithreading implementation uses `Callable` and `Future` for parallel processing

---------------------------------------------------------
