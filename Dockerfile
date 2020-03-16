FROM adoptopenjdk:13-jre-hotspot
RUN mkdir /Ramsey
COPY target/ramsey-worker.jar /Ramsey/
COPY target/version.txt /Ramsey/
CMD java -jar -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:MaxRAMPercentage=90 -XshowSettings:vm -Dspring.profiles.active=${SPRING_PROFILE} /Ramsey/ramsey-worker.jar
