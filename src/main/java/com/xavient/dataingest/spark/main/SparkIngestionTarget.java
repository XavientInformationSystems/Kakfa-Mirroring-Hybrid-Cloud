/* This class contains the main method, it is to be executed in the external cluster */

package com.xavient.dataingest.spark.main;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.xavient.dataingest.spark.constants.Constants;
import com.xavient.dataingest.spark.exception.DataIngestException;
import com.xavient.dataingest.spark.hdfsingestion.SparkHdfsIngestor;
import com.xavient.dataingest.spark.s3ingestion.SparkS3Ingestor;
import com.xavient.dataingest.spark.util.AppArgs;
import com.xavient.dataingest.spark.util.CmdLineParser;
import com.xavient.dataingest.spark.util.DataPayload;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

public class SparkIngestionTarget implements Serializable {

	private static final long serialVersionUID = 3289368866519818229L;

	public static void main(String[] args) throws ParserConfigurationException, IOException, DataIngestException {

		CmdLineParser cmdLineParser = new CmdLineParser();
		final AppArgs appArgs = cmdLineParser.validateArgs(args);

		System.setProperty("HADOOP_USER_NAME", appArgs.getProperty(Constants.HDFS_USER_NAME));

		SparkConf conf = new SparkConf().setAppName("SparkStreamingTest").setMaster("local[*]");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		jsc.hadoopConfiguration().set("fs.s3n.awsAccessKeyId", "xxx-xxx-xxx-xxx");
		jsc.hadoopConfiguration().set("fs.s3n.awsSecretAccessKey", "xxx-xxx-xxx-xxx");

		try {

			JavaStreamingContext jssc = new JavaStreamingContext(jsc, new Duration(2000));

			JavaPairReceiverInputDStream<String, String> stream = KafkaUtils.createStream(jssc,
					appArgs.getProperty(Constants.TARGET_ZK_HOST) + ":" + appArgs.getProperty(Constants.TARGET_ZK_PORT),
					"group", getKafkaTopics(appArgs));

			JavaDStream<String> lines = stream.map(tuple -> tuple._2);

			lines.print();

			JavaDStream<DataPayload> dataPayLoadDStream = payloadIngestor(lines);

			SparkS3Ingestor.s3dataWriter(dataPayLoadDStream, appArgs);

			jssc.start();
			jssc.awaitTermination();

		} finally {
			jsc.stop();

		}
	}

	private static Map<String, Integer> getKafkaTopics(AppArgs appArgs) {
		Map<String, Integer> topics = new HashMap<String, Integer>();
		topics.put(appArgs.getProperty(Constants.TARGET_KAFKA_TOPIC), 1);
		return topics;
	}

	private static JavaDStream<DataPayload> payloadIngestor(JavaDStream<String> lines) {
		JavaDStream<DataPayload> dataPayLoadDStream = lines.map(new Function<String, DataPayload>() {

			private static final long serialVersionUID = 8246041326936027578L;

			@Override
			public DataPayload call(String input) throws Exception {
				DataPayload dataPayload = new DataPayload();
				List<String> data = new ArrayList<>();
				try {
					Status status = TwitterObjectFactory.createStatus(input);
					Object[] objects = new Object[] { status.getId(),
							StringUtils.replaceChars(status.getText(), "\n", " "),
							StringUtils.replaceChars(status.getSource(), "\n", " "), status.isRetweeted(),
							StringUtils.replaceChars(status.getUser().getName(), "\n", " "),
							status.getCreatedAt().toString(), status.getRetweetCount(),
							StringUtils.replaceChars(status.getUser().getLocation(), "\n", " "),
							status.getInReplyToUserId(), status.getInReplyToStatusId(),
							StringUtils.replaceChars(status.getUser().getScreenName(), "\n", " "),
							StringUtils.replaceChars(status.getUser().getDescription(), "\n", " "),
							status.getUser().getFriendsCount(), status.getUser().getStatusesCount(),
							status.getUser().getFollowersCount() };
					for (Object object : objects) {
						data.add(String.valueOf(object));
					}
				} catch (Exception e) {
					System.out.println("Exception occurred while converting raw json to status");
				}
				dataPayload.setPayload(data);
				return dataPayload;
			}

		});
		return dataPayLoadDStream;
	}
}