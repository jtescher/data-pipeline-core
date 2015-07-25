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