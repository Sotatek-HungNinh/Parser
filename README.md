## CDDL Parser project

Generate pojo class using maven plugin
##### 1. Add this block code to pom.xml in maven project

```
<build>
    <plugins>
        <plugin>
            <groupId>com.cardano.parser</groupId>
            <artifactId>parser-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>cddl-parser</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

##### 2. Run command
```
mvn com.cardano.parser:parser-maven-plugin:cddl-parser -Dfile.path="path/to/file.cddl" -Doutput.folder="path/to/folder"
```
