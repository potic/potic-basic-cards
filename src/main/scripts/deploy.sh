#!/usr/bin/env sh

##############################################################################
##
##  Stop and kill currently running docker image, pull newest version and
##  run it.
##
##############################################################################

warn ( ) {
    echo "$*"
}

warn "Currently running docker images"
docker ps -a

warn "Killing currently running docker image..."
docker kill potic-cards; docker rm potic-cards

warn "Pulling latest docker image..."
docker pull potic/potic-cards:$TAG_TO_DEPLOY

warn "Starting docker image..."
docker run -dit --name potic-cards --link potic-articles --link potic-users -e LOG_PATH=/logs -v /logs:/logs -p 40406:8080 potic/potic-cards:$TAG_TO_DEPLOY

warn "Currently running docker images"
docker ps -a
