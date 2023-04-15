# Avaliação

Avaliação técnica dev Giovanni Attina em Java para DBC - Sicred

## Requerimentos

Para rodar o projeto precisa de:

- JDK 1.8 [Install](https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-ubuntu-20-04)
- Maven 3 [Install](https://www.digitalocean.com/community/tutorials/install-maven-linux-ubuntu)
- Docker [Install](https://docs.docker.com/engine/install/ubuntu/)




## Run

```bash
# build the service 
mvn clean package -DskipTests
# run databse mongodb and kafka server
docker-compose up -d
# run application 
java -jar target/app.jar

```
URI padrão está em localhost:8080


### Excluir dockers (mango e kafka)
para excluir os containers de docker criado
1. Pegar IDs dos containers
```bash
docker ps
```
2. Com os  **containers ids** de cada imagem, rode os seguintes comandos para matar e excluir a image
```bash
docker kill [container_id]
docker rm [container_id]
``` 

### Mudar domonio
Execute o seguinte comondo antes da rodar para mudar a porta de execução
```bash
 export SERVER_PORT={NOVA_PORTA}
```
A aplicação busca na variável ambiente SERVER_PORT a porta de saída, caso não haja
utilizava a 8080 como padrão

## Documentation

Depois de rodar aplicação acessa
[Swagger](http://localhost:8080/swagger-ui/index.html) para consultar endpoints e como utiliza-los

# Tarefas

## Tarefa 2 - Tópico
Na execução do serviço é utilizado um servidor do kafka em um container docker para publicar mensagem
async de quando uma sessão de puata fechar, com seu respectivo resultado

### Consultar mensagem publicadas
A fim de debug, executar os seguintes comandos para visualizar as sessões de votação fechadas 
através do consumo das mensagems publicadas no topíco 
````bash

docker exec -it cooperativismo-kafka-1 bash

./../../bin/kafka-console-consumer --bootstrap-server localhost:29092 --from-beginning --topic resultadoVotacaoPauta 'broker-list'
````


## Terafa 3 - Versionamento 

Para versionamento da API vejo que uma das melhores formas é o versionamento e isolamento Controller.
No Controller é possível definir o path inicial da API, como exemplo 'api/v1/....'.
E também, isolando os modelos entradas dos PayLoad da API, para cada versão sendo único.

Desse modo, quando é preciso ter uma nova versão é possível criar um novo Controller para o projeto.
Dependendo da regra de negócio, criando novos Serviços com as regras para as novas versões ou aproveitando as antigas.

No projeto foi criado uma estrutura Simple de como seria o versionamento. (api/v1/{controllers}) 
