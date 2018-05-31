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

## Usage


### Consumer

Configure plugin in your pom.xml using the *upload-pacts* goal:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
      <executions>
        <execution>
          <id>upload-pacts</id>
          <phase>test</phase>
          <goals><goal>upload-pacts</goal></goals>
          <configuration>
            <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
            <pacts>${project.build.directory}/pacts</pacts>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Producer

Configure plugin in your pom.xml. This time,
the *download-pacts* goal is used:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
      <executions>
        <execution>
          <id>download-pacts</id>
          <phase>generate-resources</phase>
          <goals><goal>download-pacts</goal></goals>
          <configuration>
            <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
            <pacts>${project.build.testOutputDirectory}/pacts-dependents</pacts>
            <provider>provider</provider>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
### Having consumer and producer in a multi-module project
Configure plugin at the parent pom
```xml
<build>
  <plugins>
      <plugin>
            <groupId>com.github.warmuuh</groupId>
            <artifactId>pactbroker-maven-plugin</artifactId>
            <version>0.0.14</version>
            <executions>
                <execution>
                    <goals>
                        <goal>upload-pacts</goal>
                    </goals>
                    <phase>none</phase>
                </execution>
                <execution>
                    <id>download-pacts</id>
                    <phase>none</phase>
                    <goals>
                        <goal>download-pacts</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
  </plugins>
</build>
```

At *generate-test-resources* phase the relevant provider pacts will be downloaded to the *providerPacts* directory to be able to verify provider behavior against consumers, meanwhile consumer pacts will be uploaded at the *verify* phase from the *consumerPacts* directory which was previously generated by tests. With this approach there is no need for profiles. A simple **mvn clean install** does the job.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
          <executions>
              <execution>
                  <goals>
                      <goal>upload-pacts</goal>
                  </goals>
                  <phase>verify</phase>
                  <configuration>
                    <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
                    <pacts>${project.build.directory}/pacts</pacts>
                  </configuration>
              </execution>
              <execution>
                  <id>download-pacts</id>
                  <phase>generate-test-resources</phase>
                  <goals>
                      <goal>download-pacts</goal>
                  </goals>
                  <configuration>
                    <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
                    <pacts>${project.build.testOutputDirectory}/pacts-dependents</pacts>
                    <provider>provider</provider>
                  </configuration>
              </execution>
          </executions>
    </plugin>
  </plugins>
</build>
```
If you are using [scala-pact](https://github.com/ITV/scala-pact) from ITV to generate pacts than it generates separate pact file for all the tests. Before you upload it to the broker you might want to group and merge them based on the provider and customer name. Use **mergePacts** config element to force pact merge before upload to the broker
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
      <executions>
        <execution>
          <id>upload-pacts</id>
          <goals><goal>upload-pacts</goal></goals>
          <configuration>
            <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
            <pacts>${project.build.directory}/pacts</pacts>
            <mergePacts>true</mergePacts>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
### Security
To provide credentials when using git repository while uploading
or downloading pacts, use the configuration sections as below:
```xml
    <configuration>
      <brokerUrl>https://github.com/pact-repo.git</brokerUrl>
      <pacts>target/pacts-dependents</pacts>
      <provider>provider</provider>
      <username>user</username>
      <password>password</password>
    </configuration>
```

To provide credentials when using a pact broker with HTTP basic auth, 
use the configuration sections as below:
```xml
    <configuration>
      <brokerUrl>https://yourbroker.pact.dius.com.au</brokerUrl>
      <pacts>target/pacts-dependents</pacts>
      <provider>provider</provider>
      <username>user</username>
      <password>password</password>
    </configuration>
```
You can also supply `<insecure>true</insecure>` to ignore certificate validation.

### Tagging Pacts
[Tagging pact versions](https://github.com/pact-foundation/pact_broker/wiki/Using-tags) is a useful technique that is supported on pact uploads. There are multiple methods available for you to tag your pacts.

#### From command line
Running a build and passing a list of tags to the parameter `-Dpact.tagNames` will upload the pacts and tag the version as directed.

For example, running `mvn clean install -Dpact.tagNames=foo,bar,baz` will upload the pacts and tag the uploaded version with the tags "foo", "bar", and "baz".

#### List tags in pom
This example will upload the pact as version 1.2.3, and tag that version with the tags "foo", "bar", and "baz".
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
      <executions>
        <execution>
          <id>upload-pacts</id>
          <phase>test</phase>
          <goals><goal>upload-pacts</goal></goals>
          <configuration>
            <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
            <pacts>${project.build.directory}/pacts</pacts>
            <consumerVersion>1.2.3</consumerVersion>
            <tagNames>
              <tagName>foo</tagName>
              <tagName>bar</tagName>
              <tagName>baz</tagName>
            </tagNames>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

**Note:** Tag names in the pom will override tags provided from the command line.

#### Single tag (legacy)
Kept in for backwards compatibility, you can provide a single `tagName` in the pom.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.warmuuh</groupId>
      <artifactId>pactbroker-maven-plugin</artifactId>
      <version>0.0.14</version>
      <executions>
        <execution>
          <id>upload-pacts</id>
          <phase>test</phase>
          <goals><goal>upload-pacts</goal></goals>
          <configuration>
            <brokerUrl>ssh://gitlab/pact-repo.git</brokerUrl>
            <pacts>${project.build.directory}/pacts</pacts>
            <consumerVersion>1.2.3</consumerVersion>
            <tagName>i-am-a-lonely-tag</tagName>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```
