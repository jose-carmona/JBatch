# JBatch

The main objective of the project is to provide a suitable test environment for Java EE 7 Batch.

This project, also, is to learn and explore Java EE 7 Batch. We are going to:

1) Create 100,000 entities with random details.
2) Compute sum of amount attribute of details of each entity.
3) Make a simply jsf interface to run the Batch Job and show log.


### Components

* Docker. Nothing is necesary to be installed but Docker.
* Payara Server to run the app.
* Testing:
    * [Junit 5](https://junit.org/junit5/)
    * [Weld-JUnit](https://github.com/weld/weld-junit)
    * [Jberet](https://github.com/jberet)
    * [EclipseLink](https://www.eclipse.org/eclipselink/) + [h2database](https://www.h2database.com/html/main.html)

### How run the app

```
$ build.sh
$ run.sh
```