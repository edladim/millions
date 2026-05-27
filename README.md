# Millions — IDATT2003

**Students:** Edvard Kvisler Ladim (157325), Oskar Hellum Reppen (157300)

**Repository:** https://github.com/edladim/millions

---

## Project description

Millions is a stock trading simulation game built in Java with JavaFX. The player starts with a chosen capital and a stock exchange loaded from a CSV file. Each week, stock prices fluctuate and the player can buy and sell shares to grow their portfolio. The game tracks net worth, profit, and awards the player a status based on performance. High scores are saved to a persistent leaderboard between sessions.

---

## Download & install

Pre-built native installers are available below, no Java installation required.

| OS | Download | How to install |
|----|----------|----------------|
| **macOS** | [Millions-1.1.1.dmg](https://github.com/edladim/millions/releases/download/v1.1.1/Millions-1.1.1.dmg) | Open the `.dmg`, drag **Millions.app** to Applications. First launch: right-click → Open to bypass Gatekeeper. |
| **Windows** | [Millions-1.1.1.msi](https://github.com/edladim/millions/releases/download/v1.1.1/Millions-1.1.1.msi) | Run the `.msi` installer and follow the wizard. |
| **Linux** | [millions_1.1.1_amd64.deb](https://github.com/edladim/millions/releases/download/v1.1.1/millions_1.1.1_amd64.deb) | `sudo dpkg -i millions_1.1.1_amd64.deb` |


---

## Requirements

- Java 25 or newer
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
java -jar target/millions-1.1.1-jar-with-dependencies.jar
```
---

## Project structure

The project follows MVC with an Observer pattern and uses the standard Maven directory layout.

```
src/
├── main/
│   ├── java/edu/ntnu/idi/idatt/millions/
│   │   ├── controller/            # Page controllers and transaction executor
│   │   ├── model/
│   │   │   ├── market/            # Exchange, Stock, price fluctuation
│   │   │   ├── player/            # Player, status, score, leaderboard entry
│   │   │   ├── portfolio/         # Portfolio, holdings, shares
│   │   │   └── transaction/       # Transaction types, calculators, factories and read-only views
│   │   ├── observer/              # Observer interfaces (Exchange, Player, Portfolio)
│   │   ├── persistence/
│   │   │   ├── leaderboard/       # Leaderboard reader/writer interfaces + CSV impl
│   │   │   └── stock/             # Stock reader/writer interfaces + CSV impl
│   │   └── view/
│   │       ├── components/
│   │       │   ├── portfolio/     # Holdings and transaction history panels
│   │       │   └── trading/       # Buy panel and stock list panel
│   │       ├── dialogs/           # End-game overlay and transaction dialog
│   │       ├── pages/             # Full-page views (Dashboard, Trading, Portfolio)
│   │       └── util/              # ViewFormatter, fonts, icons and style helpers
│   └── resources/
│       ├── data/                  # Default stock data CSV
│       ├── fonts/                 # Application fonts
│       ├── images/                # Application icons and images
│       └── styles/                # CSS stylesheets
└── test/
    └── java/edu/ntnu/idi/idatt/millions/
        ├── model/
        │   ├── market/            # Unit tests for exchange and stock logic
        │   ├── player/            # Unit tests for player and scoring
        │   ├── portfolio/         # Unit tests for portfolio and holdings
        │   └── transaction/       # Unit tests for transaction logic
        └── persistence/
            ├── leaderboard/       # Unit tests for CSV leaderboard I/O
            └── stock/             # Unit tests for CSV stock I/O
```

---

## Test coverage

After running `mvn test`, a JaCoCo coverage report is generated at:

```
target/site/jacoco/index.html
```
