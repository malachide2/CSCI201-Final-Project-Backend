## 1. GET /api/reviews

### Purpose
Fetch all reviews for a hike, including average rating and total review count.

### Authentication
Not required - public endpoint. However, if authenticated, the response will include `upvotedByCurrentUser` field indicating whether the current user has upvoted each review.

### Input
**Query Parameter:**
- `hikeId` (required, number) - The ID of the hike

### Example Request
```javascript
fetch('/api/reviews?hikeId=1')
```

### Response JSON
**Success Response (200 OK):**
```json
{
  "hikeId": 1,
  "averageRating": 4.7,
  "totalReviews": 7,
  "reviews": [
    {
      "id": 9,
      "hikeId": 1,
      "userId": 1,
      "username": "alice",
      "rating": 5.0,
      "comment": "This trail is fantastic!",
      "upvotes": 3,
      "createdAt": "2025-12-03T12:38:09",
      "upvotedByCurrentUser": false
    }
  ]
}
```

**Note:** 
- `upvotedByCurrentUser` will be `false` if the user is not authenticated or has not upvoted the review
- `upvotes` is the total count of upvotes for the review
- Reviews are typically ordered by creation date (newest first)

### Error Responses
- **400 Bad Request**: Missing required parameter `hikeId`
- **400 Bad Request**: Invalid `hikeId` format
- **500 Internal Server Error**: Database errors or server-side issues
---------------------------------------------------------

## 2. POST /api/reviews

### Purpose
Create a new review for a hike.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Input
**JSON Body:**
```json
{
  "hikeId": number,
  "rating": number (1.0–5.0, must be in 0.5 increments),
  "comment": "string"
}
```

### Example Request
```javascript
await fetch("/api/reviews", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  credentials: "include", // Required for authentication cookie
  body: JSON.stringify({
    hikeId: 1,
    rating: 5.0,
    comment: "Great hike!"
  })
})
```

### Response JSON
**Success Response (201 Created):**
```json
{
  "id": 9,
  "hikeId": 1,
  "userId": 1,
  "username": "alice",
  "rating": 5.0,
  "comment": "Great hike!",
  "upvotes": 0,
  "createdAt": "2025-12-03T12:40:00",
  "upvotedByCurrentUser": false
}
```

### Error Responses
- **400 Bad Request**: Missing required fields (hikeId, rating, comment)
- **400 Bad Request**: Rating must be between 1.0 and 5.0
- **400 Bad Request**: Rating must be in 0.5 increments (e.g., 1.0, 1.5, 2.0, etc.)
- **401 Unauthorized**: User not authenticated
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 3. POST /api/reviews/upvote

### Purpose
Toggle the upvote for a specific review. If the user has already upvoted the review, calling this endpoint will remove the upvote. If the user has not upvoted, it will add an upvote.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Input
**JSON Body:**
```json
{
  "reviewId": number
}
```

### Example Request
```javascript
await fetch("/api/reviews/upvote", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  credentials: "include", // Required for authentication cookie
  body: JSON.stringify({ reviewId: 9 })
})
```

### Response JSON
**Success Response (200 OK):**

**First Call (Upvote Added):**
```json
{
  "reviewId": 9,
  "upvotes": 1,
  "upvoted": true
}
```

**Second Call (Upvote Removed):**
```json
{
  "reviewId": 9,
  "upvotes": 0,
  "upvoted": false
}
```

**Note:** 
- The `upvoted` field indicates whether the review is currently upvoted by the authenticated user after this operation
- The `upvotes` field shows the total count of upvotes for the review after this operation
- Calling the endpoint again toggles the upvote state

### Error Responses
- **400 Bad Request**: Missing required field `reviewId`
- **401 Unauthorized**: User not authenticated
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------
