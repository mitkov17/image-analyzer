@echo off

set BUCKET_NAME=<your-bucket-name>
set ACCESS_KEY=<your-access-key>
set SECRET_KEY=<your-secret-key>
set AWS_REGION=<your-region>

java -jar target/aws-image-analyzer-0.0.1-SNAPSHOT.jar
