FROM openjdk:8

RUN mkdir -p /usr/src/potic-cards && mkdir -p /opt

COPY build/distributions/* /usr/src/potic-cards/

RUN unzip /usr/src/potic-cards/potic-cards-*.zip -d /opt/ && ln -s /opt/potic-cards-* /opt/potic-cards

WORKDIR /opt/potic-cards

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-cards --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
