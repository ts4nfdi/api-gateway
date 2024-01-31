FROM openjdk:11-jre
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /logs
COPY ./target/nfdi-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/nfdi.war
EXPOSE 8080
CMD ["sh","-c", "java -jar /usr/local/tomcat/webapps/nfdi.war"]


