/* This class contains the method to write the data to Amazon S3 */
package com.xavient.dataingest.spark.s3ingestion;

import org.apache.spark.streaming.api.java.JavaDStream;

import com.xavient.dataingest.redshift.RedshiftDataCopier;
import com.xavient.dataingest.spark.constants.Constants;
import com.xavient.dataingest.spark.util.AppArgs;
import com.xavient.dataingest.spark.util.DataPayload;

public class SparkS3Ingestor {
	@SuppressWarnings("deprecation")
	public static void s3dataWriter(JavaDStream<DataPayload> dp, AppArgs appArgs) {

		dp.foreachRDD(rdd -> {
			try {

				String fileName = appArgs.getProperty(Constants.S3_OUTPUT_PATH) + System.currentTimeMillis();

				rdd.coalesce(1, true).saveAsTextFile(fileName);

				RedshiftDataCopier.redshiftDataCopier(fileName);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
