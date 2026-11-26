# crs-fatca-registration

This is the CRS/FATCA backend service for registering new Individuals or organization to enable them to get a subscription Id

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and requires a Java 21 [JRE] to run.

### Running the service

**Service manager:** CRS_FATCA_ALL

**Individually via service manager:** CRS_FATCA_REGISTRATION

**Port:** 10031

Link: http://localhost:10031/crs-fatca-registration

### API

| Task                             | Supported methods | Description                                  |
|----------------------------------|-------------------|----------------------------------------------|
| /registration/individual/utr    | POST              | Individual registration using UTR              |
| /registration/individual/nino | POST              | Individual registration using NINO        |
| /registration/organisation/utr             | POST              | Organisation registration using UTR       |
| /registration/individual/noId              | POST               | Individual registration without Id |
| /registration/organisation/noId                | POST               | Organisation registration without Id       |
| /subscription/create-subscription                 | POST               | Creates a subscription       |
| /subscription/read-subscription                 | POST               | Reads a subscription       |
| /subscription/update-subscription                 | PUT               | Updates a subscription       |
| /registration/organisation/utr-only                | POST               | Organisation registration using UTR only used  for address reteival in FI manaagement       |
| /audit/create-registration                | POST               | Send an audit for create registration       |
| /audit/update-contact-details                | POST               | Send an audit for update contact details    |


---

## Running Tests
Run unit tests:
```
sbt test
```

Run integration tests:
```
sbt it/test
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").