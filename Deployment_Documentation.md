---

# Hike Application
## Full Stack Deployment Documentation

---

**Version:** 1.0  
**Last Updated:** 2025  
**Document Type:** Technical Documentation

---

## CSCI 201 Final Project

### Full Stack Deployment Guide

This document provides comprehensive instructions for running and deploying the Hike Application (frontend and backend) on your local machine and in a production environment.

---

**Development Team:** CSCI 201 Final Project Team 20 
**Backend Repository:** CSCI201-Final-Project-Backend  
**Frontend Repository:** CSCI201-Final-Project-Frontend (separate repository)
**Backend:** Java Servlet Web Application (Apache Tomcat 10+)  
**Frontend:** React 19.2.0 with TypeScript, Vite 7.2.2  
**Database:** MySQL 8.0+

---

## Document Information

- **Purpose:** Guide for setting up, running, and deploying the Hike Application (frontend and backend)
- **Target Audience:** Developers, System Administrators, DevOps Engineers
- **Prerequisites:** Basic knowledge of Java, React/TypeScript, MySQL, and web application deployment
- **Related Documents:** 
  - Endpoint Documentation
  - Rating System README

---

## Overview

The Hike Application is a full-stack web application consisting of:

- **Frontend**: React 19.2.0 with TypeScript, built with Vite 7.2.2, running on port 5173 (development) or port 80/443 (production)
- **Backend**: Java Servlet API providing RESTful endpoints, running on port 8080 (default) or configured production port
- **Database**: MySQL 8.0+ for data persistence

The frontend communicates with the backend via REST API calls. The backend provides endpoints for:
- User authentication (login, signup, password reset)
- Hike management (create, search, filter, view details)
- Reviews and ratings (create, upvote, view)
- User profiles and friend management
- Image uploads

This guide covers everything needed to get both the frontend and backend running locally and deployed in a production environment.

---


## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Configuration](#database-configuration)
4. [Backend Configuration](#backend-configuration)
5. [Frontend Application Setup](#frontend-application-setup)
6. [Building the Applications](#building-the-applications)
7. [Running Locally](#running-locally)
8. [CORS Configuration](#cors-configuration)
9. [Full Stack Integration](#full-stack-integration)
10. [Production Deployment](#production-deployment)
11. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before setting up the application, ensure you have the following installed:

### Required Software

#### Backend Requirements
- **Java Development Kit (JDK)**: Version 11 or higher
  - Verify installation: `java -version`
- **MySQL Server**: Version 8.0 or higher
  - Verify installation: `mysql --version`
- **Apache Tomcat**: Version 10.x or higher (for Jakarta Servlet API)
  - Download from: https://tomcat.apache.org/
- **Maven** (optional, if using Maven build): Version 3.6 or higher
  - Verify installation: `mvn -version`

#### Frontend Requirements
- **Node.js**: Version 18 or higher
  - Verify installation: `node --version`
  - Download from: https://nodejs.org/
- **npm** or **yarn**: Comes with Node.js
  - Verify installation: `npm --version` or `yarn --version`

### Required Libraries
The following JAR files are already included in `src/main/webapp/WEB-INF/lib/`:
- `gson-2.9.1.jar` - JSON processing
- `mysql-connector-j-8.3.0.jar` - MySQL JDBC driver
- `jackson-*.jar` - JSON processing
- `jakarta.mail-2.0.1.jar` - Email functionality
- `jbcrypt-0.4.jar` - Password hashing
- `jjwt-*.jar` - JWT token handling

---

## Local Development Setup

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd CSCI201-Final-Project-Backend
```

### Step 2: Set Up MySQL Database

1. **Start MySQL Server**
   ```bash
   # On macOS/Linux
   sudo systemctl start mysql
   # Or use MySQL service manager
   
   # On Windows
   # Start MySQL service from Services panel
   ```

2. **Create the Database**
   ```bash
   mysql -u root -p < setup.sql
   ```
   
   Or manually:
   ```bash
   mysql -u root -p
   ```
   Then execute:
   ```sql
   source setup.sql;
   ```

3. **Verify Database Creation**
   ```sql
   USE hike_app;
   SHOW TABLES;
   ```
   You should see: `users`, `hikes`, `reviews`, `review_upvotes`, `photos`, `friends`

---

## Database Configuration

The application requires database connection configuration in two files:

### 1. Update `DBConnector.java`
Edit `src/main/java/database/DBConnector.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/hike_app";
private static final String USER = "root";  // Change to your MySQL username
private static final String PASSWORD = "your_password_here";  // Change to your MySQL password
```

### 2. Update `DBConnect.java`
Edit `src/main/java/LoginService/DBConnect.java`:

```java
private String url = "jdbc:mysql://localhost:3306/hike_app";
private String username = "root";  // Change to your MySQL username
private String password = "your_password_here";  // Change to your MySQL password
```

**Important**: Replace `your_password_here` with your actual MySQL root password (or create a dedicated database user).

---

## Backend Configuration

### Email Configuration (Optional - for password reset functionality)

**Important**: Any registered user can request a password reset and will receive the reset code at their own registered email address.

If you plan to use the password reset feature, you need to configure the email account that your application will use to send password reset emails. Update `src/main/java/LoginService/PassResetEmail.java`:

```java
private static final String SENDER_EMAIL = "your_email@gmail.com";
private static final String SENDER_PASSWORD = "your_app_password";  // Use Gmail App Password
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
```

**What this means:**
- `SENDER_EMAIL`: This is the email account your application uses to **send** password reset emails (the "From" address). This is NOT where users receive their reset codes.
- `SENDER_PASSWORD`: The password for authenticating with the email service (Gmail SMTP server).
- **Users receive emails**: When a user requests a password reset, they provide their registered email address, and the reset code is sent **to their email address**, not to the SENDER_EMAIL.

**Example:**
- You configure: `SENDER_EMAIL = "myapp@gmail.com"` (your app's sending account)
- User requests reset with their email: `"user@example.com"` (their registered email)
- Result: Email is sent **FROM** `myapp@gmail.com` **TO** `user@example.com`

**Note**: For Gmail, you'll need to:
1. Enable 2-Factor Authentication on the Gmail account you're using as SENDER_EMAIL
2. Generate an App Password (not your regular password) for that account
3. Use the App Password in the `SENDER_PASSWORD` field

**Security Recommendation**: For production, consider using environment variables or a configuration file instead of hardcoding credentials. This prevents sensitive information from being committed to version control.

---

## Frontend Application Setup

### Step 1: Clone the Frontend Repository

```bash
# Navigate to your projects directory
cd /path/to/your/projects

# Clone the frontend repository (adjust URL as needed)
git clone <frontend-repository-url>
cd CSCI201-Final-Project-Frontend
```

### Step 2: Install Dependencies

```bash
# Using npm
npm install

# Or using yarn
yarn install
```

This will install all required dependencies including:
- React 19.2.0
- TypeScript
- Vite 7.2.2
- React Router DOM 7.9.6
- Tailwind CSS 4.1.17
- Radix UI components
- shadcn/ui components
- Lucide React icons

### Step 3: Configure Environment Variables

Create a `.env` file in the frontend root directory:

```bash
# Development
VITE_API_BASE_URL=http://localhost:8080/hikeapp

# Production (update with your production backend URL)
# VITE_API_BASE_URL=https://api.yourdomain.com
```

**Important**: The frontend uses `VITE_API_BASE_URL` to connect to the backend API. Make sure this matches your backend server URL.

### Step 4: Verify Frontend Structure

The frontend has the following structure:
```
src/
├── App.tsx                 # Main app component with routing
├── main.tsx                # Entry point
├── index.css               # Global styles
├── types/
│   └── index.ts            # TypeScript type definitions
├── contexts/
│   └── AuthContext.tsx     # Authentication state management
├── pages/                  # Page components
├── components/             # Reusable components
└── data/
    └── dummy-data.ts       # Mock data (for development)
```

---

## Building the Applications

### Building the Backend

#### Option 1: Using an IDE (Recommended for Development)

1. **Eclipse/IntelliJ IDEA Setup**
   - Import the project as a Java Web Application
   - Ensure the project structure follows:
     ```
     src/main/java/     - Java source files
     src/main/webapp/   - Web resources (JSP, static files)
     ```
   - Configure the build path to include all JARs from `src/main/webapp/WEB-INF/lib/`
   - Set the output directory to `bin/` or configure your IDE's build output

2. **Build the Project**
   - In Eclipse: Right-click project → Build Project
   - In IntelliJ: Build → Build Project

#### Option 2: Manual Compilation

1. **Compile Java Files**
   ```bash
   # Navigate to backend project root
   cd /path/to/CSCI201-Final-Project-Backend
   
   # Create output directory
   mkdir -p bin/WEB-INF/classes
   
   # Compile all Java files
   javac -cp "src/main/webapp/WEB-INF/lib/*" \
         -d bin/WEB-INF/classes \
         src/main/java/**/*.java
   ```

2. **Copy Web Resources**
   ```bash
   # Copy webapp contents to bin directory
   cp -r src/main/webapp/* bin/
   ```

### Building the Frontend

#### Development Build

The frontend uses Vite, which provides hot module replacement (HMR) for development:

```bash
# Navigate to frontend directory
cd /path/to/CSCI201-Final-Project-Frontend

# Start development server
npm run dev
# or
yarn dev
```

This will start the Vite dev server on `http://localhost:5173` (default port).

#### Production Build

To create an optimized production build:

```bash
# Navigate to frontend directory
cd /path/to/CSCI201-Final-Project-Frontend

# Build for production
npm run build
# or
yarn build
```

This creates an optimized build in the `dist/` directory that can be served by any static file server (nginx, Apache, etc.).

**Note**: Make sure your `.env` file has the correct `VITE_API_BASE_URL` for production before building.

---

## Running Locally

### Running the Backend

#### Using Apache Tomcat

1. **Configure Tomcat**
   - Copy the built application to Tomcat's `webapps` directory:
     ```bash
     # Stop Tomcat if running
     # Copy your application
     cp -r bin/ $CATALINA_HOME/webapps/hikeapp
     ```
   
   Or create a WAR file:
   ```bash
   cd bin
   jar cvf hikeapp.war *
   cp hikeapp.war $CATALINA_HOME/webapps/
   ```

2. **Start Tomcat**
   ```bash
   $CATALINA_HOME/bin/startup.sh  # Linux/macOS
   # or
   $CATALINA_HOME/bin/startup.bat  # Windows
   ```

3. **Verify Backend is Running**
   - Backend API will be available at: `http://localhost:8080/hikeapp/`
   - Check Tomcat logs for: "Database connected successfully."
   - Test an endpoint: `curl http://localhost:8080/hikeapp/api/hello`

#### Using an IDE's Built-in Server

1. **Eclipse**
   - Right-click project → Run As → Run on Server
   - Select Tomcat server
   - Application will be deployed automatically

2. **IntelliJ IDEA**
   - Run → Edit Configurations
   - Add → Tomcat Server → Local
   - Configure deployment artifact
   - Run the configuration

### Running the Frontend

1. **Start Development Server**
   ```bash
   # Navigate to frontend directory
   cd /path/to/CSCI201-Final-Project-Frontend

   # Start Vite dev server
   npm run dev
   # or
   yarn dev
   ```

2. **Access the Frontend**
   - Frontend will be available at: `http://localhost:5173`
   - The Vite dev server provides hot module replacement (HMR) for instant updates

### Running Both Together

For full-stack development, you need both servers running:

1. **Terminal 1 - Backend**
   ```bash
   # Start Tomcat (backend on port 8080)
   $CATALINA_HOME/bin/startup.sh
   ```

2. **Terminal 2 - Frontend**
   ```bash
   # Start Vite dev server (frontend on port 5173)
   cd /path/to/CSCI201-Final-Project-Frontend
   npm run dev
   ```

3. **Access the Application**
   - Open your browser and navigate to: `http://localhost:5173`
   - The frontend will make API calls to `http://localhost:8080/hikeapp/api/*`

### Verify Full Stack Installation

1. **Test Backend API**
   - Check Tomcat logs for: "Database connected successfully."
   - Test endpoint: `curl http://localhost:8080/hikeapp/api/hello`
   - Verify database connection in logs

2. **Test Frontend**
   - Open `http://localhost:5173` in your browser
   - Check browser console for any errors
   - Verify API calls are being made to the backend

3. **Test Integration**
   - Try logging in or creating an account
   - Check browser Network tab to see API requests
   - Verify responses are coming from the backend

---

## CORS Configuration

Cross-Origin Resource Sharing (CORS) must be configured on the backend to allow the frontend (running on a different port/domain) to make API requests.

### Current CORS Configuration

The backend servlets already include CORS headers. Check the following files:

- `src/main/java/servlets/CreateReviewServlet.java`
- `src/main/java/servlets/ToggleReviewUpvoteServlet.java`
- `src/main/java/servlets/AddHikeServlet.java`

### Development CORS Setup

For local development, the frontend runs on `http://localhost:5173` and the backend on `http://localhost:8080`. The CORS headers should allow the frontend origin:

```java
private void setCorsHeaders(HttpServletResponse resp) {
    resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
    resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    resp.setHeader("Access-Control-Allow-Credentials", "true");
}
```

### Production CORS Setup

For production, update the CORS headers to match your frontend domain:

```java
private void setCorsHeaders(HttpServletResponse resp) {
    resp.setHeader("Access-Control-Allow-Origin", "https://yourdomain.com");
    resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    resp.setHeader("Access-Control-Allow-Credentials", "true");
}
```

### Handling OPTIONS Requests

For preflight requests, add an OPTIONS handler in your servlets:

```java
@Override
protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
    setCorsHeaders(resp);
    resp.setStatus(HttpServletResponse.SC_OK);
}
```

### CORS Troubleshooting

If you see CORS errors in the browser console:

1. **Check CORS headers**: Verify the `Access-Control-Allow-Origin` header matches your frontend URL exactly
2. **Check OPTIONS requests**: Ensure OPTIONS requests are handled properly
3. **Check credentials**: If using cookies/auth tokens, ensure `Access-Control-Allow-Credentials: true`
4. **Browser console**: Check the Network tab for CORS-related errors

---

## Full Stack Integration

### API Endpoints

The frontend expects the following backend API endpoints:

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/send-code` - Send password reset code
- `POST /api/auth/logout` - User logout (optional)

#### Hikes
- `GET /api/hikes` - Get all hikes (with optional query parameters: search, difficulty, minLength, maxLength, minRating)
- `GET /api/hikes/:id` - Get hike by ID
- `POST /api/hikes` - Create new hike
- `GET /api/users/:id/hikes` - Get hikes by user

#### Reviews/Ratings
- `GET /api/hikes/:id/ratings` - Get ratings for a hike
- `POST /api/ratings` - Create new rating/review
- `PUT /api/ratings/:id/upvote` - Toggle upvote on a rating

#### Users
- `GET /api/users/:id` - Get user by ID
- `GET /api/users/search?username=...` - Search users by username
- `GET /api/users/:id/friends` - Get user's friends
- `POST /api/friends` - Add friend
- `DELETE /api/friends/:friendId` - Remove friend

### Environment Variables

#### Frontend (.env)
```bash
# Development
VITE_API_BASE_URL=http://localhost:8080/hikeapp

# Production
VITE_API_BASE_URL=https://api.yourdomain.com
```

#### Backend
Currently uses hardcoded values in:
- `DBConnector.java` - Database connection
- `DBConnect.java` - Database connection
- `PassResetEmail.java` - Email configuration

**Recommendation**: Use environment variables or configuration files for production.

### Authentication Flow

1. **Login/Signup**: Frontend sends credentials to `/api/auth/login` or `/api/auth/signup`
2. **Token Storage**: Backend returns JWT token, frontend stores in localStorage
3. **Authenticated Requests**: Frontend includes token in `Authorization` header or as cookie
4. **Token Validation**: Backend validates token on protected endpoints

### Data Flow

1. **User Action**: User interacts with frontend (e.g., clicks "Add Hike")
2. **API Request**: Frontend makes HTTP request to backend API endpoint
3. **Backend Processing**: Backend validates request, queries database, processes data
4. **Response**: Backend returns JSON response
5. **UI Update**: Frontend updates UI based on response

---

## Production Deployment

### Backend Deployment

#### Option 1: Deploy to Apache Tomcat Server

1. **Prepare the Application**
   ```bash
   # Build the application (see Building section)
   # Create WAR file
   cd bin
   jar cvf hikeapp.war *
   ```

2. **Deploy to Production Server**
   - Copy `hikeapp.war` to `$CATALINA_HOME/webapps/`
   - Tomcat will automatically extract and deploy
   - Or use Tomcat Manager for remote deployment

3. **Configure Production Database**
   - Update database connection strings in `DBConnector.java` and `DBConnect.java` to point to production MySQL server
   - Ensure MySQL server is accessible from the application server
   - Use a dedicated database user (not root) with appropriate permissions

4. **Configure Tomcat for Production**
   - Edit `$CATALINA_HOME/conf/server.xml`:
     - Set appropriate port (default 8080)
     - Configure SSL/HTTPS if needed
   - Set `JAVA_OPTS` for memory allocation:
     ```bash
     export JAVA_OPTS="-Xms512m -Xmx1024m"
     ```

5. **Set Up as a Service (Linux)**
   ```bash
   # Create systemd service file
   sudo nano /etc/systemd/system/tomcat.service
   ```
   Add:
   ```ini
   [Unit]
   Description=Apache Tomcat
   After=network.target
   
   [Service]
   Type=forking
   User=tomcat
   Group=tomcat
   Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk"
   Environment="CATALINA_HOME=/opt/tomcat"
   ExecStart=/opt/tomcat/bin/startup.sh
   ExecStop=/opt/tomcat/bin/shutdown.sh
   
   [Install]
   WantedBy=multi-user.target
   ```
   Enable and start:
   ```bash
   sudo systemctl enable tomcat
   sudo systemctl start tomcat
   ```

### Option 2: Deploy to Cloud Platform

#### AWS Elastic Beanstalk
1. Create a WAR file
2. Use AWS Elastic Beanstalk Java platform
3. Upload WAR file through console or CLI
4. Configure environment variables for database credentials

#### Google Cloud Platform (App Engine)
1. Create `appengine-web.xml` configuration
2. Deploy using: `gcloud app deploy`

#### Heroku
1. Add `Procfile`:
   ```
   web: java $JAVA_OPTS -jar webapp-runner.jar --port $PORT hikeapp.war
   ```
2. Deploy using Git or Heroku CLI

### Frontend Deployment

#### Option 1: Static File Hosting (Recommended)

The frontend builds to static files that can be served by any web server:

1. **Build for Production**
   ```bash
   cd /path/to/CSCI201-Final-Project-Frontend
   
   # Update .env with production API URL
   echo "VITE_API_BASE_URL=https://api.yourdomain.com" > .env.production
   
   # Build
   npm run build
   # or
   yarn build
   ```

2. **Deploy to Static Hosting**
   - **Netlify**: Drag and drop the `dist/` folder or connect via Git
   - **Vercel**: Connect repository or deploy `dist/` folder
   - **AWS S3 + CloudFront**: Upload `dist/` to S3 bucket, configure CloudFront
   - **GitHub Pages**: Push `dist/` contents to `gh-pages` branch

3. **Configure Custom Domain** (if needed)
   - Update DNS records to point to your hosting provider
   - Configure SSL certificate (usually automatic with modern hosting)

#### Option 2: Serve with Nginx

1. **Build the Frontend**
   ```bash
   cd /path/to/CSCI201-Final-Project-Frontend
   npm run build
   ```

2. **Configure Nginx**
   ```nginx
   server {
       listen 80;
       server_name yourdomain.com;
       
       root /var/www/hikeapp-frontend/dist;
       index index.html;
       
       # Serve static files
       location / {
           try_files $uri $uri/ /index.html;
       }
       
       # Proxy API requests to backend
       location /api {
           proxy_pass http://localhost:8080/hikeapp;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

3. **Copy Files**
   ```bash
   sudo cp -r dist/* /var/www/hikeapp-frontend/
   sudo systemctl restart nginx
   ```

#### Option 3: Serve with Apache Tomcat

You can serve the frontend from the same Tomcat instance:

1. **Build the Frontend**
   ```bash
   cd /path/to/CSCI201-Final-Project-Frontend
   npm run build
   ```

2. **Deploy to Tomcat**
   ```bash
   # Copy built files to Tomcat webapps
   cp -r dist/* $CATALINA_HOME/webapps/ROOT/
   ```

3. **Configure API Proxy** (if needed)
   - Use Tomcat's reverse proxy or configure Apache HTTP Server in front

### Full Stack Production Deployment

For a complete production setup:

1. **Backend**: Deploy to Tomcat on port 8080 (or configured port)
2. **Frontend**: Build and deploy to static hosting or serve via Nginx
3. **Database**: Ensure MySQL is accessible from backend server
4. **CORS**: Update CORS headers to match production frontend domain
5. **Environment Variables**: Set production API URL in frontend `.env`
6. **SSL/HTTPS**: Configure SSL certificates for both frontend and backend
7. **Domain Configuration**:
   - Frontend: `https://yourdomain.com`
   - Backend API: `https://api.yourdomain.com` or `https://yourdomain.com/api`

### Production Best Practices

1. **Security**
   - Never commit database passwords or API keys to version control
   - Use environment variables or secure configuration management
   - Enable HTTPS/SSL
   - Implement proper authentication and authorization
   - Regularly update dependencies

2. **Database**
   - Use connection pooling (configure in `context.xml`)
   - Set up database backups
   - Monitor database performance
   - Use read replicas for scaling

3. **Application Server**
   - Configure appropriate JVM memory settings
   - Set up log rotation
   - Monitor application health
   - Implement load balancing for high availability

4. **Image Storage**
   - For production, consider using cloud storage (AWS S3, Google Cloud Storage) instead of local file system
   - Update `LocalImageStorage.java` to use cloud storage APIs

---

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running: `sudo systemctl status mysql`
   - Check database credentials in `DBConnector.java` and `DBConnect.java`
   - Ensure database `hike_app` exists: `mysql -u root -p -e "SHOW DATABASES;"`
   - Check MySQL user permissions

2. **ClassNotFoundException**
   - Ensure all JAR files are in `WEB-INF/lib/`
   - Verify classpath includes all dependencies
   - Rebuild the project

3. **Port Already in Use**
   - Change Tomcat port in `server.xml`
   - Or stop the process using the port:
     ```bash
     # Find process
     lsof -i :8080
     # Kill process
     kill -9 <PID>
     ```

4. **404 Not Found**
   - Verify application is deployed correctly
   - Check Tomcat logs: `$CATALINA_HOME/logs/catalina.out`
   - Ensure servlet mappings are correct in `web.xml` (if present)

5. **Email Not Sending**
   - Verify email credentials in `PassResetEmail.java`
   - For Gmail, ensure App Password is used (not regular password)
   - Check firewall/network settings
   - Review email server logs

6. **CORS Errors**
   - Verify CORS headers in servlets match frontend URL exactly
   - Check that OPTIONS requests are handled
   - Ensure `Access-Control-Allow-Credentials` is set if using cookies
   - Check browser console for specific CORS error messages

7. **Frontend Can't Connect to Backend**
   - Verify `VITE_API_BASE_URL` in frontend `.env` file
   - Check that backend is running and accessible
   - Test backend endpoint directly: `curl http://localhost:8080/hikeapp/api/hello`
   - Check browser Network tab for failed requests
   - Verify CORS configuration

8. **Frontend Build Errors**
   - Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
   - Check Node.js version: `node --version` (should be 18+)
   - Verify all dependencies are installed: `npm install`
   - Check for TypeScript errors: `npm run type-check` (if available)

### Logs Location

- **Tomcat Logs**: `$CATALINA_HOME/logs/`
  - `catalina.out` - Main application log
  - `localhost.YYYY-MM-DD.log` - Application-specific logs
- **Application Logs**: Check console output or configured log files

### Getting Help

1. Check Tomcat logs for detailed error messages
2. Verify all prerequisites are installed correctly
3. Ensure database schema is created properly
4. Review configuration files for typos or incorrect values

---

## Additional Notes

- The application uses Jakarta Servlet API (not javax.servlet)
- Ensure your Tomcat version supports Jakarta (Tomcat 10+)
- Image uploads are stored in `webapp/images/hikes/{hikeId}/`
- JWT tokens are used for authentication - ensure proper secret key configuration
- The application structure follows standard Java web application conventions

---

## Quick Start Checklist

### Backend Setup
- [ ] Install JDK 11+
- [ ] Install MySQL 8.0+
- [ ] Install Apache Tomcat 10+
- [ ] Clone backend repository
- [ ] Run `setup.sql` to create database
- [ ] Update database credentials in `DBConnector.java` and `DBConnect.java`
- [ ] (Optional) Configure email settings in `PassResetEmail.java`
- [ ] Build the backend application
- [ ] Deploy to Tomcat
- [ ] Start Tomcat server
- [ ] Verify backend API: `curl http://localhost:8080/hikeapp/api/hello`
- [ ] Check database connection in logs

### Frontend Setup
- [ ] Install Node.js 18+
- [ ] Clone frontend repository
- [ ] Install dependencies: `npm install`
- [ ] Create `.env` file with `VITE_API_BASE_URL=http://localhost:8080/hikeapp`
- [ ] Start development server: `npm run dev`
- [ ] Access frontend: `http://localhost:5173`
- [ ] Verify frontend connects to backend (check browser console)

### Full Stack Verification
- [ ] Backend running on port 8080
- [ ] Frontend running on port 5173
- [ ] Frontend can make API calls to backend
- [ ] CORS configured correctly
- [ ] Authentication flow works (login/signup)
- [ ] Database operations work (create hike, add review, etc.)

---

For questions or issues, refer to the project's issue tracker or contact the development team.

