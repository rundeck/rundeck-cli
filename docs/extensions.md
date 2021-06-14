---
layout: page
title: Extensions
permalink: /extensions/
---

RD can use *extension jars* to add additional functionality.

## Using Extensions

Download an extension jar, and place it in the `RD_EXT_DIR` directory.

By default for UNIX this is located at `~/.rd/ext`, you can overridde the location in your `~/.rd/rd.conf` file by adding:

``` sh
export RD_EXT_DIR=/my/ext/dir
```

When you run `rd`, the extensions will be loaded and added as commands in the hierarchy of available subcommands.

You can check the list of loaded extensions by running `rd` with `RD_DEBUG=1`.

## Develop an Extension

Extensions can be developed as Java libraries.


### Dependencies

Add the `rd-cli-lib` dependency to your project.

Available in Maven Central.

Javadoc:

* [rd-cli-lib ![javadoc](https://javadoc.io/badge2/org.rundeck.cli/rd-cli-lib/javadoc.svg)](https://javadoc.io/doc/org.rundeck.cli/rd-cli-lib)
* [cli-toolbelt ![cli-toolbelt](https://javadoc.io/badge2/org.rundeck.cli-toolbelt/toolbelt/javadoc.svg)](https://javadoc.io/doc/org.rundeck.cli-toolbelt/toolbelt)


### Gradle example

A demo project can be seen here: <https://github.com/gschueler/rd-extension-demo>

~~~{groovy}
//use maven central
repositories { 
    mavenCentral()
}

dependencies {
    api "org.rundeck.cli:rd-cli-lib:{{site.app_version}}"
    api "org.rundeck.cli-toolbelt:toolbelt-jewelcli:0.2.28"
    implementation "org.rundeck.api:rd-api-client:{{site.app_version}}"

    implementation 'com.squareup.retrofit2:retrofit:2.7.1'
    implementation 'com.squareup.retrofit2:converter-jackson:2.7.1'
    implementation 'com.squareup.retrofit2:converter-jaxb:2.7.1'

}
~~~

## Implement `RdCommandExtension`

The following example adds the command `rd sub path somecomand`.

```java
package com.mycompany;
import org.rundeck.client.tool.extension.RdCommandExtension;
import org.rundeck.client.tool.extension.RdTool;
import org.rundeck.client.util.Client;
import org.rundeck.client.util.ServiceClient;
import org.rundeck.toolbelt.Command;
import org.rundeck.toolbelt.CommandOutput;
import org.rundeck.toolbelt.SubCommand;

@SubCommand(path={"sub","path"})
class MyClass implements RdCommandExtension{
    RdTool rdTool;
    public void setRdTool(RdTool rdTool){
        this.rdTool=rdTool;
    }   
    
    @Command
    public boolean someCommand(CommandOutput out){
        out.output("running someCommand");
    }
}
```

Argument parsing is done with the CLI Toolbelt, and can use the JewelCLI or Picocli libraries.  See the Example code.

## Declare the Service

The `rd` tool uses the Java ServiceLoader to load extensions on the classpath.

Declare your class in a file called `META-INF/services/org.rundeck.client.tool.extension.RdCommandExtension`

    com.mycompany.MyClass

For a standard Gradle java library, create the file in `src/main/resources/META-INF/services/org.rundeck.client.tool.extension.RdCommandExtension`
