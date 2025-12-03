## API Endpoints Documentation


# Rating System with Comments - Nicolo
### New Rating Endpoints

#### 1. GET `/api/hikes/{hikeId}/ratings`
Get all ratings for a specific hike.

**Path Parameters:**
- `hikeId` (Long) - The ID of the hike

**Request Body:** None

**Response:** `200 OK` - Array of `RatingDto`
```json
[
  {
    "id": "string",              // Review ID as string
    "hikeId": "string",          // Hike ID as string
    "userId": "string",          // User ID as string
    "rating": 4.5,               // Number (1.0-5.0, 0.5 increments)
    "comment": "string",         // Review comment text
    "upvotes": 5,                // Integer count
    "upvotedBy": ["1", "2"],    // Array of user ID strings who upvoted
    "images": [],                // Array of image URLs (empty for now)
    "createdAt": "2024-01-15T10:30:00Z"  // ISO 8601 date string
  }
]
```

**Error Responses:**
- `404 Not Found` - Hike not found
- `500 Internal Server Error`

---

#### 2. POST `/api/hikes/{hikeId}/ratings`
Create or update a rating for a hike. Each user can only have one rating per hike (updates existing if present).

**Path Parameters:**
- `hikeId` (Long) - The ID of the hike

**Request Body:** `CreateOrUpdateRatingRequest`
```json
{
  "rating": 4.5,                 // Required: Number (1.0-5.0, 0.5 increments)
  "comment": "Great hike!",      // Optional: String (max 2000 characters)
  "images": []                   // Optional: Array of image URLs (ignored for now)
}
```

**Response:** `200 OK` - `RatingDto` (same structure as GET response above)

**Error Responses:**
- `400 Bad Request` - Validation error (invalid rating, comment too long, etc.)
- `401 Unauthorized` - User not authenticated
- `404 Not Found` - Hike not found
- `500 Internal Server Error`

---

#### 3. PUT `/api/ratings/{reviewId}/upvote`
Set the upvote state for a specific rating.

**Path Parameters:**
- `reviewId` (Long) - The ID of the review/rating

**Request Body:** `UpvoteRequest`
```json
{
  "upvoted": true                // Required: Boolean (true to upvote, false to remove upvote)
}
```

**Response:** `200 OK` - `RatingDto` (updated rating with new upvote count and upvotedBy list)

**Error Responses:**
- `400 Bad Request` - Missing upvoted field
- `401 Unauthorized` - User not authenticated
- `404 Not Found` - Review not found
- `500 Internal Server Error`

---

### Legacy Review Endpoints (Still Available)

#### 4. GET `/api/hikes/{hikeId}/reviews`
Get all reviews for a hike (legacy format with additional metadata).

**Path Parameters:**
- `hikeId` (Long) - The ID of the hike

**Request Body:** None

**Response:** `200 OK` - `HikeReviewsResponseDto`
```json
{
  "hikeId": 1,                   // Long
  "averageRating": 4.2,          // Double
  "totalReviews": 10,             // Integer
  "reviews": [
    {
      "reviewId": 1,              // Long
      "rating": 4.5,              // Double
      "reviewBody": "string",     // String
      "authorUsername": "string", // String
      "authorId": 1,              // Long
      "upvotesCount": 5,          // Integer
      "currentUserUpvoted": true, // Boolean
      "createdAt": "2024-01-15T10:30:00Z",  // ISO Instant
      "updatedAt": "2024-01-15T10:30:00Z"   // ISO Instant
    }
  ]
}
```

**Error Responses:**
- `404 Not Found` - Hike not found
- `500 Internal Server Error`

---

#### 5. POST `/api/hikes/{hikeId}/reviews`
Create or update a review (legacy format).

**Path Parameters:**
- `hikeId` (Long) - The ID of the hike

**Request Body:** `CreateOrUpdateReviewRequest`
```json
{
  "rating": 4.5,                  // Required: Number (1.0-5.0, 0.5 increments)
  "reviewBody": "Great hike!"     // Optional: String (max 2000 characters)
}
```

**Response:** `200 OK` - `ReviewResponseDto` (same structure as in GET response above)

**Error Responses:**
- `400 Bad Request` - Validation error
- `401 Unauthorized` - User not authenticated
- `404 Not Found` - Hike not found
- `500 Internal Server Error`

---

#### 6. POST `/api/reviews/{reviewId}/upvote`
Toggle upvote for a review (legacy - no request body, just toggles state).

**Path Parameters:**
- `reviewId` (Long) - The ID of the review

**Request Body:** None

**Response:** `200 OK` - `ReviewResponseDto` (toggled upvote state)

**Error Responses:**
- `401 Unauthorized` - User not authenticated
- `404 Not Found` - Review not found
- `500 Internal Server Error`
