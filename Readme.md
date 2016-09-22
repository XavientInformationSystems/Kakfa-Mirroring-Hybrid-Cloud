# Kafka Mirroring in Hybrid Cloud Environment

This project teaches you how to leverage the power of Apache Kafka to replicate data between two clusters hosted in a hybrid cloud environment.

All you need is access to some basic programming skills, hands on experience with Linux and a little experience with AWS to get things kick started.

Before we proceed, let us explain what is a Hybrid Cloud Environment?

Hybrid cloud solutions combine the dynamic scalability of public cloud solutions with the flexibility and control of your private cloud. Hybrid cloud is particularly valuable for dynamic or highly changeable workloads.

For example, an enterprise can deploy an on-premises private cloud to host sensitive or critical workloads, but use a third-party public cloud provider, such as Google Compute Engine, to host less-critical resources, such as test and development workloads. To hold customer-facing archival and backup data, a hybrid cloud could also use Amazon Simple Storage Service (Amazon S3). 

Some of the benefits of Hybrid Computing include - 

-   Business Continuity
-   More Opportunity For Innovation
-   Scalability
-   Increased Speed To Market
-   Risk Management (test the waters)
-   Improved Connectivity
-   Secure Systems


### Kafka Mirroring

With Kafka mirroring feature you can maintain a replica of you existing Kafka Cluster.

The following diagram shows how to use the MirrorMaker tool to mirror a source Kafka cluster into a target (mirror) Kafka cluster.

The tool uses a Kafka consumer to consume messages from the source cluster, and re-publishes those messages to the local (target) cluster using an embedded Kafka producer 



