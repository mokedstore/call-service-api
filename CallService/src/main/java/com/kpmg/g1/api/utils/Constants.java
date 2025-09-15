package com.kpmg.g1.api.utils;

public class Constants {
	public final static String CONFIGURATION_DIR_PATH = "CALL_SERVICE_CONFIGURATION_DIR_PATH";
	public final static String CONFIGURATION_FILE_PATH = "CALL_SERVICE_CONFIGURATION_FILE_PATH";
	public final static String IS_ALERT_FETCHER = "IS_ALERT_FETCHER";
	public final static long KILL_THREADS_WAIT_TIME_IN_MILLIS = 3000l;
	
	public final static String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public final static String G1_TIMESTAMP_PATTERN_IDT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
	
	public final static String SQL_COLUMN_KID = "kId";
	
	public final static String NEW_ALERT_INIT_STATUS_NEW = "new";
	public final static String NEW_ALERT_INIT_STATUS_DUPLICATE = "duplicate";
	public final static String NEW_ALERT_INIT_STATUS_DUPLICATE_IN_PROGRESS = "duplicateInProgress";
	
	public final static String LOG_LEVEL_INFO = "info";
	public final static String LOG_LEVEL_ERROR = "error";
	
	public final static String WRITE_EVENT_CODE_DUPLICATE = "01";
	public final static String WRITE_EVENT_CODE_NEW_ACK = "+44";
	
	public final static String FULL_CLEAR_FLAG_NO = "N";
	public final static String FULL_CLEAR_FLAG_YES = "Y";
	
	public final static String VALID_ALERT_STATUS_CODE = "0";
	public final static String DUPLICATE_ALERT_STATUS_CODE = "2";
	public final static String FAILED_TO_CREATE_AUDIO_FILE_STATUS_CODE = "3";
	public final static String FAILED_TO_START_VONAGE_CALL_STATUS_CODE = "4";
	public final static String NO_MATCHING_MESSAGE_ID_STATUS_CODE = "6";
	public final static String GENERAL_G1_RUNTIME_ERROR = "15";
	
	public final static String VONAGE_WAITING_FOR_CUSTOMER_RESPONSE = "waitingCustomerResponse";
	
	public final static String ALERT_COLUMN_KID = "kId";
	public final static String ALERT_COLUMN_CREATED_AT = "createdAt";
	public final static String ALERT_COLUMN_UPDATED_AT = "updatedAt";
	public final static String ALERT_COLUMN_SITE_NUMBER = "siteNumber";
	public final static String ALERT_COLUMN_SYSTEM_NUMBER = "systemNumber";
	public final static String ALERT_COLUMN_ALARM_INCIDENT_NUMBER = "alarmIncidentNumber";
	public final static String ALERT_COLUMN_DISPATCH_LOCATION = "dispatchLocation";
	public final static String ALERT_COLUMN_ALARM_EVENT_ID = "alarmEventId";
	public final static String ALERT_COLUMN_CURRENT_WRITE_EVENT_CODE = "currentWriteEventCode";
	public final static String ALERT_COLUMN_FULL_CLEAR_STATUS = "fullClearStatus";
	public final static String ALERT_COLUMN_IS_ACTIVE_ALERT = "isActiveAlert";
	public final static String ALERT_COLUMN_ALERT_HANDLING_STATUS_CODE = "alertHandlingStatusCode";
	public final static String ALERT_COLUMN_ALERT_HANDLING_STATUS_MESSAGE = "alertHandlingStatusMessage";
	public final static String ALERT_COLUMN_PROGRESS_MESSAGES = "progressMessages";
	public final static String ALERT_COLUMN_CONTACTS = "contacts";
	public final static String ALERT_COLUMN_CALL_GENERATED_TEXT = "callGeneratedText";
	public final static String ALERT_COLUMN_TEXT_SPEECH_FILE_LOCATION = "textToSpeechFileLocation";
	public final static String ALERT_COLUMN_VONAGE_CURRENT_CONVERSATION_ID = "vonageCurrentConversationId";
	public final static String ALERT_COLUMN_ANSWERED_PHONE_NUMBER = "answeredPhoneNumber";
	public final static String ALERT_COLUMN_ORDER_OF_ANSWERED_CALL = "orderOfAnsweredCall";
	public final static String ALERT_COLUMN_VONAGE_CONVERSATION_LENGTH = "vonageConversationLength";
	public final static String ALERT_COLUMN_CUSTOMER_RESPONSE_TO_CALL = "customerResponseToCall";
	public final static String ALERT_COLUMN_ALERT_DATE = "alertDate";
	public final static String ALERT_COLUMN_ALERT_ZONE_ID = "alertZoneId";
	
