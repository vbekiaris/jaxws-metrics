# jaxws-metrics
[Metrics](https://github.com/dropwizard/metrics) for JAX-WS services. This project
 provides a JAX-WS handler which maintains duration & throughput per web service
 operation using [Metrics Timers](http://metrics.dropwizard.io/3.1.0/manual/core/#timers).


# Configuration
## Build and install
Build and install the artifact in your local repository:

`mvn clean install`

Note: you need at least JDK 7 to build and execute the tests in this project. However the resulting JAR
 can be used with JDK 1.5+.

## Add the dependency in your pom.xml
    <dependency>
        <groupId>com.github.vbekiaris</groupId>
        <artifactId>jaxws-metrics</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


## Instantiate the metrics handler
The metrics handler `com.github.vbekiaris.jaxwsmetrics.MetricsHandler` assumes you
 already have a `com.codahale.metrics.MetricRegistry` configured to collect all your
 application metrics; this `MetricRegistry` instance can be supplied either as constructor argument
 or via property setter. For example, a Spring bean configuration would look
 like this:

    <!-- the metric registry -->
    <bean id="metricRegistry" class="com.codahale.metrics.MetricRegistry"/>

    <bean id="metricsHandler" class="com.github.vbekiaris.jaxwsmetrics.MetricsHandler">
        <constructor-arg index="0">
            <ref bean="metricRegistry"/>
        </constructor-arg>
    </bean>

## Add the handler
Depending on how you configure your web service, you need to let your web services
 framework know it must invoke a JAX-WS handler. For example, when using [Apache CXF](http://cxf.apache.org/)
 with Spring XML configuration, the following snippet should configure the `echo` endpoint
 with the metrics handler:

    <jaxws:endpoint id="echo" implementor="#echoWebService" address="/echo">
        <jaxws:handlers>
            <ref bean="metricsHandler"/>
        </jaxws:handlers>
    </jaxws:endpoint>

