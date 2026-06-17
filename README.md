# Apache Click (Modernized)

This repository is a modernized fork of the original **Apache Click** framework, transitioned from its legacy Apache Ant build system to a clean, multi-module **Apache Maven** project targeting **Java 17**.

## 🚀 Evolution & Modernization
This fork represents a significant update to the project infrastructure:
*   **Java 17+ Support:** Updated source and target compatibility to JDK 17, resolving legacy `doclint` Javadoc errors and modernizing the bytecode.
*   **Maven Migration:** Replaced the monolithic `build.xml` with a standard Maven Reactor build.
*   **API Cleanup:** Migrated deprecated Jdk API calls.
*   **Unified Architecture:** Integrated the `mock` source and its corresponding tests directly into the `click` runtime module, eliminating circular dependencies.

## 📦 Project Structure
The project is organized into the following Maven modules:
*   **`click`** (`click` directory): The core application framework runtime.
*   **`click-extras`** (`extras` directory): The core lightweight controls extensions (free from heavy third-party dependencies).
*   **`click-extras-cayenne`** (`extras-cayenne` directory): Apache Cayenne ORM integration layer.
*   **`click-extras-hibernate`** (`extras-hibernate` directory): Hibernate ORM integration layer.
*   **`click-extras-spring`** (`extras-spring` directory): Spring Framework integration layer.
*   **`click-user-guide`** (`user-guide` directory): Framework documentation written in Asciidoc.
*   **`showcase/examples`**: (`showcase` directory):The showcase web application demonstrating all components in action.

## 🛠 Building the Project
Ensure you have **JDK 17** and **Maven 3.8+** installed.

```bash
# Build the entire project and install artifacts to your local .m2 repository
mvn clean install

# Run the Click Examples application
cd showcase/examples
mvn clean package jetty:run

```
## 📦 Testing Strategy
In this modernized version, the Mock utilities and their tests are fully integrated into the click runtime module. You no longer need to build a separate test module.

## 📦 How to Run Tests 
Maven automatically executes all unit tests (including the integrated Mock tests) during the standard build lifecycle:

```bash
# Run tests for the core module only
mvn test -pl click

# Run a specific Mock test
mvn test -Dtest=MockContainerTest -pl click
```
## 📦 Using Mock Utilities in External Projects
The Mock utilities (e.g., MockContainer, MockRequest) are packaged as a classifier. To use them in your own application's test suite:

```xml
<dependency>
    <groupId>org.apache.click</groupId>
    <artifactId>click</artifactId>
    <version>${click.version}</version>
    <classifier>mock</classifier>
    <scope>test</scope>
</dependency>
```

## Adding Components to Your Project

Now that framework dependencies are decoupled (CLK-47), add only the modules you actually need to your application's `pom.xml`:

### Core Extensions
```xml
<dependency>
    <groupId>org.apache.click</groupId>
    <artifactId>click-extras</artifactId>
    <version>${click.version}</version>
</dependency>
```

### Apache Cayenne Integration
```xml
<dependency>
    <groupId>org.apache.click</groupId>
    <artifactId>click-extras-cayenne</artifactId>
    <version>${click.version}</version>
</dependency>
```
### Hibernate Integration
```xml
<dependency>
    <groupId>org.apache.click</groupId>
    <artifactId>click-extras-hibernate</artifactId>
    <version>${click.version}</version>
</dependency>
```

### Spring Integration
```xml
<dependency>
    <groupId>org.apache.click</groupId>
    <artifactId>click-extras-spring</artifactId>
    <version>${click.version}</version>
</dependency>
```

---

## Documentation

The user guide is generated automatically during the build process using Asciidoctor. 
To generate the HTML and PDF documentation explicitly:

```bash
mvn generate-resources -pl user-guide
```

## Release Workflow (100% Local Deployment)
We use the `maven-release-plugin` configured with `<localCheckout>true</localCheckout>` to manage version transitions and tagging **locally before pushing to GitHub**.

### 1. Prepare the Release
This updates the `pom.xml` versions, runs validation checks, and creates the local Git tag:
```bash
mvn release:prepare -DskipTests -DallowTimestampedSnapshots=true
```
*Accept the default version suggestions (e.g. tag `click-2.7.0`, and next development `2.7.1-SNAPSHOT`).*

### 2. Perform the Release Build
This checks out the newly created tag internally and bundles all production artifacts into the central `target/checkout/` folder:
```bash
mvn release:perform -DskipTests
```

### 3. Push to GitHub Fork
Once the local build succeeds, push the development cycle commits and the new release tag to GitHub. **The tag push will trigger the automated GitHub Actions release workflow.**
```bash
git push origin main
git push origin --tags
```

---

## Troubleshooting & Resetting
If the release process fails halfway through or needs to be rolled back locally, run the following commands to return to a clean `SNAPSHOT` state:

```bash
# 1. Clean Maven release properties
mvn release:clean

# 2. Undo the 2 local version commits made by Maven
git reset --hard HEAD~2

# 3. Delete the local tag so it can be recreated from scratch
git tag -d click-2.7.0
```
