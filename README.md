# GameShop - Steam-Integrated Game Store Application

A JavaFX-based desktop application that allows users to manage and purchase game keys while integrating with their Steam library.

## Features

### User Management
- User registration and authentication
- Role-based access control (Admin/Customer)
- Balance management system
- Steam library integration

### Game Management
- Browse available games
- Purchase game keys
- View game details and pricing
- Search functionality
- Sort games by various criteria

### Admin Features
- Manage users and their roles
- Add new games to the store
- Manage game keys
- Monitor transactions

### Steam Integration
- Import Steam library
- View playtime statistics
- Compare games with friends

## Technical Requirements

### Prerequisites
- Java JDK 17 or higher
- MySQL 8.0 or higher
- Maven
- Steam API Key (for Steam integration)

### Database Setup
1. Install MySQL
2. Create a new database named `gameshop`
3. Update database credentials in `src/main/java/com/example/gameshop/utils/DatabaseConnection.java`

java
private static final String URL = "jdbc:mysql://localhost:3306/gameshop";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/gameshop.git
```

2. Navigate to project directory
```bash:README.md
cd gameshop
```

3. Build the project
```bash
mvn clean install
```

4. Run the application
```bash
mvn javafx:run
```

## Project Structure

```
gameshop/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/gameshop/
│   │   │       ├── dao/
│   │   │       ├── models/
│   │   │       ├── scenes/
│   │   │       ├── services/
│   │   │       └── utils/
│   │   └── resources/
│   │       └── styles/
│   └── test/
│       └── java/
└── pom.xml
```

## Key Components

### Models
- `User`: User account management
- `Game`: Game information and pricing
- `GameKey`: Game key management
- `GameDetails`: Detailed game information

### Services
- `SteamAPI`: Steam platform integration
- `DatabaseManager`: Database operations
- `ThreadPool`: Asynchronous operations

### UI Scenes
- Login/Register scenes
- Store interface
- Account management
- Admin dashboard
- Friend search
- Game preview

## Testing

Run the test suite using Maven:
```bash
mvn test
```

The project includes unit tests for:
- Model constructors
- Business logic
- Data validation

## Styling

The application uses a custom dark theme defined in `dark-theme.css`. Key features include:
- Dark color scheme
- Custom button styles
- Responsive layouts
- Modern UI elements

## Security Features

- Password validation
- Input sanitization
- Role-based access control
- Database query validation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX framework
- Steam Web API
- MySQL Community
- Maven build tool
