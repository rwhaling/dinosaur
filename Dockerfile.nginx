# Start from Alpine and Java 8, and name this stage "build"
FROM openjdk:8u121-jre-alpine AS build
# Install C libraries and build tools
RUN echo "installing dependencies" && \
    apk --update add gc-dev clang musl-dev libc-dev build-base git && \
    apk add libunwind-dev --update-cache --repository http://nl.alpinelinux.org/alpine/edge/main && \
    apk add libuv-dev
# Install re2 from source for clang++ compatability
RUN git clone https://github.com/google/re2.git && cd re2 && \
    CXX=clang++ make && make install

# Install SBT
ENV SBT_VERSION 0.13.15
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin
RUN echo "installing SBT $SBT_VERSION" && \
    apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar xz -C $SBT_HOME --strip-components=1 && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built && \
    sbt sbtVersion

# Set up the directory structure for our project
RUN mkdir -p /root/project-build/project
WORKDIR /root/project-build

# Resolve all our dependencies and plugins to speed up future compilations
ADD ./project/plugins.sbt project/
ADD ./project/build.properties project/
ADD build.sbt .
RUN sbt update

# Add and compile our actual application source code
ADD ./src src/
RUN sbt clean nativeLink

# Copy the binary executable to a consistent location
RUN cp ./target/scala-2.11/*-out ./dinosaur-build-out

# Start over from a clean Alpine image
FROM alpine:3.3

# Copy in C libraries
COPY --from=build \
   /usr/lib/libunwind.so.8 \
   /usr/lib/libunwind-x86_64.so.8 \
   /usr/lib/libgc.so.1 \
   /usr/lib/libstdc++.so.6 \
   /usr/lib/libgcc_s.so.1 \
   /usr/lib/libuv.so.1 \
   /usr/lib/
COPY --from=build \
   /usr/local/lib/libre2.so.0 \
   /usr/local/lib/libre2.so.0

# Copy in the executable
COPY --from=build \
   /root/project-build/dinosaur-build-out /var/www/localhost/cgi-bin/dinosaur-build-out

COPY httpd.conf /etc/apache2/httpd.conf
COPY mpm.conf /etc/apache2/mpm.conf

RUN apk --update add nginx socat bash
COPY nginx.conf /etc/nginx/nginx.conf

ADD nginx.entrypoint.sh /

ENTRYPOINT ["./nginx.entrypoint.sh"]
