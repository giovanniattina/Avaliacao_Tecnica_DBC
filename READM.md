# Avaliação

Avaliação técnica dev Giovanni Attina em Java

## Requerimentos

Para rodar o projeto precisa de"

- JDK 1.8
- Maven 3
- Docker




## Run

```bash
mvn clean package -DskipTests


# run databse mongodb
docker run -d -p 27017:27017 --name mongdb mongo:latest 

# run application 
java -jar target/app.jar

```
URI padrão está em localhost:8080


### Excluir banco local
para excluir o docker do mongo
```bash
docker ps
```
Pega o **container id** da imagem mongo e rode
```bash
docker kill [container_id]
docker rm [container_id]
``` 

## Documentation

Depois de rodar aplicação acessa
[Swagger](http://localhost:8080/swagger-ui/index.html) para consultar endpoints e como utiliza-los

