package com.kpmg.g1.api.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.JSONConfigurations;

public class CallServiceDAOImplementation {

	final static Logger log = LogManager.getLogger(CallServiceDAOImplementation.class.getName());

	private static BasicDataSource basicDS = getDataSource();

	// load sql driver bor database integration
	private static void loadDriverByName(String driverName) {
		try {
			Class.forName(driverName);
		} catch (Exception ex) {
			log.error(String.format("error occured when trying to load driver: %s. \n cause: %s \n message: %s",
					driverName, ex.getCause(), ex.getMessage()));
		}
	}

	private static String extractSQLUsername() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("sql").getString("username");
		} catch (Exception e) {
			log.error("Failed to extract sql username: " + e.getMessage());
			throw new RuntimeException();
		}
	}

	private static String extractSQLUrl() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("sql").getString("url");
		} catch (Exception e) {
			log.error("Failed to extract sql URL: " + e.getMessage());
			throw new RuntimeException();
		}
	}

	private static String extractSQLConnectionProperties() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("sql")
					.getString("connectionProperties");
		} catch (Exception e) {
			log.error("Failed to extract sql connectionProperties: " + e.getMessage());
			throw new RuntimeException();
		}
	}

	// extract SQL password according to the running environment
	private static String extractSQLPassword() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("sql").getString("password");
		} catch (Exception e) {
			log.error("Failed to extract sql password: " + e.getMessage());
			throw new RuntimeException();
		}
	}

	private static BasicDataSource getDataSource() {
		// get connection details from configuration file
		String url = extractSQLUrl();
		String username = extractSQLUsername();
		String password = extractSQLPassword();
		String connectionProperties = extractSQLConnectionProperties();
		// Load SQLServer driver to memory
		loadDriverByName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		BasicDataSource ds = new BasicDataSource();
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setConnectionProperties(connectionProperties);
		return ds;
	}

	public static String getKidByVonageUUID(String uuid) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		try {
			connection = basicDS.getConnection();
			ps = connection.prepareStatement(Constants.GET_KID_BY_VONAGE_UUID);
			ps.setString(1, uuid);
			res = ps.executeQuery();

			if (!res.isBeforeFirst()) {
				return "";
			}
			res.next();
			return res.getString(Constants.SQL_COLUMN_KID);

		} catch (SQLException e) {
			log.error("Error while trying to get kId from vonage UUID " + uuid + " Error: " + ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			closeResources(connection, ps, res);
		}
	}

	// close sql resources
	private static void closeResources(Connection connection, PreparedStatement ps, ResultSet res) {
		try {
			if (res != null) {
				res.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			log.error("Error occured while trying to close sql resources after SQL query: " + ExceptionUtils.getStackTrace(e));
		}
	}

}
