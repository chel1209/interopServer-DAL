# interopServer-DAL
DAL server for interoperability with other breeding system databases

Refer to <a target="_blank" href="http://www.kddart.org">http://www.kddart.org</a> for full details on the complete DAL API.

## Version 1.0.0

This release of the DAL-interop server provides read-only access to the supported entities.

Operations supported are:

* `get/`_entity_
* `list/`_entity_
* `list/`_entity_`/_nperpage/page/_num`

as well as some of the other basic system operations (e.g. `list/operation`).


If you'd like to play with the server, refer to either the Java or
Javascript demo clients in the <a target="_blank" href="https://github.com/kddart/libJava-DAL">`libJava-DAL`</a>
or
<a target="_blank" href="https://github.com/kddart/libJavascript-DAL">`libJavascript-DAL`</a>
repositories respectively.

This project also contains a copy of the Javascript demo that is served from
the server's _home_ page.

See `src/test/com/diversityarrays/dalclient/JavadocDALClientDemo.java` for the Java
client. With the Javascript client you will have to open the demo.html file in
a browser or navigate from the server's _home_ page.

## Quick Start

* Download <a href="https://github.com/kddart/interopServer-DAL/blob/master/dalserver-bin.zip">`dalserver-bin.zip`</a> from Github and unzip.
* A new folder named `dalserver` will be created containing the
  `dalserver.jar` file and some libraries.
* Double-click `dalserver.jar` or use `java -jar dalserver.jar`
  from a command line.
* A user interface will display allowing you to select one of the
  supported `DalDatabase` options, set configuration parameters
  and then start the server.

If you wish to bypass the GUI, use a command line of the form:<pre>
   java -jar dalserver.jar <i>command-options</i> <b><i>DALDB_NAME</i></b>
</pre>
Use the option `-help` to see the available options.

### Example use:

* Configure the KDDart-DAL database to use the URL
  `http://kddart-dal.diversityarrays.com/dal` and
  _USERNAME_ and _PASSWORD_.
* Start the server on the host `localhost`
* Open a web browser on <a href="http://localhost:40112">http://localhost:40112/</a>


## Supporting other databases

Each supported database is implemented as an instance of `DalDatabase`.
You can create your own version of `DalDatabase` and make it available
via a parallel implementation of `DalDbProviderService` stored in a
library in the `classpath` that contains a `services`
entry.

The `DalDbProviderService` provides information to allow the User Interface
to present configuration parameters and, when these are available,
acts as a _factory_ to create the desired `DalDatabase` instance.

At runtime, the `dalserver` main program searches using
the <a target="_blank"
     href="http://docs.oracle.com/javase/7/docs/api/javax/imageio/spi/ServiceRegistry.html">`Service Registry`</a>
to discover all available instances of `DalDbProviderService` and
uses the results thereof to present the configuration panels for each `DalDatabase`.

There are a number of support classes and utilities available in the source
to facilitate construction of your own `DalDatabase`. Look at the existing
implementations for ideas and approaches.

### TODO:
* Enumerate supported entities
* Design Issues and Resolution
* DOCO: Describe the _entity_ X _operation_ approach
* DOCO: Detailed description on creating `DalDatabase`
* DOCO: Detailed description on _entity_ support especially
  for `Filtering` and related functionality
* Development Roadmap
