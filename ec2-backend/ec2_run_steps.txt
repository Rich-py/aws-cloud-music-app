BACKEND EC2 RUN STEPS

1. Install Java and Maven:
   sudo yum update -y
   sudo yum install java-11-amazon-corretto maven -y

2. Upload this project folder to your backend EC2.

3. Make sure your backend EC2 uses LabRole / instance profile with:
   - DynamoDB access
   - S3 read access for your artist image bucket

4. Set environment variables:
   export AWS_REGION=us-east-1
   export FRONTEND_ORIGIN=http://YOUR_FRONTEND_EC2_PUBLIC_IP
   export S3_BUCKET_NAME=YOUR_ARTIST_IMAGES_BUCKET_NAME

5. Build:
   mvn clean package

6. Run:
   java -jar target/music-backend-ec2-1.0.0.jar

7. Backend API will run on:
   http://BACKEND_EC2_PUBLIC_IP:8080

8. Backend EC2 security group must allow inbound TCP 8080
   from your frontend EC2 public IP, or temporarily from 0.0.0.0/0 for testing.