	public final static String TEXT_TO_SPEECH_COLUMN_EVENT_ID = "customerResponseToCall";
	public final static String TEXT_TO_SPEECH_COLUMN = "messageId";
	
	public final static String GET_KID_BY_VONAGE_UUID = "SELECT kId FROM Alerts WHERE vonageCurrentConversationId = ?";
	public final static String GET_OPEN_ALERTS_BY_SITE_NO_PREFIX = "SELECT * FROM Alerts WHERE siteNumber IN (";
	public final static String GET_OPEN_ALERTS_BY_SITE_NO_SUFFIX = ") AND isActiveAlert=1";
	
	public final static String GET_MESSAGE_ID_BY_EVENT_ID = "SELECT messageId FROM TextToSpeechMessages WHERE eventId = ?";
	
	public static String SQL_QUERY_UPSERT_ALERT = "IF NOT EXISTS (SELECT * FROM Alerts WHERE alarmIncidentNumber = ?) " +
			" INSERT INTO Alerts (kId, createdAt, updatedAt, siteNumber, systemNumber, alarmIncidentNumber, dispatchLocation, alarmEventId, currentWriteEventCode,"
			+ " fullClearStatus, isActiveAlert, alertHandlingStatusCode, alertHandlingStatusMessage, progressMessages, contacts, callGeneratedText, textToSpeechFileLocation,"
			+ " vonageCurrentConversationId, answeredPhoneNumber, orderOfAnsweredCall, vonageConversationLength, customerResponseToCall, alertDate, alertZoneId) "
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
			+ " ELSE UPDATE Alerts SET kId=?, createdAt=?, updatedAt=?, siteNumber=?, systemNumber=?, alarmIncidentNumber=?, dispatchLocation=?, alarmEventId=?,"
			+ " currentWriteEventCode=?, fullClearStatus=?, isActiveAlert=?, alertHandlingStatusCode=?, alertHandlingStatusMessage=?, progressMessages=?, contacts=?,"
			+ " callGeneratedText=?, textToSpeechFileLocation=?, vonageCurrentConversationId=?, answeredPhoneNumber=?, orderOfAnsweredCall=?, vonageConversationLength=?,"
			+ " customerResponseToCall=?, alertDate=?, alertZoneId=? WHERE alarmIncidentNumber=?";
	
	public static String SQL_QUERY_INSERT_CONVERSATION_RECORD = "INSERT INTO Conversations (conversationId, uuid, fromNo, toNo, eventTimestamp, disconnectedBy, "
			+ "duration, rate, price, startTime, endTime, rawEvent, kId, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static int VONAGE_GENERATE_TOKEN_MAX_ATTEMPTS = 3;
	
	public static String ISRAEL_COUNTRY_CODE = "972";
	
	
	public static String VONAGE_START_CALL_REQUEST_BODY =  "{\n"
			+ "	\"event_url\": [\n"
			+ "        \"$vonageAnswerUrl$\"\n"
			+ "    ],\n"
			+ "    \"answer_method\": \"POST\",\n"
			+ "    \"ncco\": [\n"
			+ "        {\n"
			+ "            \"streamUrl\": [\n"
			+ "                \"$vonageStreamUrl$\"\n"
			+ "            ],\n"
			+ "            \"activeAction\": true,\n"
			+ "            \"level\": 0,\n"
			+ "            \"loop\": 1,\n"
			+ "            \"action\": \"stream\",\n"
			+ "            \"bargeIn\": true\n"
			+ "        },\n"
			+ "        {\n"
			+ "            \"action\": \"input\",\n"
			+ "            \"dtmf\": {\n"
			+ "                \"maxDigits\": 1,\n"
			+ "                \"submitOnHash\": \"true\",\n"
			+ "                \"timeout\": 5\n"
			+ "            },\n"
			+ "            \"eventUrl\": [\n"
			+ "                \"$vonageEventUrl$\"\n"
			+ "            ],\n"
			+ "            \"eventMethod\": \"POST\"\n"
			+ "        }\n"
			+ "    ],\n"
			+ "    \"to\": [\n"
			+ "        {\n"
			+ "            \"type\": \"phone\",\n"
			+ "            \"number\": \"$vonageToPhoneNumber$\"\n"
			+ "        }\n"
			+ "    ],\n"
			+ "    \"random_from_number\": true,\n"
			+ "    \"machine_detection\": \"hangup\",\n"
			+ "    \"ringing_timer\": $vonageMaxRingTime$\n"
			+ "}";
}
