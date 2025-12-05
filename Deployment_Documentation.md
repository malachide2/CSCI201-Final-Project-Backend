---

# Hike Application Backend
## Deployment Documentation

---

**Version:** 1.0  
**Last Updated:** 2025  
**Document Type:** Technical Documentation

---

## CSCI 201 Final Project

### Backend Deployment Guide

This document provides comprehensive instructions for running and deploying the Hike Application backend on your local machine and in a production environment.

---

**Development Team:** CSCI 201 Final Project Team 20 
**Repository:** CSCI201-Final-Project-Backend  
**Application Type:** Java Servlet Web Application  
**Database:** MySQL 8.0+  
**Application Server:** Apache Tomcat 10+

---

## Document Information

- **Purpose:** Guide for setting up, running, and deploying the Hike Application backend
- **Target Audience:** Developers, System Administrators, DevOps Engineers
- **Prerequisites:** Basic knowledge of Java, MySQL, and web application deployment
- **Related Documents:** 
  - Endpoint Documentation
  - Rating System README

---

## Overview

The Hike Application backend is a Java-based web application built using Jakarta Servlets. It provides RESTful API endpoints for managing hikes, reviews, user authentication, and photo uploads. This guide covers everything needed to get the application running locally and deployed in a production environment.

---


## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Configuration](#database-configuration)
4. [Application Configuration](#application-configuration)
5. [Building the Application](#building-the-application)
6. [Running Locally](#running-locally)
7. [Production Deployment](#production-deployment)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before setting up the application, ensure you have the following installed:

### Required Software
- **Java Development Kit (JDK)**: Version 11 or higher
  - Verify installation: `java -version`
- **MySQL Server**: Version 8.0 or higher
  - Verify installation: `mysql --version`
- **Apache Tomcat**: Version 10.x or higher (for Jakarta Servlet API)
  - Download from: https://tomcat.apache.org/
- **Maven** (optional, if using Maven build): Version 3.6 or higher
  - Verify installation: `mvn -version`

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

## Application Configuration

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

## Building the Application

### Option 1: Using an IDE (Recommended for Development)

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

### Option 2: Manual Compilation

1. **Compile Java Files**
   ```bash
   # Navigate to project root
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

---

## Running Locally

### Using Apache Tomcat

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

3. **Access the Application**
   - Open your browser and navigate to: `http://localhost:8080/hikeapp/`
   - The default port is 8080 (configurable in `server.xml`)

### Using an IDE's Built-in Server

1. **Eclipse**
   - Right-click project → Run As → Run on Server
   - Select Tomcat server
   - Application will be deployed automatically

2. **IntelliJ IDEA**
   - Run → Edit Configurations
   - Add → Tomcat Server → Local
   - Configure deployment artifact
   - Run the configuration

### Verify Installation

1. **Test Database Connection**
   - Check Tomcat logs for: "Database connected successfully."
   - If you see connection errors, verify database credentials

2. **Test Endpoints**
   - Access: `http://localhost:8080/hikeapp/`
   - Test API endpoints using a tool like Postman or curl

---

## Production Deployment

### Option 1: Deploy to Apache Tomcat Server

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

- [ ] Install JDK 11+
- [ ] Install MySQL 8.0+
- [ ] Install Apache Tomcat 10+
- [ ] Run `setup.sql` to create database
- [ ] Update database credentials in `DBConnector.java` and `DBConnect.java`
- [ ] (Optional) Configure email settings in `PassResetEmail.java`
- [ ] Build the application
- [ ] Deploy to Tomcat
- [ ] Start Tomcat server
- [ ] Access `http://localhost:8080/hikeapp/`
- [ ] Verify database connection in logs

---

For questions or issues, refer to the project's issue tracker or contact the development team.

