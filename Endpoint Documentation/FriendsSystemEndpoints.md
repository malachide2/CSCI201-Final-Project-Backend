## 1. GET /api/friends

### Purpose
Get list of users the current user is following, with follower/following counts.

### Example Request
```javascript
fetch('/api/friends')
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

---------------------------------------------------------

## 2. POST /api/friends

### Purpose
Follow a user by their username.

### Input
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
- 400: Missing username or trying to follow yourself
- 404: User not found
- 409: Already following this user

---------------------------------------------------------

## 3. DELETE /api/friends?friendUserId=X

### Purpose
Unfollow a user.

### Example Request
```javascript
await fetch("/api/friends?friendUserId=2", {
  method: "DELETE"
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

---------------------------------------------------------

## 4. GET /api/friends/followers

### Purpose
Get list of users following the current user.

### Example Request
```javascript
fetch('/api/friends/followers')
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

---------------------------------------------------------

## 5. GET /api/friends/activity?friendUserId=X

### Purpose
Get a friend's recent activity (their reviews). Requires following the user.

### Parameters
- friendUserId (required): The friend's user ID
- limit (optional): Max items to return (default 20, max 100)

### Example Request
```javascript
fetch('/api/friends/activity?friendUserId=2&limit=10')
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
- 400: Missing friendUserId
- 403: Not following this user
- 404: User not found

---------------------------------------------------------

## 6. GET /api/friends/status?friendUserId=X

### Purpose
Check if the current user is following another user.

### Example Request
```javascript
fetch('/api/friends/status?friendUserId=2')
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