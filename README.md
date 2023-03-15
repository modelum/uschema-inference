# U-Schema Inference Processes

This repository contains the projects involved in extracting the schema of a running database and create a corresponding U-Schema model from it. If the database holds an explicit schema definition then this schema is traversed to create the model. On the other hand, if the schema is only implicit in the data then the process traverses such data, inferring the schema by processing each entry stored. Currently, the following databases are supported: MySQL, MongoDB, Cassandra, Neo4j, and Redis.

The inference processes depend on the U-Schema core projects that are found in the corresponding [U-Schema repository](https://github.com/modelum/uschema).

```bash
$ git clone https://github.com/modelum/uschema
$ git clone https://github.com/modelum/uschema-inference
```

The repository contains the following projects:

* `es.um.uschema.cassandra2uschema`: Project containing the inference process for Cassandra.
* `es.um.uschema.mongodb2uschema`, `es.um.uschema.mongodb2uschema.spark`, `es.um.uschema.doc2uschema` and `es.um.uschema.doc2uschema.validation`: Projects containing the inference process for MongoDB and a validation process to assure the inference correctness.
* `es.um.uschema.hbase2uschema` and `es.um.uschema.hbase2uschema.validation`: Projects containing the inference process for HBase and a validation process to assure the inference correctness.
* `es.um.uschema.neo4j2uschema` and `es.um.uschema.neo4j2uschema.validation`: Projects containing the inference process for Neo4j and a validation process to assure the inference correctness.
* `es.um.uschema.redis2uschema` and `es.um.uschema.redis2uschema.validation`: Projects containing the inference process for Redis and a validation process to assure the inference correctness.
* `es.um.uschema.sql2uschema`: Project containing the inference process for MySQL.