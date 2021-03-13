FROM gradle:jdk11 as build-stage

WORKDIR /project

USER root
COPY ./ /project
RUN chown -R gradle /project
USER gradle
RUN gradle clean build

FROM openjdk as production-stage

WORKDIR /app
COPY --from=build-stage /project/build/libs /app

ENTRYPOINT ["java","-jar", "./mafia-0.0.1-SNAPSHOT.jar"]
