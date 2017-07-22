#!/bin/bash
mkfifo /tmp/app.fifo
nginx -g "daemon off;" &

nc -l -U /tmp/app.socket < /tmp/app.fifo | /var/www/localhost/cgi-bin/dinosaur-build-out > /tmp/app.fifo
