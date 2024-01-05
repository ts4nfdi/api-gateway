FROM tomcat:8.5-alpine
RUN rm -rf /usr/local/tomcat/webapps/*
COPY ./target/nfdi-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/nfdi.war
CMD ["catalina.sh","run"]

