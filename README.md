# Data Pipeline

This project shows an example of a data pipeline using Spark streaming, Kafka, and Cassandra.

The shown data flow is Order -> Kafka -> Spark -> Cassandra


## Prerequisites

## Install components

1. Install Kafka `brew install kafka`
1. Install Spark `brew install apache-spark`
1. Install Cassandra `brew install cassandra`

## Add cassandra schema

1. Start cassandra `cassandra -f`
1. Run the cassandra console `cqlsh`
1. Create a `KEYSPACE` and `Table`


```sql
CREATE KEYSPACE reports WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };
CREATE TABLE reports.line_items (
    id uuid,
    order_id uuid,
    amount text,
    currency_code text,
    PRIMARY KEY (order_id, id)
);
```

## Add kafka topic

1. Start the Kafka server `kafka-server-start.sh /usr/local/etc/kafka/server.properties`
1. Create a topic for the report model

```bash
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic order-topic
```
