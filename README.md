# oereb-client-gwt

Kantone, die nicht funktionieren, weil m.E. oder offensichtlich fehlerhaft:

- AI: WMS verlangt Authentifizierung.
- AR: WMS verlangt Authenfifizierung
- ~BS: GEOMETRY=true funktioniert nicht.~~ Ich mache für Extract einen geometry=false Request
- FR: GEOMETRY wird nicht zurückgeliefert
- LU: kein WMS
- NW: ReferenceWMS fehlt
- OW: ReferenceWMS fehlt
- SG: WMS verlangt Authentifizerung
- UR: Falsches Koordinatensystem
- VS: Kein WMS, sondern ESRI-Irgendwas.

Es funktionieren (teilweise Proxy):

- AG
- BE (sogar ohne Proxy)
- BL
- BS
- GL
- GR
- JU
- NE
- SH
- SO
- SZ
- TG
- TI
- ZU
- ZH

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

