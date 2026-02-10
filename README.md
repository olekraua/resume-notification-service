# notification-service

Standalone repository for the notification-service microservice.

## Local build

```bash
./mvnw -pl microservices/backend/services/notification-service -am -Dmaven.test.skip=true package
```

## Local run

```bash
./mvnw -pl microservices/backend/services/notification-service -am spring-boot:run
```

## Included modules

- shared
- notification
- microservices/backend/services/notification-service

