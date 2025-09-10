package com.kpmg.g1.api.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.objects.model.Alert;

public class Utils {
	
	final static Logger log = LogManager.getLogger(Utils.class.getName());
	
	public static String getTimestampFromDate(Date date) {
		// create pattern in accordance with phoenix pattern
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIMESTAMP_PATTERN);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		// in case date is null then get current time
		if (date == null) {
			Instant now = Instant.now();
			date = Date.from(now);
		}
		// get current datetime
		String timestamp = sdf.format(date);
		return timestamp;
	}
	
	public static String getTimestampFromDateUsingFormat(Date date, String format) {
		// create pattern in accordance with phoenix pattern
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		// in case date is null then get current time
		if (date == null) {
			Instant now = Instant.now();
			date = Date.from(now);
		}
		// get current datetime
		String timestamp = sdf.format(date);
		return timestamp;
	}
	
	public static String getUtcTimeFromIdtTime(String idtTimeAsString) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.G1_TIMESTAMP_PATTERN_IDT);
			 LocalDateTime localDateTime = LocalDateTime.parse(idtTimeAsString, formatter);
			 ZonedDateTime jerusalemTime = localDateTime.atZone(ZoneId.of("Asia/Jerusalem"));
			// Convert to UTC instant
	        Instant instant = jerusalemTime.toInstant();

	        // Convert to java.util.Date
	        Date date = Date.from(instant);
	        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIMESTAMP_PATTERN);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			String utcTimestamp = sdf.format(date);
			return utcTimestamp;
			 
		} catch (Exception e) {
			log.error("Failed to convert G1 IDT to UTC. Error: " +  ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static Date getDateFromString(String formattedDate, String format) {
		try {
			// create pattern in accordance with phoenix pattern
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			sdf.setLenient(false);
			return sdf.parse(formattedDate);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Alert buildAlertObjectFromOpenAlertsApi(JSONObject alertObject) {
		try {
			String kId = UUID.randomUUID().toString();
			String createdAt = getTimestampFromDate(null);
			String updatedAt = getTimestampFromDate(null);
			String siteNumber = String.valueOf(alertObject.getLong("site_no"));
			String systemNumber = String.valueOf(alertObject.getLong("system_no"));
			String alarmIncidentNumber = String.valueOf(alertObject.getLong("alarminc_no"));
			String dispatchLocation = alertObject.getString("disploc_id");
			String alaramEventId = alertObject.getString("alarm_event_id").trim();
			String currentWriteEventCode = "";
			String fullClearStatus = "";
			boolean isActiveAlert = true;
			String alertHandlingStatusCode = "0";
			String alertHandlingStatusMessage = "";
			String progressMessages = "[]";
			String contacts = "[]";
			String callGeneratedText = "";
			String textToSpeechFileLocation = "";
			String vonageCurrentConversationId = "";
			String answeredPhoneNumber = "";
			int orderOfAnsweredCall = -1;
			int vonageConversationLength = -1;
			String customerResponseToCall = "";
			String alertDate = getUtcTimeFromIdtTime(alertObject.getString("alarm_date"));
			String alertZoneId = alertObject.getString("alarm_zone_id").trim();
			return new Alert(kId, createdAt, updatedAt, siteNumber, systemNumber, alarmIncidentNumber, dispatchLocation, alaramEventId, currentWriteEventCode, fullClearStatus,
					isActiveAlert, alertHandlingStatusCode, alertHandlingStatusMessage, progressMessages, contacts, callGeneratedText, textToSpeechFileLocation,
					vonageCurrentConversationId, answeredPhoneNumber, orderOfAnsweredCall, vonageConversationLength, customerResponseToCall, alertDate, alertZoneId);
		} catch (Exception e) {
			log.error("Failed to build alert object from open alert data with object: " + alertObject + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONArray getOpenAlerts() {
		try {
			// build url and create client
			String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
					+ "/api/alerts";
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse response = null;

			get.setHeader("Accept", "application/json");
			// get response from UtilsService
			response = client.execute(get);
			String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == 200) {
				return new JSONArray(responseData);
			}
			log.error("Received Unexpected status " + response.getStatusLine().getStatusCode()
					+ " when trying to get open alerts " + responseData);
			return null;
		} catch (Exception e) {
			log.error("Received Error when trying to get open alerts. Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONObject updateEvent(String systemNumber, String alarmIncidentNumber, String alarmEventId, String alarmFullClearFlag, String comment) {
		// build url using kid
		String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
				+ "/api/write-event";

		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		
		JSONObject requsetDataObject = new JSONObject();
		try {
			requsetDataObject.put("system_no", systemNumber).put("alarminc_no", alarmIncidentNumber).put("event_id", alarmEventId).put("comment", comment)
			.put("full_clear_flag", alarmFullClearFlag).put("emp_no", JSONConfigurations.getInstance().getConfigurations().getString("g1EventsApiEmpNo"))
			.put("user_name", JSONConfigurations.getInstance().getConfigurations().getString("g1EventsApiUserName")).put("additional_info", "")
			.put("test_seqno", 0).put("phone", "").put("scheduled_date", Utils.getTimestampFromDate(null)).put("alarminc_call_seqno", 0).put("aux2", "");
		} catch (Exception e) {
			log.error("Received Exception while trying to build events api request with values: systemNumber - " + systemNumber + " alarmIncidentNumber: " + alarmIncidentNumber +
					" alarmEventId: " + alarmEventId + " comment: " + comment + " alarmFullClearFlag: " + alarmFullClearFlag + " .Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
		
		
		// add request body data
		StringEntity input = new StringEntity(requsetDataObject.toString(), "UTF-8");
		input.setContentType("application/json");
		post.setEntity(input);

		try {
			// send requset
			response = client.execute(post);
			String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
			// if response is valid return
			if (response.getStatusLine().getStatusCode() == 200) {
				return new JSONObject(responseData);
			}
			throw new RuntimeException(responseData);

		} catch (Exception e) {
			log.error("Received Exception when trying to write event with value - systemNumber: " + systemNumber + " alarmIncidentNumber: " + alarmIncidentNumber +
				" alarmEventId: " + alarmEventId + " comment: " + comment + " alarmFullClearFlag: " + alarmFullClearFlag + " .Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			try {
				response.close();
				client.close();
			} catch (Exception e) {
			}
		}
	}
	
	public static JSONObject getCallList(String siteNumber, String systemNumber, String zoneId) {
		try {
			// build url and create client
			String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
					+ "/api/calllist?site_no=" + siteNumber + "&system_no=" + systemNumber + "&zone_id=" + zoneId;
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse response = null;

			get.setHeader("Accept", "application/json");
			// get response from UtilsService
			response = client.execute(get);
			String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
			if (response.getStatusLine().getStatusCode() == 200) {
				return new JSONObject(responseData);
			}
			log.error("Received Unexpected status " + response.getStatusLine().getStatusCode()
					+ " when trying to get call list with values - siteNumber: " + siteNumber + ", systemNumber: " +  systemNumber
					+ ", zoneId: " + zoneId + " data: " + responseData);
			return null;
		} catch (Exception e) {
			log.error("Received Error when trying to get call list with values - siteNumber: " + siteNumber + ", systemNumber: " +  systemNumber
					+ ", zoneId: " + zoneId + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONObject sendMessage(String siteNumber, String systemNumber, String alarmIncidentNumber, String messageId, boolean sendMessage, String phoneNumber) {
		// build url using kid
		String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
				+ "/api/send-message";

		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		
		JSONObject requsetDataObject = new JSONObject();
		requsetDataObject.put("system_no", systemNumber).put("alarminc_no", alarmIncidentNumber).put("message_id", messageId).put("site_no", siteNumber)
		.put("event_seqno", 0).put("message_address", phoneNumber).put("send_message", sendMessage);
		
		// add request body data
		StringEntity input = new StringEntity(requsetDataObject.toString(), "UTF-8");
		input.setContentType("application/json");
		post.setEntity(input);

		try {
			// send requset
			response = client.execute(post);
			String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
			// if response is valid return
			if (response.getStatusLine().getStatusCode() == 200) {
				return new JSONObject(responseData);
			}
			throw new RuntimeException(responseData);

		} catch (Exception e) {
			log.error("Received Exception when trying to call send message API value - site number: " + siteNumber + ", system number: " + systemNumber
					+ " alarm incident number: " + alarmIncidentNumber + " messageId: " + messageId + " sendMessage: " + String.valueOf(sendMessage)
					+ " phoneNumber: " + phoneNumber + " .Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			try {
				response.close();
				client.close();
			} catch (Exception e) {
			}
		}
	}
	
	public static JSONObject convertTextToSpeech(String ssml) {
		// build url using kid
		String url = JSONConfigurations.getInstance().getConfigurations().getString("callServiceBaseUrl")
				+ "/CallService/api/action/text/to/speech";

		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		
		JSONObject requsetDataObject = new JSONObject();
		requsetDataObject.put("ssml", ssml);
		
		// add request body data
		StringEntity input = new StringEntity(requsetDataObject.toString(), "UTF-8");
		input.setContentType("application/json");
		post.setEntity(input);

		try {
			// send requset
			response = client.execute(post);
			String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
			// if response is valid return
			if (response.getStatusLine().getStatusCode() == 200) {
				return new JSONObject(responseData);
			}
			throw new RuntimeException(responseData);

		} catch (Exception e) {
			log.error("Received Exception when trying to text to speech API value - ssml: " + ssml +" .Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		} finally {
			try {
				response.close();
				client.close();
			} catch (Exception e) {}
		}
	}

}