![alt text](https://github.com/XavientInformationSystems/Kakfa-Mirroring-Hybrid-Cloud/blob/master/src/main/resources/Mirror.PNG "Mirror Architecture") 

### Use Case

Demonstrate hybrid cloud solution using Kafka Mirroring across regions 


### Environment Architecture

<IMAGE>

The architecture above represent two cluster environments, private and public cloud respectively, where data is replicated from source Kafka cluster to target Kafka cluster with the help of MirrorMaker tool and analysis over the data sets is performed using Spark Streaming clusters.

The internal environment stores all the data in HDFS which is accessible with Hive external tables. The purpose of storing data in HDFS is so that at any given point of time the raw data is never changed and can be used to tackle any discrepancies that might occur in the real time layer (target cluster).

The external environment receives the replicated data with the help of Mirror Maker and a spark streaming application is responsible to process that data and store it into Amazon S3. The crucial data that requires low level latency based on TTL is maintained in Amazon S3. The data is then pushed to Amazon Redshift where the user can issue low latency queries and have the results calculated on the go.

With the combine power of Hybrid Environment and Kafka mirroring you can perform different types of data analysis over streams of data with low latency

### Technology Stack

The below mentioned technology stack is generic in the sense that you are free to choose among a variety of databases and cloud service providers.

-   Source System – Twitter
-   Messaging System – Apache Kafka
-   Target System (Internal)– HDFS, Apache Hive
-   Target System (External) – Amazon S3 , Amazon Redshift
-   AWS Instance Type - EMR
-   Streaming API – Apache Spark
-   Programming Language – Java
-   IDE – Eclipse
-   Build tool – Apache Maven
-   Operating System – CentOS 7


### Workflow

-   A flume agent is configured to pull live data from twitter and push it to source Kafka broker which is hosted internally ( on premise infrastructure)

-   As soon as the data arrives on source Kafka cluster the data is to a target Kafka broker which is hosted on AWS (off-premise infrastructure) with the help of Kafka Mirror Tool

-   The data is then picked up from source and target Kafka brokers by their respective spark streaming applications

-   The Spark Streaming application running in the internal environment stores all the data over HDFS where you can query the data using Hive

-   The Spark Streaming application running in the external environment reads the data from the target Kafka cluster and stores it in Amazon S3 from where it is pushed to Amazon Redshift


### Pre-requisites

Kindly ensure that you have downloaded Apache Kafka in both internal and external environments  and the servers are up and running

### Instructions

- Create a Kakfa topic named "hybrid" by using the below command in both internal and external environments

```
./kafka-topics.sh --create --topic hybrid --zookeeper localhost:2181 --replication-factor 1 --partition 1
```

- Now set the required configurations files for Kaka Mirror

```
# In the source cluster you will need so specify the mirror-consumer and mirror-producer.
cd /usr/hdp/current/kafka-broker/config

# Make a file by the name mirror-consumer.properties and paste the below contents into it-

zookeeper.connect=127.0.0.1:2181
bootstrap.servers=localhost:6667
zookeeper.connection.timeout.ms=6000
group.id=test-consumer-group

# Make a file by the name mirror-producer.properties and paste the below contents into it -

metadata.broker.list=localhost:9092 (list of brokers used for bootstrapping knowledge about the rest of the cluster)
bootstrap.servers=xxx.xxx.xxx.xxx.amazonaws.com:9092
producer.type=sync
compression.codec=none
serializer.class=kafka.serializer.DefaultEncoder


# Start the Kafka Mirror in internal cluster by issuing the below command-

bin/kafka-run-class.sh kafka.tools.MirrorMaker --consumer.config config/mirror-consumer.properties --num.streams 1 --producer.config config/mirror-producer.properties --whitelist=".*"

# Your Kafka mirror shoud now be set, you can verify the same by sending some messages from source kafka producer and checking if the messages are being replicated across the target cluster

# Command to run producer in internal environment
./kafka-console-producer.sh --broker-list localhost:6667 --topic hybrid

# Command to run consumer in external environment
./kafka-console-consumer.sh --topic hybrid --zookeeper xxx.xxx.xxx.xxx.amazonaws.com:2181 --from-beginning
```

- Once the Kafka Mirror is up and running, download the codebase on your local as well as target cluster and extract the contents.

- Compile the code base on both the clusters using the below command-

```
mvn clean install

# On the internal clustser start the spark application which will read the data from source kafka cluster and store it in HDFS where you can issue hive queries.

# Command to start spark application on local cluster - 
spark-submit --class com.xavient.dataingest.spark.main.SparkIngestionSource --master yarn --jars /data/redshift.jdbc41-1.1.13.1013.jar target/uber-xav-kafka-mirror-0.0.1-SNAPSHOT.jar

# On the external clustser start the spark application which will read the data from target kafka cluster and store it in Amazon S3 from where the data will be pushed to Amazon Redshift. You will need to include the emrfs-hadoop-2.4.0.jar present in the project resources

# Command to start spark application on external cluster -
spark-submit --class com.xavient.dataingest.spark.main.SparkIngestionTarget --master yarn --jars /data/redshift.jdbc41-1.1.13.1013.jar,/data/emrfs-hadoop-2.4.0.jar target/uber-xav-kafka-mirror-0.0.1-SNAPSHOT.jar
```

- Copy the below given configuration and make a configuration file to be used by flume

```
# The configuration file needs to define the sources,the channels and the sinks.
# Sources, channels and sinks are defined per agent, in this case called 'TwitterAgent'

TwitterAgent.sources = Twitter
TwitterAgent.channels = MemChannel
TwitterAgent.sinks = Kafka

TwitterAgent.sources.Twitter.type = com.cloudera.flume.source.TwitterSource
TwitterAgent.sources.Twitter.channels = MemChannel
TwitterAgent.sources.Twitter.consumerKey = xxx-xxx-xxx-xxx-xxx
TwitterAgent.sources.Twitter.consumerSecret = xxx-xxx-xxx-xxx-xxx
TwitterAgent.sources.Twitter.accessToken = xxx-xxx-xxx-xxx-xxx
TwitterAgent.sources.Twitter.accessTokenSecret = xxx-xxx-xxx-xxx-xxx

TwitterAgent.sources.Twitter.keywords = allaboutbdata,xavient,xavientinformationsystems,followxavient,donald trump, hillary clinton, salman khan, superman, batman, modi, ramya,olympics, hadoop, big data, analytics, bigdata, cloudera, data science

TwitterAgent.channels.MemChannel.type = memory
TwitterAgent.channels.MemChannel.capacity = 1000
TwitterAgent.channels.MemChannel.transactionCapacity = 100

TwitterAgent.sinks.Kafka.type = org.apache.flume.sink.kafka.KafkaSink
TwitterAgent.sinks.Kafka.batchSize = 20
TwitterAgent.sinks.Kafka.topic = hybrid
TwitterAgent.sinks.Kafka.channel = MemChannel
TwitterAgent.sinks.Kafka.brokerList = localhost:6667

# Use the below command to start the flume agent - 
flume-ng agent -n TwitterAgent -f twitter-kafka.conf  --classpath  flume-sources-1.0-SNAPSHOT.jar
```

Once the flume agent start the data will start coming in live from Twitter which will be processed by both the clusters.

### Development
Want to contribute? Great!
