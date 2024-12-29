FROM amazoncorretto:23-jdk

RUN yum update -y && \
    yum install -y procps && \
    yum clean all

ARG JAR_DATE=unknown
COPY target/aeron-service-1.0.0.jar /app/application.jar
