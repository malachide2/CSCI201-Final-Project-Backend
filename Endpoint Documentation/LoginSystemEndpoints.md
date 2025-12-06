# Authentication & Security API Endpoints

## 1. POST /api/login

**Purpose**

Authenticates a user by verifying their email and password against the database. Upon success, it generates a JWT and returns it as an **HttpOnly cookie** for session management.

**Input**

**JSON Body:**

```
{
  "email": "string",
  "password": "string"
}
```

**Example Request**

```
await fetch("/api/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "alice@example.com",
    password: "secretpassword123"
  })
});
```

**Response JSON**

*Note: The actual JWT is sent in the `Set-Cookie` header, not in the JSON body.*

```
{
  "status": "success",
  "user_id": 1,
  "message": "Login successful"
}
```

## 2. POST /api/signup

**Purpose**

Creates a new user account with username, email, and password. Upon success, it generates a JWT and returns it as an **HttpOnly cookie** for session management.

**Input**

**JSON Body:**

```
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Example Request**

```
await fetch("/api/signup", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    username: "alice",
    email: "alice@example.com",
    password: "secretpassword123"
  })
});
```

**Response JSON**

*Note: The actual JWT is sent in the `Set-Cookie` header, not in the JSON body.*

**Success Response:**
```
{
  "status": "success",
  "user_id": 1,
  "message": "Signup successful"
}
```

**Error Responses:**
- **400 Bad Request**: Missing required fields (email, password, or username)
- **409 Conflict**: Email already registered
- **409 Conflict**: Username already taken
- **500 Internal Server Error**: Server error during user creation

## 3. POST /api/send-code

**Purpose**

Generates a secure 6-digit verification code, stores it in the database with a 15-minute expiration, and sends it to the provided email address via the Email Service.

**Input**

**JSON Body:**

```
{
  "email": "string"
}
```

**Example Request**

```
await fetch("/api/send-code", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "alice@example.com"
  })
});
```

**Response JSON**

```
{
  "status": "success",
  "message": "Verification code sent to email"
}
```

**Note:** The verification code is sent via email using the configured email service. For testing purposes, you may need to configure email settings in `PassResetEmail.java` or check backend logs if email sending is disabled.

**Error Responses:**
- **400 Bad Request**: Email is required
- **404 Not Found**: Email not registered
- **500 Internal Server Error**: Failed to send email or database error storing code

---

**Note:** The following endpoints (`/api/verify-code` and `/api/reset-password`) are documented but not yet implemented in the codebase. They are part of the planned password reset flow.