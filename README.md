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
./import.sh 1 # this creates 100 person

#or

./import.sh 5500 # this creates 550,000 persons (takes ~30 mins on my machine)
```

Query the data:

(at this point in time you might want to use -Xmx256M or less)

``` 
curl http://localhost:8080/plain-direct-poi # better do this from a browser 

# or

curl http://localhost:8080/plain-direct

# or

curl http://localhost:8080/plain-file

# or

curl http://localhost:8080/template

# or

curl http://localhost:8080/jpa

# or

curl http://localhost:8080/jpa-criteria

# or

curl http://localhost:8080/dsl
```
