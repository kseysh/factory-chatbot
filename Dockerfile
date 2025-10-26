FROM amazoncorretto:21
WORKDIR /app
COPY .chat/build/libs/chat.jar /app/app.jar

ARG SPRING_PROFILES_ACTIVE=prod
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]
