# eurovision
Vote statistic service for https://vk.com/vezdekod challenge

Deploy
===================
1. Java >= 11 needed
2. Build all subproject JAR-s with bootJar Gradle task

System microservices:
===================

**1. VoteAPI service** - REST API 4 vote adding ang statistics (total / interval). 

Swagger: http://localhost:8087/swagger-ui/index.html

Run Spring Boot app:
`$ java -jar ./gateway.jar`

- H2 embedded db persistence (every instance uses its own inmemory DB)
- artists.yml 4 Eurovision artists-members is needed in running folder
- *api-key* (e. g. user ID or token) header need be added for Rate limiting (configs are in application.properties: rate-limits.max-request-per-period=10
  rate-limits.period-ms=60000)
- In DEMO Jar-s there are 3 instances: 8086, 8087 and 8088 ports

**2. VoteStat Spring console app**

- Calculated different statistics parameters during stress test

Run console Spring app:
`$ java -jar ./stat-votes.jar vote-svc-perf -n 100 -c 10 vote-svc:8087`
where params:
- *100* is number of voters (with unique api-key);
- *10* is concurrency, number of threads
- *8087* is the port of VoteAPI service instance

**3. GatewayAPI** - reactive cloud service (WebFlux, Gateway)

- Aggregates GET-requests from multiply VoteAPI service instances
- It balances POST-requests during user voting according to crc16-method
- Communicate with other services with service registry

WebFlux swagger: http://localhost:8090/swagger-ui/index.html

Run Spring Cloud app:
`$ java -jar ./gateway.jar`

**4. Discovery service** - Eureka service registry.

- Must be started before all services
- Service management: http://localhost:8012

Run Spring Cloud app:
`$ java -jar ./discovery.jar`
$ java -jar ./gateway.jar


Service communication schema with default ports:
===================
![Безымянный-2022-01-30-2105](https://user-images.githubusercontent.com/23243577/172043896-faf0dfa0-73fc-44c4-97f2-10ccfc72825c.png)
