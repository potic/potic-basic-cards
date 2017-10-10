FROM openjdk:8

RUN mkdir -p /usr/src/potic-basic-cards && mkdir -p /usr/app

COPY build/distributions/* /usr/src/potic-basic-cards/

RUN unzip /usr/src/potic-basic-cards/potic-basic-cards-*.zip -d /usr/app/ && ln -s /usr/app/potic-basic-cards-* /usr/app/potic-basic-cards

WORKDIR /usr/app/potic-basic-cards

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-basic-cards --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
