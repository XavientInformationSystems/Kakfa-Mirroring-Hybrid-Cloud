/* This class contains all the constants that are being used in the code */

package com.xavient.dataingest.spark.constants;

public class Constants {

	public static final String CONFIG = "config";
	public static final String REWIND = "rewind";
	public static final String SOURCE_KAFKA_TOPIC = "source.kafka.topic";
	public static final String SOURCE_ZK_HOST = "source.zookeeper.host";
	public static final String SOURCE_ZK_PORT = "source.zookeeper.port";
	public static final String TARGET_KAFKA_TOPIC = "target.kafka.topic";
	public static final String TARGET_ZK_HOST = "target.zookeeper.host";
	public static final String TARGET_ZK_PORT = "target.zookeeper.port";
	public static final String HDFS_OUTPUT_PATH = "hdfs.output.path";
	public static final String HDFS_OUTPUT_DELIMITER = "hdfs.output.delimiter";
	public static final String CLUSTER_FS_URL = "cluster.fs.url";

	public static final String DELIMITER_PREFIX = "\\";

	public static final String S3_OUTPUT_PATH = "s3.output.path";	

	public static final String HDFS_USER_NAME = "hdfs.user.name";
	public static final String SPARK_MASTER_URL = "spark.master.url";
}