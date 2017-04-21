FROM wgmouton/sbt-ubuntu:latest
RUN apt-get update
ENV APACHE_RUN_USER www-data \
    APACHE_RUN_GROUP www-data \
    APACHE_PID_FILE /var/run/apache2/apache2.pid \
    APACHE_RUN_DIR /var/run/apache2 \
    APACHE_LOCK_DIR /var/lock/apache2 \
    APACHE_LOG_DIR /var/log/apache2
RUN apt-get -y install ssl-cert apache2 apache2-utils && \
    cp /etc/apache2/mods-available/cgi.load /etc/apache2/mods-enabled/cgi.load

RUN apt-get install -y clang \
                       libgc-dev \
                       libunwind-dev \
                       libre2-dev
RUN mkdir /scala-native
WORKDIR /scala-native
ADD ./project/build.properties /scala-native/project/build.properties
ADD ./project/plugins.sbt /scala-native/project/plugins.sbt
ADD ./build.sbt /scala-native/build.sbt
RUN sbt clean
RUN mkdir /output
ADD . /scala-native
RUN sbt clean nativeLink && cp target/scala-2.11/scala-native-out /usr/lib/cgi-bin/dinosaur
CMD ["bash", "-c", "apachectl -e info -DFOREGROUND"]
