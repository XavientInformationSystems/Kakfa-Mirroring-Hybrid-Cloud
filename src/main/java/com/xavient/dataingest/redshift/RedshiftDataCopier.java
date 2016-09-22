/* This class is used to copy data to Redshift Cluster Programmatically from Amazon S3*/

package com.xavient.dataingest.redshift;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class RedshiftDataCopier {

	static InputStream input = null;
	static Properties prop = new Properties(); 

	public static void redshiftDataCopier(String fileName) {	
		Connection conn = null;
		Statement stmt = null;

		try {
			fileName = fileName.replaceFirst("s3n", "s3");
			String file = fileName +"/part-00000";
			Class.forName("com.amazon.redshift.jdbc41.Driver");


			System.out.println("Connecting to database...");
			Properties props = new Properties();

			input = new FileInputStream("src/main/resources/config.properties");

			prop.load(input);

	
			props.setProperty("user", prop.getProperty("MasterUsername"));
			props.setProperty("password", prop.getProperty("MasterUserPassword"));
			conn = DriverManager.getConnection(prop.getProperty("dbURL"), props);

			stmt = conn.createStatement();
			String sql;

			sql = "copy " + prop.getProperty("audienceDataTableRedshift") + " " + "from '"
					+ file +"'"+ " " + "credentials 'aws_access_key_id="
					+ prop.getProperty("aws_access_key_id") + ";aws_secret_access_key="
					+ prop.getProperty("aws_secret_access_key") + "' delimiter '|' compupdate on";

			int j = stmt.executeUpdate(sql);

			stmt.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception ex) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Finished Copying Data");

	}

}