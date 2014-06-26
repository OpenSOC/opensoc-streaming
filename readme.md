# OpenSOC-Streaming

Extensible set of Storm topologies and topology attributes for streaming, enriching, indexing, and storing telemetry in Hadoop.


## Usage

mvn clean
mvn install

This will build all the projects at the same time

Navigate to OpenSOC-Topologies/target
Maven should build OpenSOC-Topologies-0.0.1-SNAPSHOT.jar

To run the Bro topology:
storm -jar /path/to/OpenSOC-Topologies-0.0.1-SNAPSHOT.jar ./storm jar com.opensoc.topologies.BroEnrichmentTestTopology

To run the Sourcefire topology:
storm -jar /path/to/OpenSOC-Topologies-0.0.1-SNAPSHOT.jar ./storm jar com.opensoc.topologies.SourcefireEnrichmentTestTopology

## Importing to Eclipse on OSX

Download the git OSX app here: https://mac.github.com/

Use the app to check out the repository

Create a new Eclipse workspace

Create a symbolic link from the checked out repo to the Eclipse workspace...here is an example

cd
/Users/jsirota/Documents/workspace
ln -s ~/Documents/https\:/github.com/OpenSOC/opensoc-streaming/ opensoc-streaming
ls -al

You should now see a symbolic link to the git repo

Now in Eclipse go to

file->import->maven->existing maven project

Then right-click on OpenSOC-Streaming and click on run as -> maven build

enter install in the goals box and run it

The project should build

I did not get any luck building it via the Eclipse GIT plugin.  If you can think of a better way of building this please post it here

