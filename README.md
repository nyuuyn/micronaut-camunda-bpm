# micronaut-camunda-bpm

This open source project allows you to easily integrate [Camunda](https://camunda.com/products/bpmn-engine/) into [Micronaut](https://micronaut.io) projects.

Micronaut is known for its efficient use of resources. With this integration you embed the BPMN 2.0 compliant and developer friendly Camunda process engine with minimal memory footprint into your application.

The integration is preconfigured with sensible defaults, so that you can get started with minimal configuration: simply add a dependency in your Micronaut project!

Do you want to try it out? Please jump to the [Getting Started](#getting-started) section.

Do you want to contribute to our open source project? Please read the [Contribution Guidelines](CONTRIBUTING.md) and [contact us](#contact).

Micronaut + Camunda = :heart:

[![Release](https://img.shields.io/github/v/release/NovatecConsulting/micronaut-camunda-bpm.svg)](https://github.com/NovatecConsulting/micronaut-camunda-bpm/releases)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Continuous Integration](https://github.com/NovatecConsulting/micronaut-camunda-bpm/workflows/Continuous%20Integration/badge.svg)](https://github.com/NovatecConsulting/micronaut-camunda-bpm/actions)
[![Join the chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/NovatecConsulting/micronaut-camunda-bpm)

# Table of Contents

* [Features](#features)
* [Getting Started](#getting-started)
  * [Supported JDKs](#supported-jdks)
  * [Dependency Management](#dependency-management)
  * [Deploying Models](#deploying-models)
  * [Camunda Integration](#camunda-integration)
  * [Configuration](#configuration)
* [Advanced Topics](#advanced-topics)
  * [Camunda REST API and Webapps](#camunda-rest-api-and-webapps)
  * [Add Process Engine Plugins](#add-process-engine-plugins)
  * [Custom Process Engine Configuration](#custom-process-engine-configuration)
  * [Custom JobExecutor Configuration](#custom-jobexecutor-configuration)
  * [Transaction Management](#transaction-management)
  * [Process Tests](#process-tests)
  * [Pitfalls](#pitfalls)
* [Releases](#releases)
* [Contact](#contact)

# Features
* Camunda can be integrated as an embedded process engine into a Micronaut project by simply [adding a dependency](#dependency-management) in build.gradle (Gradle) or pom.xml (Maven).
* Using H2 as an in-memory database is as simple as [adding a dependency](#dependency-management). Other [data sources can be configured](#data-source) via properties.
* BPMN process models and DMN decision tables found in the root of the resources are [automatically deployed](#deploying-models).
* The Camunda process engine with its job executor is started automatically - but the job executor is disabled for tests by default.
* The process engine and related services, e.g. RuntimeService, RepositoryService, ..., are provided as lazy initialized beans and [can be injected](#camunda-integration).
* Micronaut beans are resolved from the application context if they are [referenced by expressions or Java class names](#java-delegates) within the process models.
* The process engine [integrates with Micronaut's transaction manager](#transaction-management). Optionally, micronaut-data-jdbc or micronaut-data-jpa are supported.
* The process engine can be configured with [generic properties](#generic-properties).
* The [Camunda REST API and the Webapps](#camunda-rest-api-and-webapps) are supported (currently only for Jetty).
* [Process Engine Plugins](#add-process-engine-plugins) are automatically activated on start.
* The [process engine configuration](#custom-process-engine-configuration) and the [job executor configuration](#custom-jobexecutor-configuration) can be customized programmatically.
* A Camunda admin user is created if configured by [properties](#properties) and not present yet (including admin group and authorizations).
* Camunda's telemetry feature is automatically deactivated during test execution.

# Getting Started

This section describes what needs to be done to use `micronaut-camunda-bpm-feature` in a Micronaut project.

Here are some example applications:
* [Onboarding Process](https://github.com/tobiasschaefer/micronaut-camunda-example-onboarding) with service tasks, user tasks, and message correlation.
* Simple [application with Java/Maven](https://github.com/tobiasschaefer/micronaut-camunda-example-java-maven)
* Simple [application with Kotlin/Gradle](https://github.com/tobiasschaefer/micronaut-camunda-example-kotlin-gradle)
* [Internal example application](/micronaut-camunda-bpm-example) used during development

## Supported JDKs

We officially support the following JDKs:
* JDK 8 (LTS)
* JDK 11 (LTS)
* JDK 15 (latest version supported by Micronaut)

## Dependency Management
### Gradle
1. Optional: Create an empty Micronaut project using [Micronaut Launch](https://launch.micronaut.io) or alternatively with the CLI: `mn create-app my-example`. 
2. Add the dependency to the build.gradle:
```groovy
implementation("info.novatec:micronaut-camunda-bpm-feature:0.18.1")
runtimeOnly("com.h2database:h2")
```

Note: The module `micronaut-camunda-bpm-feature` includes the dependency `org.camunda.bpm:camunda-engine` which will be resolved transitively.

### Maven
1. Optional: Create an empty Micronaut using [Micronaut Launch](https://launch.micronaut.io) or alternatively with the CLI:  `mn create-app my-example --build=maven`.
2. Add the dependency to the pom.xml:
```xml
<dependency>
  <groupId>info.novatec</groupId>
  <artifactId>micronaut-camunda-bpm-feature</artifactId>
  <version>0.18.1</version>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

Note: The module `micronaut-camunda-bpm-feature` includes the dependency `org.camunda.bpm:camunda-engine` which will be resolved transitively.

##  Deploying Models
BPMN process models (`*.bpmn`) and DMN decision tables (`*.dmn`) should be created with the [Camunda Modeler](https://camunda.com/products/camunda-bpm/modeler) and saved  in `src/main/resources`.

When starting the application you'll see the log output: `Deploying model: xxxxxxx.bpmn`

## Camunda Integration

### Process Engine and Services

Inject the process engine or any of the Camunda services using constructor injection:
```java
// ...

import javax.inject.Singleton;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;

@Singleton
public class MyComponent {

    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    
    public MyComponent(ProcessEngine processEngine, RuntimeService runtimeService) {
        this.processEngine = processEngine;
        this.runtimeService = runtimeService;
    }

    // ...
}
```

Alternatively to constructor injection, you can also use field injection, Java bean property injection, or method parameter injection.

You can then for example use the `runtimeService` to start new processes instances or correlate existing process instances.

### Java Delegates

To invoke a Java delegate create a bean and reference it in your process model using an expression, e.g. `${loggerDelegate}`:

```java
import javax.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Singleton
public class LoggerDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LoggerDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("Hello World: {}", delegateExecution);
    }
}
```

Internally, the bean will be resolved using `io.micronaut.inject.qualifiers.Qualifiers.byName(...)`.
Therefore, you can use the annotation `javax.inject.Named` to define an explicit bean name and use that name in your expression.

## Configuration

### Data Source

By default, an in-memory H2 data source is preconfigured. Remember to add the runtime dependency `com.h2database:h2` mentioned in [Dependency Management](#dependency-management).

However, you can configure any other database, e.g. in `application.yml`:

```yaml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: secret
    driver-class-name: org.postgresql.Driver
```

after adding the appropriate driver as a dependency:

```groovy
runtimeOnly("org.postgresql:postgresql:42.2.18")
```

### Connection Pool with HikariCP

This integration uses HikariCP as a database connection pool to optimize performance. By default, the following configuration is applied:
* `datasources.default.minimum-idle: 10`
* `datasources.default.maximum-pool-size: 50`

### Properties

You may use the following properties (typically in application.yml) to configure the Camunda integration.

| Prefix                |Property          | Default                                      | Description            |
|-----------------------|------------------|----------------------------------------------|------------------------|
| camunda.admin-user    | .id              |                                              | If present, a Camunda admin account will be created by this id (including admin group and authorizations) |
|                       | .password        |                                              | Admin's password (mandatory if the id is present)  |
|                       | .firstname       |                                              | Admin's first name (optional, defaults to the capitalized id) |
|                       | .lastname        |                                              | Admin's last name (optional, defaults to the capitalized id) |
|                       | .email           |                                              | Admin's email address (optional, defaults to &lt;id&gt;@localhost) |
| camunda.rest          | .enabled         | false                                        | Enable the REST API |
|                       | .context-path    | /engine-rest                                 | Context path for the REST API |
|                       | .basic-auth-enabled | false                                     | Enables basic authentication for the REST API |
| camunda.webapps       | .enabled         | false                                        | Enable the Webapps (Cockpit, Task list, Admin) |
|                       | .context-path    | /camunda                                     | Context path for the Webapps |
|                       | .index-redirect-enabled | true                                  | Registers a redirect from / to the Webapps |
| camunda.filter        | .create          |                                              | Name of a "show all" filter for the task list |

### Generic Properties

The process engine can be configured using generic properties listed in Camunda's Documentation: [Configuration Properties](https://docs.camunda.org/manual/latest/reference/deployment-descriptors/tags/process-engine/#configuration-properties).

The properties can be set in kebab case (lowercase and hyphen separated) or camel case (indicating the separation of words with a single capitalized letter as written in Camunda's documentation). Kebab case is preferred when setting properties.

Some of the most relevant properties are:
* database-schema-update (databaseSchemaUpdate)
* history
* initialize-telemetry (initializeTelemetry)
* telemetry-reporter-activate (telemetryReporterActivate)

Example:

```yaml
camunda:
  generic-properties:
    properties:
      initialize-telemetry: true
```

# Advanced Topics

## Camunda REST API and Webapps

Currently, the Camunda REST API and Webapps (Cockpit, Task list, and Admin) are only supported on the server runtime Jetty.

To use them in your project, you have to set the micronaut runtime of your project to `jetty`, e.g.

<details>
<summary>Click to show Gradle configuration</summary>

micronaut-gradle-plugin configuration in build.gradle:

```groovy
micronaut {
  runtime("jetty")
  [...]
}
```
</details>

<details>
<summary>Click to show Maven configuration</summary>

micronaut-maven-plugin configuration in pom.xml:

```xml
<properties>
  [...]
  <micronaut.runtime>jetty</micronaut.runtime>
</properties>
```

If you use Maven, you have to remove this dependency in the pom.xml:
```xml
<dependency>
  <groupId>io.micronaut</groupId>
  <artifactId>micronaut-http-server-netty</artifactId>
  <scope>compile</scope>
</dependency>
```
and replace it with
```xml
<dependency>
  <groupId>io.micronaut.servlet</groupId>
  <artifactId>micronaut-http-server-jetty</artifactId>
</dependency>
```
</details>

### Configuration of REST API and Webapps
By default, REST API and the Webapps are not enabled. You have to configure them e.g. in the application.yaml as follows:

```yaml
camunda:
  webapps:
    enabled: true
  rest:
    enabled: true
```

Further Information:
* The Webapps are by default available at `/camunda`. By default, `/` will redirect you there.
* The REST API is by default available at `/engine-rest`, e.g. to get the engine name use `GET /engine-rest/engine`.
* See [Configuration Properties](#properties) on how to enable basic authentication for REST, create a default user, or disable the redirect.
* Enabling the REST API or the Webapps impacts the startup time. Depending on your hardware it increases by around 500-1000 milliseconds.

## Add Process Engine Plugins
Every bean that implements the interface `ProcessEnginePlugin` gets automatically added to the process engine on start.
To add your plugin you just need to annotate it with `@Singleton`. Here is an example for a `DoNothingPlugin`.

```java
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import javax.inject.Singleton;

@Singleton
public class DoNothingPlugin implements ProcessEnginePlugin {
  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {}

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {}

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {}
}
```

## Custom Process Engine Configuration
With the following bean it's possible to customize the process engine configuration:

```java
import info.novatec.micronaut.camunda.bpm.feature.MnProcessEngineConfiguration;
import info.novatec.micronaut.camunda.bpm.feature.ProcessEngineConfigurationCustomizer;
import io.micronaut.context.annotation.Replaces;
import javax.inject.Singleton;

@Singleton
@Replaces(ProcessEngineConfigurationCustomizer.class)
public class MyProcessEngineConfigurationCustomizer implements ProcessEngineConfigurationCustomizer {
    @Override
    public void customize(MnProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setProcessEngineName("CustomizedEngine");
    }
}
```

## Custom JobExecutor Configuration
With the following bean it's possible to customize the job executor:

```java
import info.novatec.micronaut.camunda.bpm.feature.JobExecutorCustomizer;
import info.novatec.micronaut.camunda.bpm.feature.MnJobExecutor;
import io.micronaut.context.annotation.Replaces;
import javax.inject.Singleton;

@Singleton
@Replaces(JobExecutorCustomizer.class)
public class MyJobExecutorCustomizer implements JobExecutorCustomizer {
    @Override
    public void customize(MnJobExecutor jobExecutor) {
        jobExecutor.setWaitTimeInMillis(300);
    }
}
```

## Transaction management

By default the process engine integrates with Micronaut's transaction manager and uses a Hikari connection pool:
* When interacting with the process engine, e.g. starting or continuing a process, the existing transaction will be propagated.
* JavaDelegates and Listeners will have the surrounding Camunda transaction propagated to them allowing the atomic persistence of data.

Optionally, `micronaut-data-jdbc` or `micronaut-data-jpa` are supported.

### Using micronaut-data-jdbc

To enable embedded transactions management support **with micronaut-data-jdbc** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-data-jdbc")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-jdbc</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure the JDBC properties as described [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#jdbc).

### Using micronaut-data-jpa

To enable embedded transactions management support **with micronaut-data-jpa** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-hibernate-jpa")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-hibernate-jpa</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure JPA as described in [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#hibernate).

## Process Tests

Process tests can easily be implemented with JUnit 5 by adding the `camunda-bpm-assert` library as a dependency:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
testImplementation("org.camunda.bpm.assert:camunda-bpm-assert:8.0.0")
testImplementation("org.assertj:assertj-core:3.16.1")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>org.camunda.bpm.assert</groupId>
  <artifactId>camunda-bpm-assert</artifactId>
  <version>8.0.0</version>
  <scope>test</scope>
</dependency>
<dependency>
<groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>3.16.1</version>
  <scope>test</scope>
</dependency>
```
</details>

and then implement the test using the usual `@MicronautTest` annotation:

```java
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

@MicronautTest
class HelloWorldProcessTest {

    @Inject
    RuntimeService runtimeService;

    @Test
    void happyPath() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HelloWorld");
        assertThat(processInstance).isStarted();

        assertThat(processInstance).isWaitingAt("TimerEvent_Wait");
        execute(job());

        assertThat(processInstance).isEnded();
    }
}
```

Note: the integration automatically disables the job executor and the process engine's telemetry feature during test execution. This is deduced from the "test" profile.

See also a test in our example application: [HelloWorldProcessTest](/micronaut-camunda-bpm-example/src/test/java/info/novatec/micronaut/camunda/bpm/example/HelloWorldProcessTest.java)

## Pitfalls

### No version information in Fat/Uber/Shadow JAR

If you create a Fat/Uber/Shadow JAR and run that you will see a warning:

`WARN  i.n.m.c.b.f.MnProcessEngineConfiguration - The Camunda version cannot be determined. If you created a Fat/Uber/Shadow JAR then please consider using the Micronaut Application Plugin's 'dockerBuild' task to create a Docker image.`

This is because the repackaging of the jars implicitly removes the META-INF information.

Instead, of creating a Fat/Uber/Shadow JAR, please use the [Micronaut Application Plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin/blob/master/README.md#micronaut-application-plugin)'s `dockerBuild` task to create a layered Docker image:

`./gradlew dockerBuild`

and use the resulting image to run a Docker container.

Missing version information leads to
* Detailed telemetry cannot be sent to Camunda because the version is mandatory. 

### Executing Blocking Operations on Netty's I/O Thread Pool
When using the default server implementation Netty, blocking operations must be performed on I/O instead of Netty threads to avoid possible deadlocks. Therefore, as soon as Camunda ["borrows a client thread"](https://docs.camunda.org/manual/current/user-guide/process-engine/transactions-in-processes/)  you have to make sure that the [event loop is not blocked](https://objectcomputing.com/resources/publications/sett/june-2020-micronaut-2-dont-let-event-loops-own-you).
A frequently occurring example is the implementation of a REST endpoint which interacts with the process engine. By default, Micronaut would use a Netty thread for this blocking operation. To prevent the use of a Netty thread it is recommended to use the annotation [`@ExecuteOn(TaskExecutors.IO)`](https://docs.micronaut.io/latest/guide/index.html#reactiveServer). This will make sure that an I/O thread is used.

```java
@Post("/hello-world-process")
@ExecuteOn(TaskExecutors.IO)
public String startHelloWorldProcess() {
    return runtimeService.startProcessInstanceByKey("HelloWorld").getId();
}
```

# Releases

The list of [releases](/releases) contains a detailed changelog.

The following compatibility matrix shows the officially supported Micronaut and Camunda versions for each release.
Other combinations might also work but have not been tested.

| Release |Micronaut | Camunda |
|--------|-------|--------|
| 0.18.1 | 2.3.3 | 7.14.0 |

<details>
<summary>Click to see older releases</summary>

| Release |Micronaut | Camunda |
|--------|-------|--------|
| 0.18.0 | 2.3.2 | 7.14.0 |
| 0.17.0 | 2.3.2 | 7.14.0 |
| 0.16.1 | 2.3.1 | 7.14.0 |
| 0.16.0 | 2.3.0 | 7.14.0 |
| 0.15.0 | 2.3.0 | 7.14.0 |
| 0.14.0 | 2.2.3 | 7.14.0 |
| 0.13.0 | 2.2.2 | 7.14.0 |
| 0.12.0 | 2.2.1 | 7.14.0 |
| 0.11.0 | 2.2.1 | 7.14.0 |
| 0.10.1 | 2.2.0 | 7.14.0 |
| 0.10.0 | 2.2.0 | 7.14.0 |
| 0.9.0 | 2.1.3 | 7.14.0 |
| 0.8.0 | 2.1.2 | 7.13.0 |
| 0.7.0 | 2.1.1 | 7.13.0 |
| 0.6.0 | 2.1.0 | 7.13.0 |
| 0.5.3 | 2.0.1 | 7.13.0 |
| 0.5.2 | 2.0.0 | 7.13.0 |
| 0.5.1 | 2.0.0 | 7.13.0 |
| 0.5.0 | 2.0.0 | 7.13.0 |
| 0.4.2 | 1.3.6 | 7.13.0 |
| 0.3.1 | 1.3.5 | 7.12.0 |
| 0.2.2 | 1.3.3 | 7.12.0 |
| 0.2.1 | 1.3.3 | 7.12.0 |
| 0.2.0 | 1.3.3 | 7.12.0 |
| 0.1.0 | 1.3.3 | 7.12.0 |
</details>



Download of Releases:
* [GitHub Artifacts](https://github.com/NovatecConsulting/micronaut-camunda-bpm/releases)
* [Maven Central Artifacts](https://search.maven.org/artifact/info.novatec/micronaut-camunda-bpm-feature)

# Contact

This open source project is being developed by [Novatec Consulting GmbH](https://www.novatec-gmbh.de/en/) with the support of the open source community.

If you have any questions or ideas feel free to create an [issue](https://github.com/NovatecConsulting/micronaut-camunda-bpm/issues) or contact us via Gitter or mail.

We'd also like to hear from you if you're using the project :-)

Do you want to contact the core team?
* [Chat via Gitter](https://gitter.im/NovatecConsulting/micronaut-camunda-bpm)
* [mailto:micronaut-camunda@novatec-gmbh.de](mailto:micronaut-camunda@novatec-gmbh.de)
