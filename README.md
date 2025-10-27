# Automated Traffic System

A Spring Boot-based application for managing and analyzing traffic data with RESTful API endpoints for data retrieval, analysis, and reporting.

## Features

- **Traffic Data Management**
  - Upload traffic data files
  - Retrieve paginated traffic data
  - Filter and sort traffic data
  - Versioned API endpoints (v1, v2)

- **Data Analysis**
  - Traffic statistics (total, average, min, max)
  - Paginated data retrieval
  - Custom sorting and filtering

- **API Versioning**
  - v1: Basic traffic data operations
  - v2: Enhanced features and statistics

## Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7.0+
- Spring Boot 3.0.0+
- Lombok
- JUnit 5 for testing

## Getting Started

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/automated-traffic-system.git
   cd automated-traffic-system
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

   The application will be available at `http://localhost:8080`

## API Documentation

### Base URLs
```
http://localhost:8080/api/v1  # For v1 API
http://localhost:8080/api/v2  # For v2 API
http://localhost:8080/api/ai  # For AI endpoints
http://localhost:8080/api/reports  # For report generation
```

### Traffic Data Controller (v1 & v2)

#### Data Upload
- **POST** `/v{version}/traffic/upload`  
  Upload traffic data from a file  
  `Content-Type: multipart/form-data`

#### Data Retrieval
- **GET** `/v{version}/traffic`  
  Get all traffic data with pagination  
  Query Params: `page`, `size`, `sort`

- **GET** `/v{version}/traffic/total`  
  Get total number of cars in the system

- **GET** `/v{version}/traffic/daily`  
  Get daily car counts (date -> count mapping)

- **GET** `/v{version}/traffic/top-three`  
  Get top 3 half-hour periods with most cars

- **GET** `/v{version}/traffic/least-cars-period`  
  Find 1.5-hour period with least cars

- **POST** `/v{version}/traffic`  
  Add new traffic data record

#### Statistics (v2 only)
- **GET** `/v2/traffic/stats`  
  Get comprehensive traffic statistics

### Traffic AI Controller

- **GET** `/ai/traffic/analyze`  
  Analyze traffic patterns for a date range  
  Query Params: `startDate`, `endDate`

- **GET** `/ai/traffic/predict`  
  Predict traffic for future time period

### Reports Controller

- **GET** `/reports`  
  Get traffic report (supports content negotiation)  
  `Accept: text/plain` for text report  
  `Accept: application/json` for JSON report

### Sample API Requests

#### Traffic Data Endpoints

1. **Upload Traffic Data**
```bash
curl -X POST "http://localhost:8080/api/v1/traffic/upload" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@traffic_data.txt"
```

2. **Get All Traffic Data (Paginated)**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic?page=0&size=10&sort=timestamp,desc" \
     -H "Accept: application/json"
```

3. **Get Daily Traffic Summary**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic/daily" \
     -H "Accept: application/json"
```

4. **Get Top 3 Busiest Periods**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic/top-three" \
     -H "Accept: application/json"
```

5. **Find Least Busy 1.5 Hour Period**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic/least-cars-period" \
     -H "Accept: application/json"
```

6. **Get Traffic Statistics (v2)**
```bash
curl -X GET "http://localhost:8080/api/v2/traffic/stats" \
     -H "Accept: application/json"
```

#### AI Analysis Endpoints

7. **Analyze Traffic Patterns**
```bash
curl -X GET "http://localhost:8080/api/ai/traffic/analyze?startDate=2023-01-01&endDate=2023-01-31" \
     -H "Accept: application/json"
```

8. **Predict Future Traffic**
```bash
curl -X GET "http://localhost:8080/api/ai/traffic/predict?hours=24" \
     -H "Accept: application/json"
```

#### Report Generation

9. **Get Text Report**
```bash
curl -X GET "http://localhost:8080/api/reports" \
     -H "Accept: text/plain"
```

10. **Get JSON Report**
```bash
curl -X GET "http://localhost:8080/api/reports" \
     -H "Accept: application/json"
```

#### Advanced Queries

11. **Filter by Date Range**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic?start=2023-01-01T00:00:00&end=2023-01-31T23:59:59" \
     -H "Accept: application/json"
```

12. **Get Traffic Data with Custom Fields**
```bash
curl -X GET "http://localhost:8080/api/v1/traffic?fields=timestamp,carCount,location&sort=carCount,desc" \
     -H "Accept: application/json"
```

### Endpoints

#### Traffic Data (v1)

- **GET** `/traffic` - Get paginated traffic data
  - Query Parameters:
    - `page` - Page number (default: 0)
    - `size` - Number of items per page (default: 10)
    - `sort` - Sort field and direction (e.g., `timestamp,desc`)

- **POST** `/traffic/upload` - Upload traffic data file
  - Content-Type: `multipart/form-data`
  - Parameter: `file` - Text file containing traffic data

#### Traffic Statistics (v2)

- **GET** `/traffic/stats` - Get traffic statistics
  - Returns: Total cars, average, min, and max values

## Data Format

### Traffic Data File Format
```
TIMESTAMP CAR_COUNT
2023-01-01T12:00:00 10
2023-01-01T12:15:00 15
```

### API Response Examples

#### Get Traffic Data (v1)
```json
{
  "content": [
    {
      "id": 1,
      "timestamp": "2023-01-01T12:00:00",
      "carCount": 10,
      "location": "Main Street"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

#### Get Traffic Statistics (v2)
```json
{
  "totalCars": 100,
  "averageCars": 10.5,
  "maxCars": 30,
  "minCars": 5
}
```

## Testing

Run tests using:
```bash
./gradlew test
```

Test coverage includes:
- Controller unit tests
- Service layer tests
- Exception handling
- Pagination and sorting
- File upload validation

## Pagination

The API supports pagination with the following defaults:
- Page number: 0 (first page)
- Page size: 10 items per page
- Sort: By ID in ascending order

## Error Handling

The API returns appropriate HTTP status codes and error messages:
- `400 Bad Request` - Invalid input parameters
- `404 Not Found` - Resource not found
- `415 Unsupported Media Type` - Invalid file type
- `500 Internal Server Error` - Server-side error

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

Your Name - [@yourtwitter](https://twitter.com/yourtwitter) - email@example.com

Project Link: [https://github.com/yourusername/automated-traffic-system](https://github.com/yourusername/automated-traffic-system)

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [JUnit 5](https://junit.org/junit5/)
- [Lombok](https://projectlombok.org/)
- [Mockito](https://site.mockito.org/)
