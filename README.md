# monkey-netty
![Build](https://github.com/tlf30/monkey-netty/workflows/Java%20CI%20with%20Gradle/badge.svg)  
An implementation of a server-client communication system for jMonkeyEngine using Netty.IO that utilizes 
both TCP and UDP communication.

**Checkout our [Wiki](https://github.com/tlf30/monkey-netty/wiki) for getting started.**

**See example for server and client in `examples` module.**

## Installing with Gradle
Note: We will no longer be publishing packages to GitHub, future packages will be in Maven Central.  
In your `build.gradle` you will need to:

```groovy
dependencies {
    ...
    implementation 'io.tlf.monkeynetty:monkey-netty:0.1.0'
}
```

## Installing with Maven
Note: We will no longer be publishing packages to GitHub, future packages will be in Maven Central.   
In your pom.xml you will need to:

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

