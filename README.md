# oereb-client-gwt

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

