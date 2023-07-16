# 2PL-scheduler

2PL scheduler for the Data Management project

## Requirements

- Check if the schedule inserted to satisfy the assumptions (no two read/write on the same object for the same transaction, no rollbacks)
- Insert a schedule, and check if it is in the 2PL class (with/without lock anticipation, with/without shared locks)
- Show conflict-serializability by precedence graph: Gen(2PL)=> DT(S) conflict serializable <=> P(S) is acyclic

## Application setup

This application is built using Spring Boot, Maven and Thymeleaf for the web part.

- Spring Boot initializer: https://start.spring.io/
- Spring Boot tutorial:

	- https://spring.io/guides/gs/spring-boot/
	- https://spring.io/guides/gs/serving-web-content/
- Thymeleaf:

	- https://www.thymeleaf.org/faq.html 
	- It replaces JSP pages: https://www.thymeleaf.org/doc/articles/thvsjsp.html

## Docker image

You can use a docker container to run this application, using this tutorial:

- Docker guide: https://www.docker.com/blog/how-i-built-my-first-containerized-java-web-application/

## How to run on Eclipse

You need to run the Java Spring Boot application, then using a browser you can see the local web interface at http://localhost:8080/
