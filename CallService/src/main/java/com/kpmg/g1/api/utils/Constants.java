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
	public final static String WRITE_EVENT_CODE_NO_ANSWER = "+9";
	public final static String WRITE_EVENT_RECEIVED_USER_ANSWER = "+23";
	public final static String WRITE_EVENT_SUCCESSFUL_SMS_SENT = "MSG121";
	
	public final static String FULL_CLEAR_FLAG_NO = "N";
	public final static String FULL_CLEAR_FLAG_YES = "Y";
	
	public final static String VALID_ALERT_STATUS_CODE = "0";
	public final static String VALID_ALERT_NO_ANSWER_STATUS_CODE = "1";
	public final static String DUPLICATE_ALERT_STATUS_CODE = "2";
	public final static String FAILED_TO_CREATE_AUDIO_FILE_STATUS_CODE = "3";
	public final static String FAILED_TO_START_VONAGE_CALL_STATUS_CODE = "4";
	public final static String NO_MATCHING_MESSAGE_ID_STATUS_CODE = "6";
	public final static String GENERAL_G1_RUNTIME_ERROR = "15";
	public final static String OPEN_FOR_TOO_LONG = "16";
	
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
	public final static String GET_DISPATCH_LOCATION_BY_VONAGE_UUID = "SELECT DISTINCT dispatchLocation FROM Alerts WHERE vonageCurrentConversationId = ?";
	public final static String GET_FILE_LOCATION_BY_VONAGE_UUID = "SELECT textToSpeechFileLocation FROM Alerts WHERE vonageCurrentConversationId = ?";
	public final static String GET_ALERT_DATA_BY_VONAGE_UUID = "SELECT * FROM Alerts WHERE vonageCurrentConversationId = ?";
	public final static String GET_OPEN_ALERTS_BY_SITE_NO_PREFIX = "SELECT * FROM Alerts WHERE siteNumber IN (";
	public final static String GET_OPEN_ALERTS_BY_SITE_NO_SUFFIX = ") AND isActiveAlert=1";
	public final static String GET_OPEN_ALERTS_OPEN_FOR_LONG_TIME = "SELECT * FROM Alerts WHERE isActiveAlert=1 and ? > updatedAt";
	
	
	public final static String NO_RESPONSE_SMS_CONTENT_MESSAGE_ID = "noAnswer";
	
	public final static String CONVERSATION_APPROVED_STATUS = "dtmf:1";
	public final static String CONVERSATION_TRANSFER_STATUS = "dtmf:2";
	public final static String CONVERSATION_NO_ANSWER_LONG_CALL_STATUS = "noAnswerLongCall";
	public final static String CONVERSATION_NO_ANSWER_STATUS = "noAnswer";
	
	public final static String COMMENT_NO_ANSWER_PREFIX = "אין מענה: ";
	public final static String COMMENT_ANSWER_PREFIX = "מענה: ";
	
	public final static int DEFAULT_CONVERSATION_AS_ANSWERED_IN_SECONDS = 30;
	
	public final static String GET_MESSAGE_ID_BY_EVENT_ID = "SELECT messageId FROM TextToSpeechMessages WHERE eventId = ?";
	
	
	public final static String SQL_QUERY_UPSERT_ALERT = "IF NOT EXISTS (SELECT * FROM Alerts WHERE alarmIncidentNumber = ?) " +
			" INSERT INTO Alerts (kId, createdAt, updatedAt, siteNumber, systemNumber, alarmIncidentNumber, dispatchLocation, alarmEventId, currentWriteEventCode,"
			+ " fullClearStatus, isActiveAlert, alertHandlingStatusCode, alertHandlingStatusMessage, progressMessages, contacts, callGeneratedText, textToSpeechFileLocation,"
			+ " vonageCurrentConversationId, answeredPhoneNumber, orderOfAnsweredCall, vonageConversationLength, customerResponseToCall, alertDate, alertZoneId) "
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
			+ " ELSE UPDATE Alerts SET kId=?, createdAt=?, updatedAt=?, siteNumber=?, systemNumber=?, alarmIncidentNumber=?, dispatchLocation=?, alarmEventId=?,"
			+ " currentWriteEventCode=?, fullClearStatus=?, isActiveAlert=?, alertHandlingStatusCode=?, alertHandlingStatusMessage=?, progressMessages=?, contacts=?,"
			+ " callGeneratedText=?, textToSpeechFileLocation=?, vonageCurrentConversationId=?, answeredPhoneNumber=?, orderOfAnsweredCall=?, vonageConversationLength=?,"
			+ " customerResponseToCall=?, alertDate=?, alertZoneId=? WHERE alarmIncidentNumber=?";
	
	public final static String SQL_QUERY_INSERT_CONVERSATION_RECORD = "INSERT INTO Conversations (conversationId, uuid, fromNo, toNo, eventTimestamp, disconnectedBy, "
			+ "duration, rate, price, startTime, endTime, rawEvent, kId, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public final static String SQL_QUERY_GET_UUID_FROM_CONVERSATION_ID = "SELECT DISTINCT uuid FROM Conversations WHERE conversationId=?";
	public final static String SQL_QUERY_GET_STATUS_OF_ANSWERED_CALLS_BY_IDS = "SELECT status FROM Conversations WHERE (uuid = ? OR conversationId = ?)"
			+ " AND (status = 'dtmf:1' or status = 'dtmf:2')";
	public final static String SQL_QUERY_CHECK_NUMBER_OF_ANSWERS = "SELECT COUNT(*) AS count FROM Conversations WHERE (uuid = ? OR conversationId = ?)" +
			" AND status LIKE 'dtmf%'";
	
	public final static String CONVERSATIONS_COLUMN_UUID = "uuid";
	public final static String CONVERSATIONS_COLUMN_STATUS = "status";
	
	public final static String SQL_QUERY_GET_DISPATCH_NUMBER_FROM_ID = "SELECT dispatchPhoneNumber FROM DispatchIdToPhoneNumber WHERE dispatchLocation=?";
	
	public final static String DISPATCH_COLUMN_PHONE_NUMBER = "dispatchPhoneNumber";
	
	public final static int VONAGE_GENERATE_TOKEN_MAX_ATTEMPTS = 3;
	
	public final static String ISRAEL_COUNTRY_CODE = "972";
	
	public final static int ISO_DATE_FORMAT_EXPECTED_LENGTH = 24;
	
	
	public final static String VONAGE_START_CALL_REQUEST_BODY =  "{\n"
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
			+ "                \"timeout\": $clientResponseTimeout$\n"
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
	
	public final static String VONAGE_CALL_FOR_ACTION_NCCO_EVENT = "[\n"
			+ "	{\n"
			+ "		\"streamUrl\": [\n"
			+ "			\"$vonageStreamUrl$\"\n"
			+ "		],\n"
			+ "		\"activeAction\": true,\n"
			+ "		\"level\": 0,\n"
			+ "		\"loop\": 1,\n"
			+ "		\"action\": \"stream\",\n"
			+ "		\"bargeIn\": true\n"
			+ "	},\n"
			+ "	{\n"
			+ "		\"action\": \"input\",\n"
			+ "		\"dtmf\": {\n"
			+ "			\"maxDigits\": 1,\n"
			+ "			\"submitOnHash\": \"true\",\n"
			+ "			\"timeout\": $clientResponseTimeout$\n"
			+ "		},\n"
			+ "		\"eventUrl\": [\n"
			+ "			\"$vonageEventUrl$\"\n"
			+ "		],\n"
			+ "		\"eventMethod\": \"POST\"\n"
			+ "	}\n"
			+ "]";
	
	public final static String VONAGE_GOODBYE_NCCO_EVENT = "[\n"
			+ "	{\n"
			+ "		\"streamUrl\": [\n"
			+ "			\"$vonageStreamUrl$\"\n"
			+ "		],\n"
			+ "		\"activeAction\": true,\n"
			+ "		\"level\": 0,\n"
			+ "		\"loop\": 1,\n"
			+ "		\"action\": \"stream\",\n"
			+ "		\"bargeIn\": false\n"
			+ "	}\n"
			+ "]";
	
	public final static String VONAGE_TRANSFER_CALL_NCCO_EVENT = "[\n"
			+ "	{\n"
			+ "		\"streamUrl\": [\n"
			+ "			\"$vonageStreamUrl$\"\n"
			+ "		],\n"
			+ "		\"activeAction\": true,\n"
			+ "		\"level\": 0,\n"
			+ "		\"loop\": 1,\n"
			+ "		\"action\": \"stream\",\n"
			+ "		\"bargeIn\": false\n"
			+ "	},\n"
			+ "	{\n"
			+ "		\"action\": \"connect\",\n"
			+ "		\"eventUrl\": [ \"$vonageEventUrl$\"],\n"
			+ "		\"timeout\": $transferCallRingTimeout$,\n"
			+ "		\"randomFromNumber\": true,\n"
			+ "		\"endpoint\": [\n"
			+ "			{\n"
			+ "				\"type\": \"phone\",\n"
			+ "				\"number\": \"$dispactchNumber$\",\n"
			+ "			}\n"
			+ "		]\n"
			+ "	}\n"
			+ "]";
}
