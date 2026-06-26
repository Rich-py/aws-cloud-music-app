# 🎵 AWS Cloud Music Subscription Application

A cloud-native music subscription web application built and deployed on **Amazon Web Services (AWS)**. The application demonstrates three different cloud deployment architectures using **Amazon EC2**, **Amazon ECS (AWS Fargate)**, and **AWS Lambda with API Gateway**, while integrating **Amazon DynamoDB** and **Amazon S3** to provide a scalable and highly available solution.

This project showcases practical cloud engineering concepts including containerization, serverless computing, RESTful API development, NoSQL database integration, cloud storage, and AWS service orchestration.

---

## 📌 Project Overview

The application allows users to:

- Register and log into the system
- Browse the music catalogue
- Search songs by title, artist, album, or year
- Subscribe and unsubscribe to songs
- Store subscription information in Amazon DynamoDB
- Retrieve artist images securely from Amazon S3
- Interact with three independent backend architectures

The primary goal of this project was to compare different AWS compute services while maintaining the same application functionality.

---

# ☁️ AWS Architecture

The application uses the following AWS services:

| Service | Purpose |
|---------|----------|
| Amazon ECS (AWS Fargate) | Containerized backend deployment |
| Amazon EC2 | Virtual machine backend deployment |
| AWS Lambda | Serverless backend deployment |
| Amazon API Gateway | REST API for Lambda backend |
| Amazon DynamoDB | NoSQL database |
| Amazon S3 | Frontend hosting and artist image storage |
| Elastic Load Balancer (ALB) | Traffic routing for ECS backend |
| Amazon ECR | Docker image repository |
| IAM LabRole | Secure AWS resource access |

---

# 🏗️ Project Structure

```
aws-cloud-music-app
│
├── frontend
│   └── index-lambda.html
│
├── ecs-backend
│   ├── app.py
│   ├── Dockerfile
│   ├── requirements.txt
│   └── index-ecs.html
│
├── ec2-backend
│   ├── backend
│   └── EC2_RUN_STEPS.md
│
├── lambda-backend
│   └── lambda_function.py
│
└── database
    ├── pom.xml
    └── src/
```

---

# 🚀 Technologies Used

## Cloud Services

- Amazon ECS
- AWS Fargate
- Amazon EC2
- AWS Lambda
- Amazon API Gateway
- Amazon DynamoDB
- Amazon S3
- Amazon Elastic Container Registry (ECR)
- Application Load Balancer (ALB)
- IAM

## Backend

- Python
- Flask
- Flask-CORS
- boto3

## Containerization

- Docker

## Frontend

- HTML
- CSS
- JavaScript

## Database

- Amazon DynamoDB

---

# 🐳 Amazon ECS Deployment

The ECS implementation demonstrates a modern containerized deployment architecture.

Deployment workflow:

1. Flask backend application developed in Python.
2. Application containerized using Docker.
3. Docker image pushed to Amazon Elastic Container Registry (ECR).
4. ECS Task Definition created using the Docker image.
5. ECS Service deployed using AWS Fargate.
6. Application exposed through an Application Load Balancer (ALB).
7. Frontend configured to communicate with the ALB endpoint.
8. Backend performs CRUD operations on Amazon DynamoDB using boto3.

---

# 💻 Amazon EC2 Deployment

A second backend implementation was deployed on Amazon EC2.

The EC2 backend communicates with:

- Amazon DynamoDB
- Amazon S3
- Frontend application

Deployment instructions are included in:

```
ec2-backend/EC2_RUN_STEPS.md
```

---

# ⚡ AWS Lambda Deployment

The serverless implementation uses:

- AWS Lambda
- Amazon API Gateway
- Amazon DynamoDB

API Gateway routes HTTP requests to Lambda functions, which process application logic and interact with DynamoDB.

---

# 🗄️ Database Design

Amazon DynamoDB stores all application data.

The database consists of three tables:

- Login
- Music
- Subscriptions

The application implements both **Query** and **Scan** operations, with appropriate indexing strategies to support different access patterns.

---

# 🔒 Security

Security practices implemented include:

- IAM LabRole for AWS permissions
- Secure access to Amazon S3 resources
- RESTful API design
- CORS configuration
- Docker container isolation
- AWS managed infrastructure

---

# 📸 Application Features

- User Authentication
- Music Search
- Song Subscription
- Subscription Management
- Responsive Web Interface
- Cloud-Based Backend Services

---

# 👨‍💻 My Contribution

This project was developed as part of a team for the Cloud Computing course at **RMIT University**.

My primary responsibilities included:

- Designing and implementing the **Amazon ECS backend**
- Developing the Flask REST API
- Containerizing the application using Docker
- Writing the Dockerfile and dependency configuration
- Deploying the application on Amazon ECS using AWS Fargate
- Configuring the ECS Task Definition and ECS Service
- Setting up the Application Load Balancer (ALB)
- Integrating Amazon DynamoDB using boto3
- Testing and validating the ECS deployment

---

# 🎓 Academic Context

This repository showcases a cloud-native application developed for the **Cloud Computing** course at **RMIT University**.

The project demonstrates practical experience with modern cloud architectures, including virtual machines, containerized deployments, and serverless computing using AWS services.

---

# 🚀 Future Improvements

Potential enhancements include:

- CI/CD pipeline using GitHub Actions
- Infrastructure as Code (Terraform)
- Amazon CloudFront integration
- HTTPS using AWS Certificate Manager
- Amazon Cognito authentication
- CloudWatch monitoring and logging
- ECS Auto Scaling policies
- Infrastructure automation using AWS CloudFormation

---

# 📄 License

This repository is provided for educational and portfolio purposes.

© Richard Paul John Rathnaraj