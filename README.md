# oereb-client-gwt

## todo
- Achtung: alles was keinen AbortController hat, muss mit dem Loader für den Benutzer disabled werden. Sonst wird nicht sauber geresettet.


## Develop

### Run 
First Terminal:
```
./mvnw spring-boot:run -Penv-dev -pl *-server -am -Dspring-boot.run.profiles=dev
```

Second Terminal:
```
./mvnw gwt:codeserver -pl *-client -am
```

Or without downloading all the snapshots again:
```
./mvnw gwt:codeserver -pl *-client -am -nsu 
```

Test single class in subproject:
```
./mvnw test -Dtest=XXXXXXTest -pl oereb-client-server
```

