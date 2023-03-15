# Neo4j Queries

API para poder realizar consultas e inserciones a una base de datos Neo4j utilizando elementos de un modelo USchema.
Si se activa el flag de validación (método setValidation(true) de la clase QueriesNeo4j) se almacenan los resultados obtenidos. Estos resultados se utilizarán para generar un informe en el que se indica: (1) Queries ejecutadas, (2) Elementos obtenidos, (3) Elementos esperados según lo descrito en el modelo, (4) Propiedades de las relaciones, (5) Propiedade y relaciones de los nodos.

## Getting Started

Run As -> Maven Install

**Validation**
En la clase es.um.uschema.s13e.neo4j.queries.main.ValidationLauncher cambiar las constantes:
* *URL*: Dirección de la base de datos. (bolt://host:port)
* *USER*: Usuario de la base de datos.
* *PASSWORD*: Contraseña del usuario de la base de datos.
* *MODEL_URI*: Fichero XMI con el modelo USchema de entrada.
* *VALIDATION_RESULTS_URL*: Fichero que se generará con los resultados de la validación.

**Uso de la API**
1. Configurar la conexión con Neo4j utilizando la clase *es.um.uschema.s13e.neo4j.queries.configuration.Neo4jSparkConfiguration*
```
Neo4jSparkConfiguration configuration = 
    new Neo4jSparkConfiguration(APP_NAME)
        .local(4)
        .url(URL)
        .user(USER)
        .password(PASSWORD);
```
2. Crear instancia de *es.um.uschema.s13e.neo4j.queries.QueriesNeo4j* con el objeto *Neo4jSparkConfiguration* anterior. 
3. Utilizar los métodos de *QueriesNeo4j*
* getNodes(StructuralVariation) : List<Neo4jNode>
* getReferences(StructuralVariaton) : List<Neo4jReference>

### Dependencies

* Project *es.um.uschema* 
From [U-Schema](https://github.com/modelum/uschema) git repository

### Prerequisites

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors
* **Author1**
## License
## Acknowledgments
