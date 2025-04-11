# Use an official Maven image to build the application
FROM maven:3.8.6-openjdk-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file and download the dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code into the container
COPY src ./src

# Package the application
RUN mvn clean package

# Use an official OpenJDK image to run the application
FROM openjdk:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/sistema-mensajeria-1.0.0.jar /app/sistema-mensajeria-1.0.0.jar

# Specify the command to run the application
CMD ["java", "-jar", "sistema-mensajeria-1.0.0.jar"]