FROM amazoncorretto:21-alpine as build
WORKDIR /app

COPY build/libs/weather-api-0.0.1-SNAPSHOT.jar /app/app.jar
RUN jar xf /app/app.jar

RUN jdeps \
  --print-module-deps \
  --ignore-missing-deps \
  --recursive \
  --multi-release 21 \
  --class-path="BOOT-INF/lib/*" \
  app.jar > jre-deps.info

RUN apk add --no-cache binutils && jlink --verbose \
--compress 2 \
--strip-debug \
--no-header-files \
--no-man-pages \
--output jre \
--add-modules $(cat jre-deps.info)

FROM alpine:latest
ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY --from=build /app/jre $JAVA_HOME
COPY --from=build /app/app.jar /app/app.jar

WORKDIR /app

CMD ["java", "-jar", "app.jar"]