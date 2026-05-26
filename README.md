# Millions — IDATT2003

**Students:** Edvard Kvisler Ladim (157325), Oskar Hellum Reppen (157300)

**Repository:** https://github.com/edladim/millions

---

## Project description

Millions is a stock trading simulation game built in Java with JavaFX. The player starts with a chosen capital and a stock exchange loaded from a CSV file. Each week, stock prices fluctuate and the player can buy and sell shares to grow their portfolio. The game tracks net worth, profit, and awards the player a status (Novice → Investor → Speculator) based on performance.

---

## Requirements

- Java 21 or newer
- Maven 3.8 or newer

---

## Build and run

### Run the application
```bash
mvn javafx:run
```

### Run all tests
```bash
mvn test
```

### Compile only
```bash
mvn compile
```

### Build and run a packaged JAR with dependencies
```bash
mvn clean package
```

After building the project, run:

```bash
java -jar target/millions-1.1.0-jar-with-dependencies.jar
```
---

## Project structure

The project follows MVC with an Observer pattern and uses the standard Maven directory layout.

```
src/
├── main/
│   ├── java/edu/ntnu/idi/idatt/millions/
│   │   ├── controller/       # Page controllers (setup, trading, portfolio)
│   │   ├── filehandler/      # CSV reading and writing
│   │   ├── model/            # Domain entities (Exchange, Player, Portfolio, Stock, Share)
│   │   │   └── transaction/  # Transaction types, calculators and factory
│   │   ├── observer/         # Observer interfaces (Exchange, Player, Portfolio)
│   │   └── view/             # JavaFX views and formatter
│   │       ├── components/   # Reusable UI components (Sidebar, Chart)
│   │       └── pages/        # Full-page views (Dashboard, Trading, Portfolio)
│   └── resources/
│       ├── StockData.csv     # Default stock data loaded on startup
│       └── styles/           # CSS stylesheets
└── test/
    └── java/edu/ntnu/idi/idatt/millions/
        ├── model/            # Unit tests for domain classes
        │   └── transaction/  # Unit tests for transaction logic
        └── filehandler/      # Unit tests for CSV parsing
```

---

## Test coverage

After running `mvn test`, a JaCoCo coverage report is generated at:

```
target/site/jacoco/index.html
```
