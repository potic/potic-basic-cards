FROM openjdk:8

RUN mkdir -p /usr/src/potic-cards && mkdir -p /usr/app

COPY build/distributions/* /usr/src/potic-cards/

RUN unzip /usr/src/potic-cards/potic-cards-*.zip -d /usr/app/ && ln -s /usr/app/potic-cards-* /usr/app/potic-cards

WORKDIR /usr/app/potic-cards

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-cards --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
