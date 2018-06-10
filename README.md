[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# opentracing-curator
OpenTracing instrumentation for curator framework

## Installation

pom.xml
```xml
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-curator</artifactId>
    <version>0.0.1</version>
</dependency>
```

build.gradle
```groovy
compile 'io.opentracing.contrib:opentracing-curator:0.0.1'
```

## Usage
Add the tracing module in your list
```java
CuratorFramework client = ...;
TracerDriver tracerDrivier = new OpentracingCuratorDriver();
client.getZookeeperClient().setTracerDriver(tracerDrivier);
```

Alternatively, you can define the peer service name
```java
CuratorFramework client = ...;
TracerDriver tracerDrivier = new OpentracingCuratorDriver("peerServiceName");
client.getZookeeperClient().setTracerDriver(tracerDrivier);
```


You can find more info on curator [here](https://github.com/apache/curator)

## Tracing tags
The following tags are added to traces :
 
| Span tag name | Notes |
|:--------------|:-------------------|
| `span.kind` | `client` |
| `component` | `java-curator` |
| `peer.service` | if exists, the peer service name set in `OpentracingCuratorDriver` ctor |
| `error` | `true` if any error occurred. `false` otherwise |
| `session.id` | the zookeeper session id of the current operation |
| `path` | if exists, the zookeeper node on which the operation took place |

[ci-img]: https://travis-ci.org/opentracing-contrib/java-curator.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-curator
[cov-img]: https://coveralls.io/repos/github/opentracing-contrib/java-curator/badge.svg?branch=master
[cov]: https://coveralls.io/github/opentracing-contrib/java-curator?branch=master
[maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-curator.svg
[maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-curator