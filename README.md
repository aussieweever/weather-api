# Restful Weather API

## Tech Stack

* Java 11
* SpringBoot
    * Spring Data JPA
    * Spring AOP
* H2 In-Memory DB
* Mockito
* MockMvc
* Flyway
* Lombok
* Swagger

## Quick Start

### Start the Weather API

#### Command Line

* Navigate to the project root under the terminal
* Run command to build the project:
  `./gradlew clean build`
* Run command to start the application:
  `java -jar ./build/libs/weather-api-0.0.1-SNAPSHOP.jar`

#### Intellij IDEA

* Or you can load the project into Intellij IDEA, and run from there

### Use the Weather API

* Under commandline, you can use curl to access the endpoint:

`curl -H "X-API-Key: 0391b035-d01a-4edc-ac35-44afbcb92231"  http://localhost:8080/api/weather?city=Melbourne&country=Australia`

* Use postman with parameters used above.
* Use Swagger UI from `http://localhost:8080/swagger-ui/index.html`

## Configuration

All the configurations are in the application.yml

### H2 Database

The Console of the H2 Database is enabled, you can access it through `http://localhost:8080/h2-console`. The login
information are set in the yaml file already.

By current setting, the schema and pre-configured data is provisioned on application starts up through flyway. All the 5
API keys are:

- 0391b035-d01a-4edc-ac35-44afbcb92231
- 399e82fd-64e6-45fe-8eed-23f5e80526ec
- b39260fb-b365-4ec5-a464-31a93d096502
- eecb96d0-92d0-484a-9e33-804939b09dcb
- 1e0bdb4c-0448-4c9e-b255-11ca9b70d8cc

### OpenWeatherMap

You can replace the api-key by your own

### Application Settings

- __weather-data-expires-minutes__: How long does the weather data expires
- __rate-limit-duration-minutes__: The duration in which the maximum number of requests allowed for each user
- __rate-limit-max-requests__: The maximum number of requests can be sent during the rate-limit-duration-minutes

## Design Introduction

* Use Spring AOP to check the API Key and Rate Limit.
* There are other 3rd party libraries can be used for the rate limit check, such as Bucket4j etc. However, store the
  data into database can simplify the implementation, and also, in case we changed the database to some other RDBMS,
  this solution can support checking the rate limit across multiple jvm instances.
* Take the assumption that we don't need to care of the cleanup of the requests in the database. A better solution is
  that we only need to store the latest 5 (or a configurable number) records for each user, but due to the time
  restriction, this is not implemented.

## Improvement Ideas

A better design to the RateLimit aspect is to make it not restricted to weather requests check only. A draft idea is to
provide a request type in the requests table, so the table can hold different type of requests, and the
RequestRepository can query request on the type.

In the RateLimit aspect, by providing an input parameter to specify what type of request this rate limit check is for,
we can easily use the RateLimit for different type of requests.
