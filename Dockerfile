FROM openjdk:11-jre
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /logs
COPY ./target/ndfi-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ndfi.war
RUN sh -c 'touch /usr/local/tomcat/webapps/RDB2RDF.war'
EXPOSE 8080
CMD ["catalina.sh","run"]
CMD ["sh","-c", "java -jar /usr/local/tomcat/webapps/RDB2RDF.war"]

