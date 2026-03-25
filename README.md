# Millions project IDATT2003

STUDENT NAMES: Edvard Kvisler Ladim, Oskar Hellum Reppen

STUDENT ID: 157325, 157300

---
## Project description

---
## Project structure
This project follows a layered architecture inspired by MVC.
All source code is stored under the standard Maven directory layout.
### Source code layout
```
src/
├── main/
│   └── java/
│       └── edu.ntnu.idi.idatt.millions/
│           ├── model/                   # Core domain entities and repository interfaces
│           └──  fileHandler/             # Application services and DTOs
│
└── test/
    └── java/
        └── edu.ntnu.idi.idatt.millions/  # All JUnit test classes
```
### Package Overview
- ***"model"***

  Contains all core business entities (Author, JournalEntry)
  and repository interfaces.

### JUnit test structure
All JUnit tests are stored in:
```
src/test/java/edu.ntnu.idi.idatt.millions/
```
and mirror the structure of the main code, for example:
```
src/test/java/edu.ntnu.idi.idatt.millions/model/PlayerTest.java
````

This ensures a clean 1-to-1 relationship between production code and test code.

---
## Link to repository

https://github.com/NTNU-IDI/mappe-2025-edladim/tree/main

---
## How to run the project

What is the input and output of the program? What is the expected behaviour of the program?)
- Java **21 or newer** (Java 25 recommended)
- Maven installed (optional if you only run the JAR)
---
### Running in an IDEA
(IntelliJ, VS Code, Eclipse, NetBeans, etc.)
1. Open or import the project as a **Maven project**
2. Wait for Maven to import dependencies
3. Navigate to: `src/main/java/edu/ntnu/idi/idatt/millions/Main.java`
4. Run the `main` method

The program will run in a terminal window inside your IDE.

---

### Running From Terminal
#### Build the project

```bash 

mvn clean package
```
This will generate a runnable fat JAR here:

`target/journal-1.0-SNAPSHOT.jar`

---

#### Run the JAR
```bash

java -jar target/journal-1.0-SNAPSHOT.jar

```
This is a **self-contained JAR**, meaning:
- It does not require any other dependencies to run
- Does not require maven to be installed
- Works on any operating system

---
## How to run the tests

```bash

mvn test
```
### Coverage Report (JaCoCo)
#### Go to:
```

target/site/jacoco/index.html
```
Open the report in your browser to see the code coverage.
