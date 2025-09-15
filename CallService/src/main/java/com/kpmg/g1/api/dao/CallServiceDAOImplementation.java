package com.kpmg.g1.api.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.objects.model.Conversation;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.JSONConfigurations;
import com.kpmg.g1.api.utils.Utils;

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
	
	public static String getMessageIdByEventIdForTextToSpeech(String eventId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		try {
			connection = basicDS.getConnection();
			ps = connection.prepareStatement(Constants.GET_MESSAGE_ID_BY_EVENT_ID);
			ps.setString(1, eventId);
			res = ps.executeQuery();

			if (!res.isBeforeFirst()) {
				return null;
			}
			res.next();
			return res.getString(Constants.TEXT_TO_SPEECH_COLUMN);

		} catch (SQLException e) {
			log.error("Error while trying to get messageId from alert event id " + eventId + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			closeResources(connection, ps, res);
		}
	}
	
	public static List<Alert> getOpenAlertsBySiteNumbers(Set<String> siteNumbers) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet res = null;
		List<Alert> openAlerts = new ArrayList<Alert>();
		try {
			connection = basicDS.getConnection();
			// build Prepared statement dynamically
			String queryPlaceHolders = String.join(",", Collections.nCopies(siteNumbers.size(), "?"));
			String sqlPreparedStatementString = Constants.GET_OPEN_ALERTS_BY_SITE_NO_PREFIX + queryPlaceHolders + Constants.GET_OPEN_ALERTS_BY_SITE_NO_SUFFIX;
			ps = connection.prepareStatement(sqlPreparedStatementString);
			Iterator<String> siteNumberIterator = siteNumbers.iterator();
			int preparedStatementArgumentsCounter = 0;
			while(siteNumberIterator.hasNext()) {
				preparedStatementArgumentsCounter++;
				String currentSiteNumber = siteNumberIterator.next();
				ps.setString(preparedStatementArgumentsCounter, currentSiteNumber);
			}
			res = ps.executeQuery();
			// check if there are any results found and if there are not return empty string
			if(!res.isBeforeFirst()) {
				return openAlerts;
			}
			
			// loop over results
			while(res.next()) {
				Alert openAlert = buildAlertObjectFromSQLResult(res);
				if (openAlert != null) {
					openAlerts.add(openAlert);
				}
			}
			return openAlerts;
		} catch (SQLException e) {
			log.error("Error occured while trying to get Open alerts for site numbers " + siteNumbers + " Error: " + ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			closeResources(connection, ps, res);
		}
	}
	
	public static String upsertAlert(Alert alert) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = basicDS.getConnection();
			ps = connection.prepareStatement(Constants.SQL_QUERY_UPSERT_ALERT);
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			Timestamp createdAtTs = new Timestamp(Utils.getDateFromString(alert.getCreatedAt(), Constants.TIMESTAMP_PATTERN).getTime());
			Timestamp updatedAtTs = new Timestamp(Utils.getDateFromString(alert.getUpdatedAt(), Constants.TIMESTAMP_PATTERN).getTime());
			Timestamp alertDate = new Timestamp(Utils.getDateFromString(alert.getAlertDate(), Constants.TIMESTAMP_PATTERN).getTime());
			ps.setString(1, alert.getAlarmIncidentNumber());
			ps.setString(2, alert.getkId());
			ps.setTimestamp(3, createdAtTs);
			ps.setTimestamp(4, updatedAtTs);
			ps.setString(5, alert.getSiteNumber());
			ps.setString(6, alert.getSystemNumber());
			ps.setString(7, alert.getAlarmIncidentNumber());
			ps.setString(8, alert.getDispatchLocation());
			ps.setString(9, alert.getAlarmEventId());
			ps.setString(10, alert.getCurrentWriteEventCode());
			ps.setString(11, alert.getFullClearStatus());
			ps.setBoolean(12, alert.isActiveAlert());
			ps.setString(13, alert.getAlertHandlingStatusCode());
			ps.setString(14, alert.getAlertHandlingStatusMessage());
			ps.setString(15, alert.getProgressMessages());
			ps.setString(16, alert.getContacts());
			ps.setString(17, alert.getCallGeneratedText());
			ps.setString(18, alert.getTextToSpeechFileLocation());
			ps.setString(19, alert.getVonageCurrentConversationId());
			ps.setString(20, alert.getAnsweredPhoneNumber());
			ps.setInt(21, alert.getOrderOfAnsweredCall());
			ps.setInt(22, alert.getVonageConversationLength());
			ps.setString(23, alert.getCustomerResponseToCall());
			ps.setTimestamp(24, alertDate);
			ps.setString(25, alert.getAlertZoneId());
			ps.setString(26, alert.getkId());
			ps.setTimestamp(27, createdAtTs);
			ps.setTimestamp(28, updatedAtTs);
			ps.setString(29, alert.getSiteNumber());
			ps.setString(30, alert.getSystemNumber());
			ps.setString(31, alert.getAlarmIncidentNumber());
			ps.setString(32, alert.getDispatchLocation());
			ps.setString(33, alert.getAlarmEventId());
			ps.setString(34, alert.getCurrentWriteEventCode());
			ps.setString(35, alert.getFullClearStatus());
			ps.setBoolean(36, alert.isActiveAlert());
			ps.setString(37, alert.getAlertHandlingStatusCode());
			ps.setString(38, alert.getAlertHandlingStatusMessage());
			ps.setString(39, alert.getProgressMessages());
			ps.setString(40, alert.getContacts());
			ps.setString(41, alert.getCallGeneratedText());
			ps.setString(42, alert.getTextToSpeechFileLocation());
			ps.setString(43, alert.getVonageCurrentConversationId());
			ps.setString(44, alert.getAnsweredPhoneNumber());
			ps.setInt(45, alert.getOrderOfAnsweredCall());
			ps.setInt(46, alert.getVonageConversationLength());
			ps.setString(47, alert.getCustomerResponseToCall());
			ps.setTimestamp(48, alertDate);
			ps.setString(49, alert.getAlertZoneId());
			ps.setString(50, alert.getAlarmIncidentNumber());
			
			ps.executeUpdate();
			return "success";
		} catch (Exception e) {
			log.error("Error occurred while trying to upsert Alert event of alarm incident number: " + alert.getAlarmIncidentNumber()
				+ " Alert: " + alert.toString() + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			closeResources(connection, ps, null);
		}
	}
	
	public static String insertConversation(Conversation conversation) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = basicDS.getConnection();
			ps = connection.prepareStatement(Constants.SQL_QUERY_INSERT_CONVERSATION_RECORD);
			ps.setString(1, conversation.getConversationId());
			ps.setString(2, conversation.getUuid());
			ps.setString(3, conversation.getFromNo());
			ps.setString(4, conversation.getToNo());
			Timestamp eventTimestamp = new Timestamp(Utils.getDateFromString(conversation.getEventTimestamp(), Constants.TIMESTAMP_PATTERN).getTime());
			ps.setTimestamp(5, eventTimestamp);
			ps.setString(6, conversation.getDisconnectedBy());
			ps.setInt(7, conversation.getDuration());
			BigDecimal rate = new BigDecimal(String.valueOf(conversation.getRate()));
			ps.setBigDecimal(8, rate);
			BigDecimal price = new BigDecimal(String.valueOf(conversation.getPrice()));
			ps.setBigDecimal(9, price);
			if (conversation.getStartTime() == null) {
				ps.setNull(10, java.sql.Types.TIMESTAMP);
			} else {
				Timestamp startTimeTimestamp = new Timestamp(Utils.getDateFromString(conversation.getStartTime(), Constants.TIMESTAMP_PATTERN).getTime());
				ps.setTimestamp(10, startTimeTimestamp);
			}
			if (conversation.getEndTime() == null) {
				ps.setNull(11, java.sql.Types.TIMESTAMP);
			} else {
				Timestamp endTimeTimestamp = new Timestamp(Utils.getDateFromString(conversation.getEndTime(), Constants.TIMESTAMP_PATTERN).getTime());
				ps.setTimestamp(11, endTimeTimestamp);
			}
			ps.setString(12, conversation.getRawEvent());
			ps.setString(13, conversation.getkId());
			ps.setString(14, conversation.getStatus());
			
			ps.executeUpdate();
			return "success";
		} catch (Exception e) {
			log.error("Error occurred while trying to upsert Converstaion event: " + conversation.toString() + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			closeResources(connection, ps, null);
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
	
	private static Alert buildAlertObjectFromSQLResult(ResultSet res) {
		try {
			String kId = res.getString(Constants.ALERT_COLUMN_KID);
			Timestamp createdAtTs = res.getTimestamp(Constants.ALERT_COLUMN_CREATED_AT);
			Date createdAtDate = createdAtTs;
			String createdAt = Utils.getTimestampFromDate(createdAtDate);
			Timestamp updatedAtTs = res.getTimestamp(Constants.ALERT_COLUMN_UPDATED_AT);
			Date updatedAtDate = updatedAtTs;
			String updatedAt = Utils.getTimestampFromDate(updatedAtDate);
			String siteNumber = res.getString(Constants.ALERT_COLUMN_SITE_NUMBER);
			String systemNumber  = res.getString(Constants.ALERT_COLUMN_SYSTEM_NUMBER);
			String alarmIncidentNumber  = res.getString(Constants.ALERT_COLUMN_ALARM_INCIDENT_NUMBER);
			String dispatchLocation  = res.getString(Constants.ALERT_COLUMN_DISPATCH_LOCATION);
			String alarmEventId  = res.getString(Constants.ALERT_COLUMN_ALARM_EVENT_ID);
			String currentWriteEventCode  = res.getString(Constants.ALERT_COLUMN_CURRENT_WRITE_EVENT_CODE);
			String fullClearStatus  = res.getString(Constants.ALERT_COLUMN_FULL_CLEAR_STATUS);
			boolean isActiveAlert = res.getBoolean(Constants.ALERT_COLUMN_IS_ACTIVE_ALERT);
			String alertHandlingStatusCode = res.getString(Constants.ALERT_COLUMN_ALERT_HANDLING_STATUS_CODE);
			String alertHandlingStatusMessage = res.getString(Constants.ALERT_COLUMN_ALERT_HANDLING_STATUS_MESSAGE);
			String progressMessages = res.getString(Constants.ALERT_COLUMN_PROGRESS_MESSAGES);
			String contacts = res.getString(Constants.ALERT_COLUMN_CONTACTS);
			String callGeneratedText = res.getString(Constants.ALERT_COLUMN_CALL_GENERATED_TEXT);
			String textToSpeechFileLocation = res.getString(Constants.ALERT_COLUMN_TEXT_SPEECH_FILE_LOCATION);
			String vonageCurrentConversationId = res.getString(Constants.ALERT_COLUMN_VONAGE_CURRENT_CONVERSATION_ID);
			String answeredPhoneNumber = res.getString(Constants.ALERT_COLUMN_ANSWERED_PHONE_NUMBER);
			int orderOfAnsweredCall = res.getInt(Constants.ALERT_COLUMN_ORDER_OF_ANSWERED_CALL);
			int vonageConversationLength = res.getInt(Constants.ALERT_COLUMN_VONAGE_CONVERSATION_LENGTH);
			String customerResponseToCall = res.getString(Constants.ALERT_COLUMN_CUSTOMER_RESPONSE_TO_CALL);
			Timestamp alertDateTs = res.getTimestamp(Constants.ALERT_COLUMN_ALERT_DATE);
			Date alertDateDate = alertDateTs;
			String alertDate = Utils.getTimestampFromDate(alertDateDate);
			String alertZoneId  = res.getString(Constants.ALERT_COLUMN_ALERT_ZONE_ID);
			return new Alert(kId, createdAt, updatedAt, siteNumber, systemNumber, alarmIncidentNumber, dispatchLocation, alarmEventId, currentWriteEventCode, fullClearStatus, isActiveAlert,
					alertHandlingStatusCode, alertHandlingStatusMessage, progressMessages, contacts, callGeneratedText, textToSpeechFileLocation,
					vonageCurrentConversationId, answeredPhoneNumber, orderOfAnsweredCall, vonageConversationLength, customerResponseToCall, alertDate, alertZoneId);
		} catch (Exception e) {
			log.error("Error occurred while trying to build alert object from result set. Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}

}
