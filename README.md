# geode-kakfa-connector

This project is a BETA Spring Boot application that moves JSON based data to and from
 [Apache Geode](https://geode.apache.org/) and [Apache Kafka](https://kafka.apache.org/).
 
 
This application also using [Spring Batch](https://spring.io/projects/spring-batch), [Spring Data Geode](https://spring.io/projects/spring-data-geode), [Spring Kafka](https://spring.io/projects/spring-kafka), 
 [Spring Security](https://spring.io/projects/spring-security).


# Spring Batch Database


An in-memory H2 database is configured by default.

username:sa 
password:<empty>
 
You can change those parameters by adding the 
following spring properties
 

    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=password
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

For H2

You can change also uses a file-based storage by changing the URL
spring.datasource.url:

    spring.datasource.url=jdbc:h2:file:/data/demo

# Testing

## Start/Setup Kafka

- [Download Apache Kafka](https://kafka.apache.org/downloads)
- Clone and Open source scripts to start kakfa named https://github.com/ggreen/kafka-devOps
- git clone https://github.com/ggreen/kafka-devOps.git
- cd kafka-devOps
- create file ./setenv.sh (modified the following exported as need)

    export KAFKA_HOST=localhost
    export KAFKA_HOME=<install-direct> ex: /devtools/integration/messaging/apacheKafka/kafka_2.11-2.3.0/
    export CONFIG_DIR=./config
    export KAFKA_BROKER_PORT=9092
    export ZOOKEEPER_PORT=2181
    
- cp -r $KAFKA_HOME/config .
- cp $KAFKA_HOME/config/zookeeper.properties ./zookeeper.properties
- ./startZookeeper.sh
- ./startKafka.sh
- ./createTopic.sh <topic> <partitions> <replication-factor> ex: ./createTopic.sh test 3 1


## Start/Setup Apache Geode


- Download binary for [Apache Geode](https://geode.apache.org/releases/)
- cd <APACHE-GEODE-DIR>/bin
- ./gfsh
- gfsh>start locator --name=locator
- gfsh>start server --name=server1 --locators=localhost[10334] --server-port=50001
- gfsh>create region --name=test --type=PARTITION_REDUNDANT
- gfsh>create region --name=test2 --type=PARTITION_REDUNDANT

## Load Apache Geode

- Use the following to create PDX data into Apache Geode
- https://github.com/Pivotal-Data-Engineering/dataTx-geode-rest-kotlin-app
- Goto http://localhost:9090/swagger-ui.html

![puts](docs/images/geode-rest-pdx-put.png)

# User Guide

This application exposes Spring Batch 
jobs that can be started through Swagger.


## From Geode to Kafka

Sample screen shot to start job to move from Apache Geode
to Apache Kakfa.

![from Geode to Kakfa](docs/images/job-geode-kafka.png)


You can use the kafka-devOps showMessage.sh script to view the data


    kafka-devOps$ ./showMessages.sh test
    {"keyClassName":"java.lang.String","keyString":"1","valueJson":"{\n  \"@type\" : \"solutions.imani.User\",\n  \"email\" : \"2@e.com\",\n  \"firstName\" : \"2\"\n}"}
 

## From Kafka to Geode

Sample screen shot to start job to move from Apache Kafka
to Apache Geode.

![from Kafka to Geode](docs/images/job-kafka-to-geode.png)