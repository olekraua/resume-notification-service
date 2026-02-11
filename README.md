# resume-notification-service

Single-service repository for `microservices/backend/services/notification-service`.

## Build
`./mvnw -pl microservices/backend/services/notification-service -am -DskipTests package`

## Run
`./mvnw -pl microservices/backend/services/notification-service -am spring-boot:run`

## Shared libraries
This service depends on artifacts from `resume-platform-libs`.

Install/update shared libraries before building service repos:
`cd ../resume-platform-libs && ./mvnw -DskipTests install`
