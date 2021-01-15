# monkey-netty
![Build](https://github.com/tlf30/monkey-netty/workflows/Java%20CI%20with%20Gradle/badge.svg)  
An implementation of a server-client communication system for jMonkeyEngine using Netty.IO that utilizes both TCP and UDP communication.  
See example for server and client in `examples` module

## Installing with Gradle
In your `build.gradle` you will need to:

1. Include the github repo:
```groovy
repositories {
    ...
    maven {
        url = 'https://maven.pkg.github.com/tlf30/monkey-netty'
    }
}
```

2. Specify the dependency:
```groovy
dependencies {
    ...
    implementation 'io.tlf.monkeynetty:monkey-netty:0.1.0'
}
```

## Installing with Maven
In your pom.xml you will need to:

1. Include the github repo:
```xml
<repositories>
    ...
    <repository>
        <id>monkey-netty</id>
        <name>Monkey-Netty GitHub Packages</name>
        <url>https://maven.pkg.github.com/tlf30/monkey-netty</url>
    </repository>
</repositories>
```

2. Specify the dependency:
```xml
<dependencies>
    ...
    <dependency>
        <groupId>io.tlf.monkeynetty</groupId>
        <artifactId>monkey-netty</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

