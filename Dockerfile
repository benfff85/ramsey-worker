FROM openjdk:21
RUN mkdir /Ramsey
COPY target/ramsey-worker.jar /Ramsey/
COPY target/version.txt /Ramsey/
CMD java -jar -XX:+UnlockExperimentalVMOptions -XX:MaxRAMPercentage=90 -XX:+CrashOnOutOfMemoryError -XshowSettings -Dspring.profiles.active=${SPRING_PROFILE} /Ramsey/ramsey-worker.jar
