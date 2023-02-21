# U-Schema Inference Processes

This repository contains the projects involved in extracting the schema of a running database and create a corresponding U-Schema model from it. If the database holds an explicit schema definition then this schema is traversed to create the model. On the other hand, if the schema is only implicit in the data then the process traverses such data, inferring the schema by processing each entry stored. Currently, the following databases are supported: MySQL, MongoDB, Cassandra, Neo4j, and Redis.

The inference processes depend on the U-Schema core projects that are found in the corresponding [U-Schema repository](https://github.com/modelum/uschema).

The repository contains the following projects:

* `es.um.unosql.cassandra2unosql`: Project containing the inference process for Cassandra.
* `es.um.unosql.mongodb2unosql`, `es.um.unosql.mongodb2unosql.spark`, `es.um.unosql.doc2unosql` and `es.um.unosql.doc2unosql.validation`: Projects containing the inference process for MongoDB and a validation process to assure the inference correctness.
* `es.um.unosql.hbase2unosql` and `es.um.unosql.hbase2unosql.validation`: Projects containing the inference process for HBase and a validation process to assure the inference correctness.
* `es.um.unosql.neo4j2unosql` and `es.um.unosql.neo4j2unosql.validation`: Projects containing the inference process for Neo4j and a validation process to assure the inference correctness.
* `es.um.unosql.redis2unosql` and `es.um.unosql.redis2unosql.validation`: Projects containing the inference process for Redis and a validation process to assure the inference correctness.
* `es.um.unosql.sql2unosql `: Project containing the inference process for MySQL.