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

## 2. POST /api/send-code

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

## 3. POST /api/verify-code

**Purpose**

Validates that the security code provided matches the one stored in the database for the given email and checks if it has not expired.

**Input**

**JSON Body:**

```
{
  "email": "string",
  "code": "string" (6 digits)
}
```

**Example Request**

```
await fetch("/api/verify-code", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "alice@example.com",
    code: "123456"
  })
});
```

**Response JSON**

```
{
  "status": "success",
  "message": "Code verified successfully"
}
```

## 4. POST /api/reset-password

**Purpose**

Updates the user's password. This endpoint strictly requires the valid verification code to be present in the request to authorize the change.

**Input**

**JSON Body:**

```
{
  "email": "string",
  "code": "string",
  "newPassword": "string"
}
```

**Example Request**

```
await fetch("/api/reset-password", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "alice@example.com",
    code: "123456",
    newPassword: "newSecurePassword!99"
  })
});
```

**Response JSON**

```
{
  "status": "success",
  "message": "Password updated successfully"
}
```