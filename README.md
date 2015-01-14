# interopServer-DAL
DAL server for interoperability with other breeding system databases

Refer to <a target="_blank" href="http://www.kddart.org">http://www.kddart.org</a> for full details on the complete DAL API.

## Version 1.0.0

This release of the DAL-interop server is intended to prove the feasibility of the concept,
investigate the issues in building such a cross-database service and to provide the
architectural underpinnings and foundation for future development.

On this basis, only read-only access to the entities is supported in this release.
For future development plans, see the <a href="#roadmap">Development Roadmap</a> below.

In this release, only two concrete implementaions of `DalDatabase` are
provided; one for _KDDart_ and one for _BMS_. Our understanding is that the BMS database
schema is currently undergoing change and it seemed inopportune to devote too much
effort until the extent of those changes is more fully understood.

### The Entities

* _Genus_
* _Genotype_
* _GenotypeAlias_

### Entity-related operations:

* list/genus
* get/genus/_id
* get/genotype/_id
* list/genotype/_nperpage/page/_num
* genotype/_genoid/list/alias
* get/genotypealias/_id
* list/genotypealias/_nperpage/page/_num


The `Filtering` clause is also supported for most of the fields that may be useful to search on
and applies to these retrieval operations.

### System-related operations:

* get/version
* list/operation
* get/login/status
* list/group
* switch/group/_groupid
* list/all/group
* _tname/list/field

## Related Github Projects

If you'd like to play with the server, refer to either the Java or
Javascript demo clients in the <a target="_blank" href="https://github.com/kddart/libJava-DAL">`libJava-DAL`</a>
or
<a target="_blank" href="https://github.com/kddart/libJavascript-DAL">`libJavascript-DAL`</a>
repositories respectively.

This project also contains a copy of the Javascript demo that is
served from the server's _home_ page.

See `src/test/com/diversityarrays/dalclient/JavadocDALClientDemo.java`
in the `libJava-DAL` project for the source of the Java client. A
compiled binary is available in that project but does require all its
library dependencies (also available in the `libJava-DAL` project).

For the Javascript client you may open the `demo.html` file in a
browser or navigate from the server's _home_ page.

## Quick Start

1. Download <a href="https://github.com/kddart/interopServer-DAL/blob/master/dalserver-bin.zip">`dalserver-bin.zip`</a>
   from Github and unzip.<br>
   This file contains a pre-built binary of the server and is ready to run.
2. A new folder named `dalserver` will be created containing the
   `dalserver.jar` file and the required library dependencies.
3. Double-click `dalserver.jar` or use `java -jar dalserver.jar`
   from a command line.
4. A user interface will display allowing you to select one of the
   supported `DalDatabase` options.
5. Choose one of the `DalDatabase` options by clicking on the _Tab_ with
   the database name.
6. Set values for the configuration parameters; those marked with a
   red asterisk are mandatory parameters.<br>
   Each `DalDatabase` has its own set of configuration parameters.<br>
   If you wish to test your settings, use the *Test* button.<br>
   To save the values you've entered, use the *Save Settings* button.
7. Click on the _Start Server_ button.

You should see a dialog popup titled *Creating DalDatabase: _database-name_*
while the database is created and any required initialisation occurs.

When initialisation is complete a new window will appear that will
display details of server activity.

If you wish to bypass the GUI, use a command line of the form:<pre>
   java -jar dalserver.jar <i>command-options</i> <b><i>DALDB_NAME</i></b>
</pre>
This form of startup will use the values you previously saved using the
*Save Settings* button. The settings (i.e. parameter values) for each `DalDatabase`
are saved separately - even if they appear to have the same name.

Use the option `-help` to see the available options.

### Example use:

* Configure the KDDart-DAL database to use the URL
  `http://kddart-dal.diversityarrays.com/dal` and
  _USERNAME_ and _PASSWORD_.
* Start the server on the host `localhost`
* Open a web browser on <a href="http://localhost:40112">http://localhost:40112/</a>

For the BMS database, provide a value for _Central JDBC URL_ value of the form:<pre>
    jdbc:mysql://_ipaddress_:_port_number/_schema-name_?user=_username
</pre>

(A JDBC driver for _mysql_ is already in the _classpath_ for the dalserver)

## Supporting other databases

Each supported database is implemented as an instance of
`DalDatabase`.  You can create your own version of `DalDatabase` and
make it available via a parallel implementation of
`DalDbProviderService` stored in a library in the `classpath`. This
library (`jar` file) must also contain a `services` entry. (see the
`Service Registry` link below for more details).

The `DalDbProviderService` instance provides information to allow the
User Interface to present configuration parameters and, when these are
provided, acts as a _factory_ to create the desired `DalDatabase`
instance.

At runtime, the `dalserver` main program uses
the <a target="_blank"
     href="http://docs.oracle.com/javase/7/docs/api/javax/imageio/spi/ServiceRegistry.html">`Service Registry`</a>
to discover all instances of `DalDbProviderService` and
uses these to present the configuration panels for each `DalDatabase`.

There are a number of support classes and utilities available in the source
to facilitate construction of your own `DalDatabase`. Look at the existing
implementations for ideas and approaches.

## Development Roadmap
<a name="roadmap">

Immediate plans for *DAL-interop* development are to support the entities and operations
required to let _KDXplore_ (DArT's data curation software) function with the server.
_KDXplore_ is designed to work in an _offline_ mode but it is able to operate _online_
to acquire data from and upload/update data to a DAL server.

This work will extend the range of entities to include at least:

* BreedingMethod
* Item
* ItemUnit
* Storage
* Trait
* TraitAlias
* Trial

and their related operations and will necessarily include _update_ operations.

As mentioned above, an understanding of the changes to the BMS schema will contribute to
an updated implementation of `BMS_DalDatabase` to adapt to these changes.

### TODO - Documentation

* Generate Javadoc (although you should be able to do this from your IDE or command line)
* Design Issues, Decisions and the reasons underpinning them
* Describe the _entity_ X _operation_ approach and why this reduces the
  development effort
* Detailed description on creating a new version of `DalDatabase`; the
  issues to be considered, design options available and the support classes
  relevant to each
* Detailed description on _entity_ support especially
  for `Filtering` and related functionality
