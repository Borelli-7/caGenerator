# caGenerator
Certificate generator is a standalone application able to generate PSD2 compliant test certificates with all possible PSD2 roles (ASPSP, PISP, AISP, PIISP).

## Prerequisites

- Java 17 or higher
- Json file containing tpp information.
  example: `Tpp.json`
  This will create a valid certificate.
```json
{
"authorizationNumber": "PSDDE-FAKENCA-87B2AC",
"roles": [
"PISP", "AISP"
],
"organizationName": "Fictional Corporation AG",
"organizationUnit": "Information Technology",
"domainComponent": "public.corporation.de",
"localityName": "Nuremberg",
"stateOrProvinceName": "Bayern",
"countryCode": "DE",
"validity": 365,
"commonName": "Fake NCA",
"ocspCheckNeeded": false
}
```
To create an expired certificate, set the validity in the `Tpp.json` to a negative number:

Example:
```
{
"authorizationNumber": "PSDDE-FAKENCA-87B2AC",
"roles": [
"PISP", "AISP"
],
"organizationName": "Fictional Corporation AG",
"organizationUnit": "Information Technology",
"domainComponent": "public.corporation.de",
"localityName": "Nuremberg",
"stateOrProvinceName": "Bayern",
"countryCode": "DE",
"validity": -365, //Set this value to any negative number
"commonName": "Fake NCA",
"ocspCheckNeeded": false
}
```

### How to use and run a library in other project

#### 1- Create a New Maven Project:
Create a new Maven project using a similar command

Example:
```shell
mvn archetype:generate -DgroupId=com.example -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

#### 2- Add the Library as a Dependency:
Open the `pom.xml` file of the new project and add a dependency for your library:
It allows you to create a build Fatjar when you do 'mvn clean package':
```xml
<dependencies>
    <dependency>
        <groupId>de.adorsys.psd2.qwac</groupId>
        <artifactId>certificate-generator-lib</artifactId>
        <version>4.2</version>
    </dependency>
</dependencies>

<build>
<finalName>Testin</finalName>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.3.0</version>
    </plugin>

   <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-assembly-plugin</artifactId>
          <version>3.6.0</version>
          <configuration>
              <descriptorRefs>
                  <descriptorRef>jar-with-dependencies</descriptorRef>
              </descriptorRefs>
              <archive>
                  <manifest>
                      <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                      <mainClass>org.example.Main</mainClass>
                  </manifest>
              </archive>
          </configuration>
          <executions>
              <execution>
                  <id>make-assembly</id>
                  <phase>package</phase>
                  <goals>
                      <goal>single</goal>
                  </goals>
              </execution>
          </executions>
      </plugin>
  </plugins>
</build>
```

_**Remark**: `<finalName>Testin</finalName>` You can replace "Testin" by the name of your artifact.
<mainClass>``org.example.Main``</mainClass> put your Main class package name here._

#### 3- Use the Library in Your Code:
You can now use the library classes and methods in your project.

For example:
```java
package org.example;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    public static void main(String[] args) {
        final int ARGS_SIZE = 1;
        try {
            // Check if the required arguments are provided
            if (args.length < ARGS_SIZE) {
            logger.info("Usage: java App <path/to/yourTppFile.json> [--target_folder <target_folder>]");
            return;
        }

            String tppJsonFilePath = args[0];
            // Optional target folder argument
            String targetFolder = args.length > 1 && "--target_folder".equals(args[1]) ? args[2] : "certs";

            CertificateService certificateService = new CertificateService();

            certificateService.generatePemFilesCerts(tppJsonFilePath, targetFolder);
        } catch (IOException e) {
             logger.error("An error occurred: {}", e.getMessage(), e);
        }
    }
}
```

#### 4- Build and Run Your Application:
Navigate to your project directory and build the application:
```shell
mvn clean package
```

##### Then run the application:
(run the second artifact created with the name found in the <descriptionRef></descriptionRef> in the ``pom.xml``)

###### - Using a default target folder:

`java -jar target/Testin-jar-with-dependencies.jar <path/to/yourTppFile.json>`

Example:
```shell
java -jar target/Testin-jar-with-dependencies.jar /home/user/Documents/Tpp.json
```

The default folder where the certificates are stored is called:```certs```.
It will be created by default in the root of your project directory

###### - Or using a specify target folder :

Specifying the target folder:

`java -jar target/Testin-jar-with-dependencies.jar <path/to/yourTppFile.json> --target_folder <path/to/target_folder>`

Example:
```shell
java -jar target/Testin-jar-with-dependencies.jar /home/user/Documents/Tpp.json --target_folder /home/User/Certs
```
#### Note:
In the "java -jar target/Testin-jar-with-dependency.jar <path/to/yourTppFile.json>"

Make sure the name of the Testin-jar-with-dependency comes from

`<finalName>Testin</finalName>`
and
`<descriptorRef>jar-with-dependencies</descriptorRef>`

There will be 2 artifacts created in the Target, run the second artifact.
The one with the description: jar-with-dependencies.
