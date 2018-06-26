FROM java:8-alpine
RUN mkdir -p /app /app/resources
WORKDIR /app
COPY target/planeta-crt.jar .
COPY resources/public resources/public
CMD java -jar planeta-crt.jar
EXPOSE 3333