# Game Shop Application

A JavaFX-based game shop application that allows users to manage their game library, integrate with Steam, and interact with other users.

## Features

### User Features
- **Account Management**
  - User registration and login
  - Balance management
  - Steam ID integration
  - Profile customization

- **Game Library**
  - View purchased games
  - Import and view Steam library games
  - Sort games by various criteria
  - Search functionality
  - Game details view

- **Social Features**
  - Add and manage friends
  - View friends' game libraries
  - See common games with friends

- **Store**
  - Browse available games
  - Purchase games
  - Search and filter games
  - View game details

### Admin Features
- **User Management**
  - View all users
  - Delete users
  - Manage user roles

- **Game Management**
  - Add new games
  - Edit existing games
  - Delete games
  - Manage game keys

## Technical Requirements

- Java JDK 17 or higher
- JavaFX 17.0.2
- MySQL Database
- Maven for dependency management

## Database Setup

1. Create a MySQL database named `gameshop`
2. Run the following SQL scripts:
```sql
-- Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    balance DECIMAL(10,2) DEFAULT 0.0,
    steam_id VARCHAR(50)
);

-- Games table
CREATE TABLE games (
    game_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    app_id INT,
    developer VARCHAR(100),
    publisher VARCHAR(100),
    genre VARCHAR(50)
);

-- Game keys table
CREATE TABLE game_keys (
    key_id INT PRIMARY KEY AUTO_INCREMENT,
    game_id INT NOT NULL,
    key_value VARCHAR(100) NOT NULL,
    is_sold BOOLEAN DEFAULT false,
    FOREIGN KEY (game_id) REFERENCES games(game_id)
);

-- User games table
CREATE TABLE user_games (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    game_id INT NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (game_id) REFERENCES games(game_id)
);

-- Friends table
CREATE TABLE friends (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (friend_id) REFERENCES users(user_id),
    UNIQUE KEY unique_friendship (user_id, friend_id)
);
```

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/gameshop.git
```

2. Navigate to the project directory:
```bash
cd gameshop
```

3. Build the project using Maven:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

## Configuration

1. Update the database connection settings in `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/gameshop
db.user=your_username
db.password=your_password
```

2. Configure Steam API key (if using Steam integration):
```properties
steam.api.key=your_steam_api_key
```

## Usage

### User Guide
1. Register a new account or login with existing credentials
2. Add funds to your account using the "Add Funds" button
3. Browse the store and purchase games
4. Connect your Steam account to import your Steam library
5. Use the search and sort features to organize your library
6. Add friends and view their game libraries

### Admin Guide
1. Login with admin credentials
2. Use the admin panel to manage users and games
3. Add new games to the store
4. Generate and manage game keys
5. Monitor user activities

## Contributing

1. Fork the repository
2. Create a new branch for your feature
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX framework
- Steam Web API
- MySQL database
- All contributors and testers
