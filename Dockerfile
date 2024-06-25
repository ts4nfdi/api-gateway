FROM openjdk:11-jre
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /logs
COPY ./target/API-Gateway-0.0.1-SNAPSHOT.jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["sh","-c", "java -jar /usr/local/tomcat/webapps/API-Gateway-0.0.1-SNAPSHOT.jar"]


