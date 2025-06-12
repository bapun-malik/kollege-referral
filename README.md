# Multi-Level Referral & Earning System

A robust and scalable referral system built with Spring Boot that enables multi-level referral tracking and earning management.

## 🚀 Features

- Multi-level referral tracking
- Real-time notifications using WebSocket
- RESTful API endpoints
- Interactive web interface
- Secure user authentication
- Referral analytics and reporting
- Earning management system

## 🛠️ Technology Stack

- **Backend Framework:** Spring Boot 3.2.3
- **Java Version:** JDK 17
- **Database:** MySQL
- **ORM:** Spring Data JPA
- **Frontend:**
  - Thymeleaf (Template Engine)
  - Bootstrap 5.3.3
  - SockJS (WebSocket)
  - STOMP WebSocket
- **API Documentation:** OpenAPI/Swagger
- **Build Tool:** Maven

## 📋 Prerequisites

- JDK 17 or later
- Maven 3.6+
- MySQL 8.0+
- Node.js (for WebJars)

## 🔧 Installation & Setup

1. **Clone the repository**
   ```bash
   git clone git@github.com:bapun-malik/kollege-referral.git
   cd kollege-referral
   ```

2. **Configure MySQL Database**
   - Create a new MySQL database
   - Update `src/main/resources/application.properties` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will be available at `http://localhost:8080`

## Use The Fronted
- go to localhost:8080 to access the home page
- create User,look up a user etc. features

## 🏗️ Project Structure

```
src/main/java/com/kollege/referral/
├── config/          # Configuration classes
├── controller/      # REST controllers and web controllers
├── dto/            # Data Transfer Objects
├── model/          # Entity classes
├── repository/     # JPA repositories
├── service/        # Business logic implementation
└── ReferralSystemApplication.java  # Main application class
```

## 📚 API Documentation

The API documentation is available through Swagger UI:
- Access Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🔌 WebSocket Endpoints

- **Connection URL:** `ws://localhost:8080/ws`
- **STOMP Endpoint:** `/app`
- **Topic Subscriptions:**
  - `/topic/notifications` - For real-time notifications
  - `/topic/referrals` - For referral updates

## 🔍 Monitoring

Spring Boot Actuator endpoints are available for monitoring:
- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`

## 🧪 Testing

Run the tests using:
```bash
mvn test
```

## 🚀 Deployment

1. **Build the JAR file:**
   ```bash
   mvn clean package
   ```

2. **Run the JAR file:**
   ```bash
   java -jar target/kollege-referral-0.0.1-SNAPSHOT.jar
   ```

## 📝 Configuration Options

Key application properties that can be configured:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# WebSocket Configuration
spring.websocket.max-text-message-size=8192

# Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Support

For support and queries, please create an issue in the repository or contact the development team.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- The open-source community for various libraries used in this project
