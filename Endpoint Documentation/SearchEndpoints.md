## 1. GET /api/hikes/

### Purpose
Retrieves a filterable and searchable list of all hiking trails. This single endpoint handles all logic for the Home Page Trail Grid (searching by text, and filtering by numeric ranges and difficulty).

### Authentication
None required. This is a public endpoint accessible by unauthenticated users to browse hikes.

### Input
**Content-Type:** None (Parameters sent via URL Query String)

**Query Parameters:**
- `q` (required, text): Search query for partial matching on hike name and location.
- `difficulty` (optional, number): Filters results by a specific difficulty level. Must be one of the four tiers.
- `min_length` (ptional, number): Minimum hike distance in miles (hikes.distance).
- `max_length` (optional, number): Maximum hike distance in miles (hikes.distance).
- `min_rating` (optional, number): Minimum required average rating for the hike. (0.0 to 5.0).

**Difficulty Mapping:**
The frontend strings map to the database's numeric difficulty column (DECIMAL(2,1)) as follows:

Easy $\rightarrow$ 1.0

Moderate $\rightarrow$ 2.0

Hard $\rightarrow$ 3.0

Expert $\rightarrow$ 4.0


### Example Request
Scenario: Searching for trails with "Peak" in the name/location, rated at least 4.0 stars, and having a distance between 5.0 and 15.0 miles.
```javascript
// Example Fetch Request from Frontend
const params = new URLSearchParams({
  q: 'Peak',
  min_rating: 4.0,
  min_length: 5.0,
  max_length: 15.0,
  difficulty: 'Hard'
});

await fetch(`/api/hikes?${params.toString()}`, {
  method: 'GET'
});
```

Resulting URL: /api/hikes?q=Peak&min_rating=4.0&min_length=5.0&max_length=15.0&difficulty=Hard



### Response JSON

**Success Response (200 OK):**
Returns a JSON array of hike objects, sorted by average rating (descending) then creation date.

```json
[
  {
    "hike_id": 101,
    "name": "Baldy Summit Trail",
    "location_text": "San Gabriel Mountains",
    "distance": 10.2,
    "difficulty": 3.0,
    "average_rating": 4.9,
    "thumbnail_url": "[https://cdn.example.com/images/hike_101.jpg](https://cdn.example.com/images/hike_101.jpg)"
  },
  {
    "hike_id": 102,
    "name": "Bridge to Nowhere",
    "location_text": "Azusa, CA",
    "distance": 9.5,
    "difficulty": 2.0,
    "average_rating": 4.5,
    "thumbnail_url": null
  }
]

```

### Error Responses
- **400 Bad Request**: nvalid input provided for numeric fields (e.g., min_length is not a number).
- **500 Internal Server Error**: Database connection failure or query execution error.