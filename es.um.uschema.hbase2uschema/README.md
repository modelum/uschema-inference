# HBase inference execution instructions
## Launch HBase
```
wget -q https://apache.brunneis.com/hbase/2.3.1/hbase-2.3.1-bin.tar.gz
tar xzf hbase-2.3.1-bin.tar.gz
hbase-2.3.1/bin/start-hbase.sh
```

## Initialize the database with contents

```
hbase-2.3.1/bin/hbase shell
hbase(main):XXX:X>
create 'people', 'user', 'address', 'watchedMovies', 'favoriteMovies'
put 'people', 156, 'user:name', 'Allison'
put 'people', 156, 'user:email', 'allison@gmail.com'
put 'people', 156, 'address:city', 'Aylesbury'
put 'people', 156, 'address:number', 8
put 'people', '156', 'address:street', 'Lott Meadow'
put 'people', '156', 'watchedMovies:202.title', 'The Matrix'
put 'people', '156', 'watchedMovies:202.year', 1999
put 'people', '156', 'watchedMovies:202.genre', 'Science Fiction'

put 'people', 178, 'user:name', 'Brian'
put 'people', 178, 'user:surname', 'Caldwell'
put 'people', 178, 'user:email', 'brian_caldwell@gmail.com'
put 'people', 178, 'address:city', 'Aylesbury'
put 'people', 178, 'address:number', 6
put 'people', '178', 'address:street', 'Fairfax Cres'
put 'people', '178', 'address:postcode', '30760'
put 'people', '178', 'favoriteMovies:202.title', 'The Matrix'
put 'people', '178', 'favoriteMovies:202.year', 1999
put 'people', '178', 'favoriteMovies:202.genre', 'Science Fiction'
```

## Prerequisites
* Java 8 installed.
* The `JAVA_HOME` environment variable pointing to the Java JDK folder. For example, before starting HBase:
    ```
    export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
    ```
