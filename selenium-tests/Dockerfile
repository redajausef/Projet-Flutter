FROM selenium/standalone-chrome:latest

USER root

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

USER seluser

# Run tests
CMD ["mvn", "compile", "exec:java", "-Dexec.mainClass=com.clinassist.selenium.ClinAssistSeleniumTests"]
