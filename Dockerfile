FROM eclipse-temurin:17-jdk
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /logs
COPY ./build/libs/api-gateway-1.0-SNAPSHOT.jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["sh","-c", "java -jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar"]
