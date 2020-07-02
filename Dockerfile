FROM openjdk:14-alpine
COPY build/libs/reactivemicronaut-*-all.jar reactivemicronaut.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Dmicronaut.environments=prod", "-Xmx128m", "-jar", "reactivemicronaut.jar"]