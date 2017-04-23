FROM rwhaling/scala-native-alpine:0.1.0-sbt
RUN apk --update add uwsgi-cgi

RUN mkdir -p /dinosaur-build /output /output/usr/lib /usr/lib/cgi-bin
ADD . /dinosaur-build
WORKDIR /dinosaur-build

RUN sbt clean nativeLink && \
    mv /dinosaur-build/target/scala-2.11/*-out /usr/lib/cgi-bin/dinosaur-build-out

ADD cgi.ini /usr/lib/cgi-bin/cgi.ini
WORKDIR /usr/lib/cgi-bin
ENTRYPOINT ["sh", "-c"]
CMD ["uwsgi --ini cgi.ini"]
