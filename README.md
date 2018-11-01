## Trips CRUD - microservice

### Run with SBT

Use `sbt run` command

### Run with Docker Compose

#### Requirements
- Docker daemon needs to be available for SBT packager plugin

#### Build Docker image

First you need to build an image. In order to do that just run SBT commands to build a service image:

```bash
sbt stage
sbt docker:publishLocal
```

Now you can run the environment. There are two shell scripts to start and stop docker-compose environment which 
includes 
Postgres 
database
 as well

start script:
```bash
sh start.sh
``` 

stop script:
```bash
sh remove.sh
```