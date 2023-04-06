# Avaliação

Avaliação técnica dev Giovanni Attina em Java para DBC

## Requerimentos

Para rodar o projeto precisa de"

- JDK 1.8 [Install](https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-ubuntu-20-04)
- Maven 3 [Install](https://www.digitalocean.com/community/tutorials/install-maven-linux-ubuntu)
- Docker [Install](https://docs.docker.com/engine/install/ubuntu/)




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
para excluir o docker criado para o mongo
1. Pegar ID do container
```bash
docker ps
```
2. Com o  **container id** da imagem mongo, rode os seguintes comandos para matar e excluir a image
```bash
docker kill [container_id]
docker rm [container_id]
``` 

### Mudar domonio
Execute o seguinte comondo para mudar a porta de execução
```bash
 export SERVER_PORT={NOVA_PORTA}
```
A aplicação busca na variável ambiente SERVER_PORT a porta de saída, caso não haja
utilizava a 8080 como padrão

## Documentation

Depois de rodar aplicação acessa
[Swagger](http://localhost:8080/swagger-ui/index.html) para consultar endpoints e como utiliza-los

# Tarefas

## Tarefa 2

## Terafa 3 - Versionamento 

Para versionamento da API vejo que uma das melhores formas é o versionamento e isolamento Controller.
No Controller é possível definir o path inicial da API, como exemplo 'api/v1/....'.
E também, isolando os modelos entradas dos PayLoad da API, para cada versão sendo único.

Desse modo, quando é preciso ter uma nova versão é possível criar um novo Controller para o projeto.
Dependendo da regra de negócio, criando novos Serviços com as regras para as novas versões ou aproveitando as antigas.

No projeto foi criado uma estrutura Simple de como seria o versionamento. (api/v1/{controllers}) 
