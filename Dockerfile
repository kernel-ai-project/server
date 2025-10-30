# syntax=docker/dockerfile:1.7

# 1) 빌드
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# wrapper/설정만 먼저 복사해서 의존성 캐시
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Gradle 캐시 재사용 → 다음 빌드부터 의존성 다운로드 0에 가깝게
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies || true

# 실제 소스는 마지막
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# 2) 런타임
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
