Pactbroker Maven Plugin
==========

Pactbroker Maven Plugin integrates with [PactBroker](https://github.com/bethesque/pact_broker) and allows to
upload pacts created by consumer rsp download pacts that are verified against providers.

It also allows to use a **git-repository** instead of a PactBroker-instance as simplification so no additional infrastructure is needed.

Installation
-----
add on-demand-repository (using https://jitpack.io/)
```
<pluginRepositories>
	<pluginRepository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</pluginRepository>
</pluginRepositories>
```
or install locally
```
git clone ...
cd pactbroker-maven-plugin
mvn install
```

##Usage


###Consumer

Configure plugin in your pom.xml using the *upload-pacts* goal:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.5</version>
      <configuration>
        <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
        <pacts>target/pacts</pacts>
      </configuration>
      <executions><execution>
        <goals><goal>upload-pacts</goal></goals>
      </execution></executions>
    </plugin>
  </plugins>
</build>
```


###Producer

Configure plugin in your pom.xml. This time,
the *download-pacts* goal is used:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.5</version>
      <configuration>
        <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
        <pacts>target/pacts-dependents</pacts>
        <provider>provider</provider>
      </configuration>
      <executions><execution>
        <phase>generate-resources</phase>
        <goals><goal>download-pacts</goal></goals>
      </execution></executions>
    </plugin>
  </plugins>
</build>
```

