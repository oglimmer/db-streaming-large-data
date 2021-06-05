# Streaming large data sets

Start a mysql:

```
docker run --rm -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql
```

Start server:

```
./gradlew bootRun
```

Fill DB with data:

```
curl http://localhost:8080 -X POST -d '5500' -H 'Content-Type: application/json'
```

Query the data:

(at this point in time you might want to use -Xmx256M)

```
curl http://localhost:8080/plain-direct

# or

curl http://localhost:8080/plain-file

# or

curl http://localhost:8080/dsl
```
