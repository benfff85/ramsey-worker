FROM adoptopenjdk:13-jre-hotspot
RUN mkdir /Ramsey
COPY target/ramsey-worker.jar /Ramsey/
COPY target/version.txt /Ramsey/
CMD java -jar /Ramsey/ramsey-worker.jar --spring.profiles.active=${SPRING_PROFILE}
