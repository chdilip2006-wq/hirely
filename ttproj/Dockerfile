FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /backend
COPY backend/pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /var/data/uploads
COPY --from=backend-build /backend/target/job-portal-1.0.0-exec.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
