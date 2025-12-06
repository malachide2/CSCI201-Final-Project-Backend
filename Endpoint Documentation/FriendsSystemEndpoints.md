## 1. GET /api/friends

### Purpose
Get list of users the current user is following, with follower/following counts.

### Authentication
Required - user must be authenticated (valid JWT cookie). The current user ID is determined from the authentication token.

### Example Request
```javascript
fetch('/api/friends', {
  credentials: 'include' // Required for authentication cookie
})
```

### Response JSON
```json
{
  "userId": 1,
  "totalFriends": 3,
  "totalFollowers": 5,
  "friends": [
    {
      "userId": 2,
      "username": "alice",
      "email": "alice@usc.edu",
      "friendsSince": "2025-12-03T10:30:00"
    }
  ]
}
```

### Error Responses
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 2. POST /api/friends

### Purpose
Follow a user by their username.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Input
**JSON Body:**
```json
{
  "username": "string"
}
```

### Example Request
```javascript
await fetch("/api/friends", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  credentials: "include", // Required for authentication cookie
  body: JSON.stringify({ username: "alice" })
})
```

### Response JSON
```json
{
  "status": "success",
  "message": "Now following alice",
  "friend": {
    "userId": 2,
    "username": "alice",
    "email": "alice@usc.edu",
    "friendsSince": "2025-12-03T10:30:00"
  }
}
```

### Error Responses
- **400 Bad Request**: Missing required field `username` or username is empty
- **400 Bad Request**: Trying to follow yourself
- **401 Unauthorized**: User not authenticated
- **404 Not Found**: User not found
- **409 Conflict**: Already following this user
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 3. DELETE /api/friends?friendUserId=X

### Purpose
Unfollow a user.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Input
**Query Parameter:**
- `friendUserId` (required, number): The user ID of the friend to unfollow

### Example Request
```javascript
await fetch("/api/friends?friendUserId=2", {
  method: "DELETE",
  credentials: "include" // Required for authentication cookie
})
```

### Response JSON
```json
{
  "status": "success",
  "message": "Successfully unfollowed user",
  "unfollowedUserId": 2
}
```

### Error Responses
- **400 Bad Request**: Missing required parameter `friendUserId` or invalid format
- **400 Bad Request**: You are not following this user
- **401 Unauthorized**: User not authenticated
- **404 Not Found**: User not found
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 4. GET /api/friends/followers

### Purpose
Get list of users following the current user (followers).

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Example Request
```javascript
fetch('/api/friends/followers', {
  credentials: 'include' // Required for authentication cookie
})
```

### Response JSON
```json
{
  "userId": 1,
  "totalFriends": 3,
  "totalFollowers": 5,
  "friends": [
    {
      "userId": 4,
      "username": "charlie",
      "email": "charlie@usc.edu",
      "friendsSince": "2025-12-01T09:15:00"
    }
  ]
}
```

### Error Responses
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 5. GET /api/friends/activity?friendUserId=X

### Purpose
Get a friend's recent activity (their reviews). Requires following the user.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Parameters
**Query Parameters:**
- `friendUserId` (required, number): The friend's user ID
- `limit` (optional, number): Max items to return (default 20, max 100)

### Example Request
```javascript
fetch('/api/friends/activity?friendUserId=2&limit=10', {
  credentials: 'include' // Required for authentication cookie
})
```

### Response JSON
```json
{
  "friendUserId": 2,
  "friendUsername": "alice",
  "totalActivities": 2,
  "activities": [
    {
      "type": "review",
      "id": 15,
      "hikeId": 3,
      "hikeName": "Runyon Canyon",
      "rating": 4.5,
      "comment": "Great views!",
      "createdAt": "2025-12-02T16:45:00",
      "username": "alice"
    }
  ]
}
```

### Error Responses
- **400 Bad Request**: Missing required parameter `friendUserId` or invalid format
- **401 Unauthorized**: User not authenticated
- **403 Forbidden**: Not following this user (must follow to view activity)
- **404 Not Found**: User not found
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## 6. GET /api/friends/status?friendUserId=X

### Purpose
Check if the current user is following another user, and if that user follows back.

### Authentication
Required - user must be authenticated (valid JWT cookie)

### Parameters
**Query Parameter:**
- `friendUserId` (required, number): The user ID to check status for

### Example Request
```javascript
fetch('/api/friends/status?friendUserId=2', {
  credentials: 'include' // Required for authentication cookie
})
```

### Response JSON
```json
{
  "currentUserId": 1,
  "targetUserId": 2,
  "targetUsername": "alice",
  "isFollowing": true,
  "followsBack": false
}
```

### Error Responses
- **400 Bad Request**: Missing required parameter `friendUserId` or invalid format
- **401 Unauthorized**: User not authenticated
- **404 Not Found**: User not found
- **500 Internal Server Error**: Database errors or server-side issues

---------------------------------------------------------

## Frontend Integration Example
```typescript
const addFriend = async (username: string) => {
  const response = await fetch('/api/friends', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username })
  });
  return response.json();
};

const removeFriend = async (friendUserId: number) => {
  const response = await fetch(`/api/friends?friendUserId=${friendUserId}`, {
    method: 'DELETE'
  });
  return response.json();
};

const getFriends = async () => {
  const response = await fetch('/api/friends');
  return response.json();
};

const getFollowers = async () => {
  const response = await fetch('/api/friends/followers');
  return response.json();
};

const getFriendActivity = async (friendUserId: number) => {
  const response = await fetch(`/api/friends/activity?friendUserId=${friendUserId}`);
  return response.json();
};

const checkFollowingStatus = async (friendUserId: number) => {
  const response = await fetch(`/api/friends/status?friendUserId=${friendUserId}`);
  return response.json();
};
```