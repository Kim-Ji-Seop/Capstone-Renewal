#!/bin/sh

sleep 30 # initial delay

while :; do
    certbot renew
    sleep 12h & wait $${!}
    docker kill --signal=HUP nginx # signal nginx to reload its configuration
done