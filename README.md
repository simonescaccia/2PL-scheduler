# 2PL-scheduler

2PL scheduler for the Data Management project

## Application description

This application aims to implement a Two-Phase Locking (2PL) scheduler for the Data Management project. It is built using Spring Boot and Maven for the backend, Thymeleaf for the front-end, and Docker for containerization.

## Requirements

- Check if the inserted schedule satisfies the assumptions (no two read/write on the same object for the same transaction, no rollbacks)
- If the schedule is valid return the list of transactions with the list of operations for each transaction.
- Insert a schedule, and check if it is in the 2PL class (with/without lock anticipation, with/without shared locks)
- If the schedule is in the 2PL class, then return the schedule with locks and unlocks, else return a description of why it is not in the 2PL class.
- Show conflict-serializability by precedence graph: Gen(2PL)=> DT(S) conflict serializable <=> P(S) is acyclic

## Logic

### Lock Table

Implementation

### 2PL without lock anticipation

- To check if a schedule is in the 2PL class without lock anticipation, we may block a transaction and resume it later.
- Wait-for-graph: if there is a cycle then there is a deadlock, so the schedule is not in the 2PL class. Adjacent list representation of the graph.

## Application setup

This application is built using Spring Boot, Maven and Thymeleaf for the web part.

- Spring Boot initializer: https://start.spring.io/
- Spring Boot tutorial:
  - https://spring.io/guides/gs/spring-boot/
  - https://spring.io/guides/gs/serving-web-content/
- Thymeleaf:
  - https://www.thymeleaf.org/faq.html 
  - It replaces JSP pages: https://www.thymeleaf.org/doc/articles/thvsjsp.html

### Docker image

You can use a docker container to run this application, using this tutorial:

- Docker guide: https://www.docker.com/blog/how-i-built-my-first-containerized-java-web-application/

### How to run on Eclipse

You need to run the Java Spring Boot application, then using a browser you can see the local web interface at http://localhost:8080/
