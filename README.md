# Ramsey Worker
Worker component for the Ramsey Project

TODO Description of Targeted vs Comprehensive

## Image Build and Deploy

Build the image using SpringBoot defaults
```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=benferenchak/ramsey-worker:dev
````

Publish the image to Dockerhub
```bash
docker push benferenchak/ramsey-worker:dev
```

Start a container using the image
```bash
docker run --restart=always \
  --name=ramsey-worker \
  -e SPRING_PROFILES_ACTIVE=dev \
  --cpus=8 \
  benferenchak/ramsey-worker:dev
```