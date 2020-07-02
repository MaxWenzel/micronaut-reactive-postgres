#!/bin/sh
docker build . -t reactivemicronaut
echo
echo
echo "To run the docker container execute:"
echo "    $ docker run --net=postgres_postgresnet -p 8082:8082 reactivemicronaut "
