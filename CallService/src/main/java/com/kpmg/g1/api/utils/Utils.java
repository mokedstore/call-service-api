package com.kpmg.g1.api.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

import com.kpmg.g1.api.cache.ConversationsUUIDCache;
import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.objects.model.Conversation;

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
	
	public static Date getOlderDateByMinutes(Date date, int minutes) {
		try {
			Instant newInstant = date.toInstant().minus(30, ChronoUnit.MINUTES);
	        Date newDate = Date.from(newInstant);
	        return newDate;
		} catch (Exception e) {
			log.error("getOlderDateByMinutes: failed to reduce " + String.valueOf(minutes) + " from date: " + date.toString() + " . Error: " + ExceptionUtils.getStackTrace(e));
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
			log.error("getDateFromString: Failed to convert formattedDate: " + formattedDate + " Using format: "+ format + "Error: " + ExceptionUtils.getStackTrace(e));
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
			String csNumber =  alertObject.getString("cs_no");
			return new Alert(kId, createdAt, updatedAt, siteNumber, systemNumber, alarmIncidentNumber, dispatchLocation, alaramEventId, currentWriteEventCode, fullClearStatus,
					isActiveAlert, alertHandlingStatusCode, alertHandlingStatusMessage, progressMessages, contacts, callGeneratedText, textToSpeechFileLocation,
					vonageCurrentConversationId, answeredPhoneNumber, orderOfAnsweredCall, vonageConversationLength, customerResponseToCall, alertDate, alertZoneId, csNumber);
		} catch (Exception e) {
			log.error("Failed to build alert object from open alert data with object: " + alertObject.toString() + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static Conversation buildConversationObjectFromVonageEvent(JSONObject vonageEventObject) {
		try {
			String conversationId = vonageEventObject.optString("conversation_uuid", "");
			String uuid = vonageEventObject.optString("uuid", vonageEventObject.optString("call_uuid", ""));
			// in case uuid is empty try to get it via conversation id
			if ((uuid.isEmpty()) && (!conversationId.isEmpty()))  {
				uuid = CallServiceDAOImplementation.getVonageUuidByConversationId(conversationId);
				if (uuid == null) {
					uuid = "";
				}
			}
			String fromNumber = vonageEventObject.optString("from", "");
			String toNumber = vonageEventObject.optString("to", "");
			String eventTimestamp = vonageEventObject.getString("timestamp");
			// sometimes timestamp arrives with high granularity for sub seconds so normalize to milliseconds
			if (eventTimestamp.length() > Constants.ISO_DATE_FORMAT_EXPECTED_LENGTH) {
				eventTimestamp = eventTimestamp.substring(0,Constants.ISO_DATE_FORMAT_EXPECTED_LENGTH -1) + "Z";
			}
			String disconnectedBy = (vonageEventObject.has("disconnected_by")) && (!vonageEventObject.isNull("disconnected_by")) ?  vonageEventObject.getString("disconnected_by") : "";
			int duration = Integer.parseInt(vonageEventObject.optString("duration", "0"));
			double rate = Double.parseDouble(vonageEventObject.optString("rate", "0.0"));
			double price = Double.parseDouble(vonageEventObject.optString("price", "0.0"));
			String rawEvent = vonageEventObject.toString();
			String startTime = (vonageEventObject.has("start_time")) && (!vonageEventObject.isNull("start_time")) ?  vonageEventObject.getString("start_time") : null;
			String endTime = (vonageEventObject.has("end_time")) && (!vonageEventObject.isNull("end_time")) ?  vonageEventObject.getString("end_time") : null;
			String status = vonageEventObject.optString("status", "");
			String kid = null;
			if (ConversationsUUIDCache.getInstance().getConversationToKidCache().containsKey(uuid)) {
				kid = ConversationsUUIDCache.getInstance().getConversationToKidCache().get(uuid);
			} else {
				kid = CallServiceDAOImplementation.getKidByVonageUUID(uuid);
				if (kid== null) {
					kid = "";
				} else {
					ConversationsUUIDCache.getInstance().addToCache(uuid, kid);
				}
			}
			return new Conversation(conversationId, uuid, fromNumber, toNumber, eventTimestamp, disconnectedBy, duration, rate, price, rawEvent, startTime, endTime, kid, status);
		} catch (Exception e) {
			log.error("Failed to build conversation object from vonage event data with object: " + vonageEventObject.toString() + " Error: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONArray getOpenAlerts() {
		try {
			// build url and create client
			String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
					+ "/api/alarms";
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
	
	public static JSONObject updateEvent(String systemNumber, String alarmIncidentNumber, String alarmEventId, String alarmFullClearFlag, String comment, String fcAllSystems) {
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
			.put("full_clear_flag", alarmFullClearFlag).put("emp_no", JSONConfigurations.getInstance().getConfigurations().getInt("g1EventsApiEmpNo"))
			.put("user_name", JSONConfigurations.getInstance().getConfigurations().getString("g1EventsApiUserName")).put("additional_info", "")
			.put("test_seqno", 0).put("phone", "").put("scheduled_date", Utils.getTimestampFromDate(null)).put("alarminc_call_seqno", 0).put("aux2", "")
			.put("fc_all_systems", Constants.FULL_CLEAR_FLAG_NO);
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
	
	public static JSONObject sendSmsDirect(int siteNumber, String phoneNumber, String csNumber, String subject, String message) {
		// build url using kid
		String url = JSONConfigurations.getInstance().getConfigurations().getString("g1ServicesBaseUrl")
				+ "/api/send-sms-direct";

		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		CloseableHttpResponse response = null;

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		
		JSONObject requsetDataObject = new JSONObject();
		requsetDataObject.put("phone_number", phoneNumber).put("site_no", siteNumber).put("cs_no", csNumber).put("subject", subject)
		.put("message", message);
		
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
			log.error("Received Exception when trying to call send direct sms API value - site number: " + String.valueOf(siteNumber) + ", phone number: " + phoneNumber
					+ " cs number: " + csNumber + " subject: " + subject + " message: " + message
					+ " .Error: " + ExceptionUtils.getStackTrace(e));
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
	
	public static JSONObject vonageStartCall(String toNumber, String pathToSpeechFile) {
		int startCallsAttempts = 0;
		while (startCallsAttempts < Constants.VONAGE_GENERATE_TOKEN_MAX_ATTEMPTS) {
			startCallsAttempts++;
			VonageToken vonageToken = VonageToken.getInstance();
			if (vonageToken == null || vonageToken.getTokenValue() == null) {
				continue;
			}
			String url = null;
			try {
				url = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("callsUrl");
			} catch (Exception e) {
				log.error("vonageStartCall: Failed to get vonage calls URL: " + ExceptionUtils.getStackTrace(e));
				return null;
			}
			
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			CloseableHttpResponse response = null;

			post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "application/json");
			post.setHeader("Authorization", "Bearer " + vonageToken.getTokenValue());
			
			try {
				// get variables to inject to Vonage body
				JSONObject vonageObject = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage");
				String vonageAnswerUrl = vonageObject.getString("answerUrlEndpoint");
				Path pathToSpeechFileObject = Paths.get(pathToSpeechFile);
		        String fileName = pathToSpeechFileObject.getFileName().toString();
				String streamMessageUrl = vonageObject.getString("streamUrlEndpoint") + "/" + fileName;
				String eventUrl = vonageObject.getString("eventUrlEndpoint");
				String maxRingTime = String.valueOf(vonageObject.getInt("maxRingTimeSeconds"));
				String clientResponseTimeout =  String.valueOf(vonageObject.getInt("clientResponseTimeoutSeconds"));
				String phoneNumberToCall = Constants.ISRAEL_COUNTRY_CODE + toNumber.substring(1);
				
				
				String startCallRequestStr = Constants.VONAGE_START_CALL_REQUEST_BODY.replace("$vonageAnswerUrl$", vonageAnswerUrl).replace("$vonageStreamUrl$", streamMessageUrl)
						.replace("$vonageEventUrl$", eventUrl).replace("$vonageToPhoneNumber$", phoneNumberToCall).replace("$vonageMaxRingTime$", maxRingTime)
						.replace("$clientResponseTimeout$", clientResponseTimeout);
				StringEntity input = new StringEntity(startCallRequestStr, "UTF-8");
				input.setContentType("application/json");
				post.setEntity(input);
				
				response = client.execute(post);
				String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
				// if response is valid return
				if (response.getStatusLine().getStatusCode() == 201) {
					return new JSONObject(responseData);
				} else if (response.getStatusLine().getStatusCode() == 401) {
					log.error("Received Unexpected status 401 when trying to start call with values - toNumber: " + toNumber + ", pathToSpeechFile: " +  pathToSpeechFile
							+ " data: " + responseData + ". trying to renew token");
					VonageToken.manuallyRenewToken();
				} else {
					log.error("Received Unexpected status " + response.getStatusLine().getStatusCode()
							+ " when trying to start call with values - toNumber: " + toNumber + ", pathToSpeechFile: " +  pathToSpeechFile
							+ " data: " + responseData);
				}

			} catch (Exception e) {
				log.error("Received Exception when trying to start call with values - toNumber: " + toNumber + ", pathToSpeechFile: " +  pathToSpeechFile
						+ " .Error: " + ExceptionUtils.getStackTrace(e));
			}
		}
		return null;
	}
	
	// allTimeDispatch is set until G1 will handle dispatch logic on their side 
	public static boolean allowTransferToDispatchAllAlertsTime(String alertCode, boolean allTimeDispatch) {
		try {
			if (allTimeDispatch) {
				return true;
			}
			// check if given alert code is part of codes that are allowed to be passed to dispatch at any time
			JSONArray alertCodesAlwaysAllowPass = JSONConfigurations.getInstance().getConfigurations().getJSONObject("dispatchTransfer").getJSONArray("alertCodesAlwaysAllowPass");
			for (int i = 0; i < alertCodesAlwaysAllowPass.length(); i++) {
				String currentAlertCode = alertCodesAlwaysAllowPass.getString(i);
				if (currentAlertCode.equals(alertCode)) {
					return true;
				}
			}
			// check day and time of day to determine if call can be passed to dispatch regardless of its type
			JSONObject daysConf = JSONConfigurations.getInstance().getConfigurations().getJSONObject("dispatchTransfer").getJSONObject("days");
			LocalDateTime now = LocalDateTime.now();
			String dayOfWeek = String.valueOf(now.getDayOfWeek().getValue());
			int hour = now.getHour();
			JSONArray specificDayConf = daysConf.getJSONArray(dayOfWeek);
			for (int i = 0; i < specificDayConf.length(); i++) {
				JSONObject currentDayConf = specificDayConf.getJSONObject(i);
				int gte = currentDayConf.getInt("gte");
				int lt = currentDayConf.getInt("lt");
				if (hour >= gte && hour < lt) {
					return true;
				}
			}
			
		} catch (Exception e) {
			log.error("allowTransferToDispatchAllAlertsTime: Error occurred while trying to check if should allow to pass call to dispatch with alertCode: " + alertCode +
					" Error: " + ExceptionUtils.getStackTrace(e));
			return false;
		}
		return false;
	}

}
