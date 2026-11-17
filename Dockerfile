FROM eclipse-temurin:21-jre-alpine

# 작업 디렉터리와 앱을 실행할 비루트 계정을 준비합니다.
WORKDIR /app
RUN addgroup -S appgroup && adduser -S -G appgroup appuser

# Gradle bootJar가 만든 실행용 JAR를 앱 계정 소유로 복사합니다.
COPY --chown=appuser:appgroup build/libs/app.jar app.jar

# 앱 계정으로 실행하며 컨테이너 내부에서는 8080을 사용합니다.
USER appuser:appgroup
EXPOSE 8080

# JVM 메모리 부족 오류 시 종료해 운영 Compose의 재시작 정책이 동작하게 합니다.
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
