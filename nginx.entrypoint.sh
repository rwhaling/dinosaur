#!/bin/sh
rm /tmp/app.socket
rm /tmp/app.fifo
mkfifo /tmp/app.fifo
nginx -g "daemon off;" &
export ROUTER_MODE=FCGI
# nc -l -U /tmp/app.socket < /tmp/app.fifo | /var/www/localhost/cgi-bin/dinosaur-build-out > /tmp/app.fifo
socat UNIX-LISTEN:/tmp/app.socket,fork,max-children=48,backlog=4096 EXEC:/var/www/localhost/cgi-bin/dinosaur-build-out
