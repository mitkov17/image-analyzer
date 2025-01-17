# Image Analyzer Application

The Image Analyzer Application is a full-stack application for uploading and analyzing images using AWS S3 and AWS Rekognition. The application includes a backend RESTful API and a frontend Angular application for interaction.

## Features
- Upload images to AWS S3.
- Analyze images using AWS Rekognition.
- Search images by keywords detected in their labels.
- View uploaded images in a gallery.

## Backend setup 

1. Configure the application:
   - Open the `runApplication.bat` file in the root directory.
   - Replace the placeholders with your actual AWS credentials and S3 bucket name:
     ```bat
     @echo off

     set BUCKET_NAME=<your-bucket-name>
     set ACCESS_KEY=<your-access-key>
     set SECRET_KEY=<your-secret-key>
     set AWS_REGION=<your-region>

     java -jar target/aws-image-analyzer-0.0.1-SNAPSHOT.jar
     ```

2. Open a terminal or command prompt and navigate to the project's root directory (where the pom.xml file is located). Then build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   runApplication.bat
   ```
- By default, the backend will run on http://localhost:8080.

## Frontend setup

1. Install Node.js:
   - Download and install Node.js version 16 or higher.

2. Install Angular CLI (if not already installed):
   ```bash
   npm install -g @angular/cli
   ```
   
3. Navigate to the frontend directory:
   ```bash
   cd aws-image-analyzer-frontend
   ```

4. Install dependencies:
   ```bash
   npm install
   ```

5. Run the frontend:
   ```bash
   ng serve
   ```
- The frontend application will be available at http://localhost:4200.
