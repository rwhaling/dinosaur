# Start from a clean Alpine image
FROM alpine:3.3
RUN apk --update add apache2 apache2-utils python3
ADD handler.py /var/www/localhost/cgi-bin/handler.py

COPY httpd.conf /etc/apache2/httpd.conf
COPY mpm.conf /etc/apache2/mpm.conf


RUN mkdir -p /run/apache2
ADD apache.entrypoint.sh /root/

ENTRYPOINT "/root/apache.entrypoint.sh"
