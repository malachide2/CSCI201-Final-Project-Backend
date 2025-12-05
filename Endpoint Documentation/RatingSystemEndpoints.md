## 1 GET /api/reviews

### Purpose
Fetch all reviews for a hike, including average rating and total review count.

### Input
**Query Parameter:**
- `hikeId` (number) - The ID of the hike

### Example Request
```javascript
fetch(`/api/reviews?hikeId=1`)
```

### Response JSON
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
      "upvotes": 0,
      "createdAt": "2025-12-03T12:38:09",
      "upvotedByCurrentUser": false
    }
  ]
}
```
---------------------------------------------------------

## 2 POST /api/reviews

### Purpose
Create a new review for a hike.

### Input
**JSON Body:**
```json
{
  "hikeId": number,
  "rating": number (1.0–5.0, increments of 0.5),
  "comment": "string"
}
```

### Example Request
```javascript
await fetch("/api/reviews", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    hikeId: 1,
    rating: 5.0,
    comment: "Great hike!"
  })
})
```

### Response JSON
**Created Review:**
```json
{
  "id": 9,
  "hikeId": 1,
  "userId": 1,
  "username": "alice",
  "rating": 5,
  "comment": "Great hike!",
  "upvotes": 0,
  "createdAt": "2025-12-03T12:40:00",
  "upvotedByCurrentUser": false
}
```

---------------------------------------------------------

## 3 POST /api/reviews/upvote

### Purpose
Toggle the upvote for a specific review.

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
  body: JSON.stringify({ reviewId: 9 })
})
```

### Response JSON

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

> **Note:** Calling the endpoint again toggles the upvote off.

---------------------------------------------------------
