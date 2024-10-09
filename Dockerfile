# Build stage
FROM openjdk:17-jdk-slim as build
WORKDIR /app
COPY . .
RUN ./gradlew clean build

# Production stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Set the timezone environment variable
ENV TZ=Asia/Seoul

# Set the timezone inside the container
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

# Add JVM timezone setting
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
