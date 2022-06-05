# eurovision
Vote statistic service for https://vk.com/vezdekod challenge


System microservices:
===================

**1. VoteAPI service** - REST API 4 vote adding ang statistics (total / interval). 

Swagger: http://localhost:8087/swagger-ui/index.html

Run Spring Boot app:
`$ java -jar ./gateway.jar`

artists.yml 4 Eurovision artists-members is needed in running folder

*api-key* header need be added for Rate limiting (configs are in applucation.yml)

**2. VoteStat console app**. Calculated statistics of stress test.


**3. GatewayAPI** - reactive cloud service (WebFlux, Gateway)

WebFlux swagger: http://localhost:8090/swagger-ui/index.html

Run Spring Cloud app:
`$ java -jar ./gateway.jar`

**4. Discovery service** - Eureka service registry. Service management: http://localhost:8012

Run Spring Cloud app:
`$ java -jar ./discovery.jar`
$ java -jar ./gateway.jar

Service communication schema with default ports:

![Безымянный-2022-01-30-2105](https://user-images.githubusercontent.com/23243577/172043896-faf0dfa0-73fc-44c4-97f2-10ccfc72825c.png)
