package com.kpmg.g1.api.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.cache.ConversationsUUIDCache;
import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.JSONConfigurations;
import com.kpmg.g1.api.utils.Utils;

public class OpenAlertHandler extends Thread {
	
	final static Logger log = LogManager.getLogger(OpenAlertHandler.class.getName());
	private Alert alert;
	
	public OpenAlertHandler() {
		this.alert = new Alert();
	}
	
	public OpenAlertHandler(Alert alert) {
		this.alert = alert;
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = alert;
	}
	
	@Override
	public void run() {
		if (this.alert.getAlertInitStatus().equals(Constants.NEW_ALERT_INIT_STATUS_NEW)) {
			handleNewAlert();
		} else if (this.alert.getAlertInitStatus().equals(Constants.NEW_ALERT_INIT_STATUS_DUPLICATE)) {
			handleDuplicateAlert();
		}
	}
	
	private void handleNewAlert() {
		this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"successfuly retrieved open alert. start handling it");
		this.alert.setAlertHandlingStatusCode(Constants.VALID_ALERT_STATUS_CODE);
		this.alert.setCurrentWriteEventCode(Constants.WRITE_EVENT_CODE_NEW_ACK);
		this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_NO);
		// update event with G1 API to acknowledge it
		JSONObject updateEventOfDuplicateAlertResponse = Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
				this.alert.getFullClearStatus(), "", Constants.FULL_CLEAR_FLAG_NO);
		if (updateEventOfDuplicateAlertResponse == null) {
			this.alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			this.alert.setAlertHandlingStatusMessage("Failed to update event due to error in write-event API");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to update write-event API");
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			CallServiceDAOImplementation.upsertAlert(this.alert);
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"successfuly updated write event api with code: " + this.alert.getCurrentWriteEventCode() + " (acknowledged) and flag " + this.alert.getFullClearStatus());
		}
		// Get message id in order to create text to speech file
		String messageId = CallServiceDAOImplementation.getMessageIdByEventIdForTextToSpeech(alert.getAlarmEventId());
		// in case message id is null check for custom rules
		if (messageId == null) {
			messageId = getCustomMessageIdFromAlarmEventId();
		}
		if (messageId == null) {
			this.alert.setAlertHandlingStatusCode(Constants.NO_MATCHING_MESSAGE_ID_STATUS_CODE);
			this.alert.setAlertHandlingStatusMessage("Failed to find matching messageId for eventId: " + alert.getAlarmEventId());
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to to find matching messageId for eventId: " + alert.getAlarmEventId());
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			CallServiceDAOImplementation.upsertAlert(this.alert);
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully retrieved message id using alarm event id to get alarm content");
		}
		// get list of contacts that should be called
		JSONObject callListResponse = Utils.getCallList(this.alert.getSiteNumber(), this.alert.getSystemNumber(), this.alert.getAlertZoneId());
		if ((callListResponse == null) || (!callListResponse.has("results")) || (callListResponse.getJSONArray("results").length() == 0)) {
			this.alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			this.alert.setAlertHandlingStatusMessage("Failed to get contacts list");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to get contacts list for site number: " + alert.getSiteNumber() + " system number: " + alert.getSystemNumber() +
					" zone id: " + alert.getAlertZoneId());
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			CallServiceDAOImplementation.upsertAlert(this.alert);
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully received call list contacts");
			this.alert.setContacts(callListResponse.getJSONArray("results").toString());
		}
		// get text for text to speech logic and create WAV file
		JSONObject getSsmlDataObject = Utils.sendMessage(this.alert.getSiteNumber(), this.alert.getSystemNumber(),
				this.alert.getAlarmIncidentNumber(), messageId, false, "");
		String ssmlText = null;
		if ((getSsmlDataObject == null) || (!getSsmlDataObject.has("ssml_content")) || (getSsmlDataObject.getString("ssml_content").isEmpty())) {
			this.alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			this.alert.setAlertHandlingStatusMessage("Failed to get contacts ssml content from send message api");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to get ssml content using send message API for site number: " + alert.getSiteNumber() + " system number: " + alert.getSystemNumber() +
					" alarm incident number: " + this.alert.getAlarmIncidentNumber()); 
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			CallServiceDAOImplementation.upsertAlert(this.alert);
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully retrieved SSML contnet for Text to Speech creation");
			ssmlText = getSsmlDataObject.getString("ssml_content");
			this.alert.setCallGeneratedText(ssmlText);

		}
		JSONObject textToSpeechResponse = Utils.convertTextToSpeech(ssmlText);
		if (textToSpeechResponse == null) {
			this.alert.setAlertHandlingStatusCode(Constants.FAILED_TO_CREATE_AUDIO_FILE_STATUS_CODE);
			this.alert.setAlertHandlingStatusMessage("Failed to create audio file from text");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to create audio file from SSML content: " + ssmlText); 
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			CallServiceDAOImplementation.upsertAlert(this.alert);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully created audio file for SSML text");
			this.alert.setTextToSpeechFileLocation(textToSpeechResponse.getString("path"));
		}
		// for each contact add number of tries field to keep track
		JSONArray contacts = new JSONArray(this.alert.getContacts());
		for (int i = 0; i < contacts.length(); i++) {
			contacts.getJSONObject(i).put("numberOfTries", 0);
		}
		this.alert.setContacts(contacts.toString());
		String phoneNumber = contacts.getJSONObject(0).getString("phone");
		//String callToNumber
		JSONObject startVonageCallResponse = Utils.vonageStartCall(phoneNumber, this.alert.getTextToSpeechFileLocation());
		if (startVonageCallResponse == null) {
			this.alert.setAlertHandlingStatusCode(Constants.FAILED_TO_START_VONAGE_CALL_STATUS_CODE);
			this.alert.setAlertHandlingStatusMessage("Failed to create vonage call");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to create call to vonage to number: " + phoneNumber); 
			this.alert.setActiveAlert(false);
			this.alert.setCurrentWriteEventCode(Constants.FAILED_ALERT_CODE_EVENT);
			this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
			// update that alert handling failed
			Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
					this.alert.getFullClearStatus(), Constants.FAILED_ALERT_COMMENT, Constants.FULL_CLEAR_FLAG_YES);
			Utils.sendSmsDirect(Integer.parseInt(this.alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), this.alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			CallServiceDAOImplementation.upsertAlert(this.alert);
			return;
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully started call with Vonage to number: " + phoneNumber + " attempt: 1");
			this.alert.setVonageCurrentConversationId(startVonageCallResponse.getString("uuid"));
			this.alert.setAlertHandlingStatusMessage(Constants.VONAGE_WAITING_FOR_CUSTOMER_RESPONSE);
			contacts.getJSONObject(0).put("numberOfTries", 1);
			this.alert.setContacts(contacts.toString());
			ConversationsUUIDCache.getInstance().addToCache(this.alert.getVonageCurrentConversationId(), this.alert.getkId());
		}
		CallServiceDAOImplementation.upsertAlert(this.alert);
	}
	
	private void handleDuplicateAlert() {
		this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"successfuly retrieved alert and found that it is a duplicate of alert with id: " + this.alert.getInCaseOfDuplicateCurrentAlertId());
		this.alert.setCurrentWriteEventCode(Constants.WRITE_EVENT_CODE_DUPLICATE);
		this.alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
		this.alert.setAlertHandlingStatusCode(Constants.DUPLICATE_ALERT_STATUS_CODE);
		this.alert.setAlertHandlingStatusMessage("Alert is a duplicate. site has already have an open alert");
		// try to update alert with relevant code and update its value in DB
		JSONObject updateEventOfDuplicateAlertResponse = Utils.updateEvent(this.alert.getSystemNumber(), this.alert.getAlarmIncidentNumber(), this.alert.getCurrentWriteEventCode(),
				this.alert.getFullClearStatus(), "", Constants.FULL_CLEAR_FLAG_NO);
		if (updateEventOfDuplicateAlertResponse == null) {
			this.alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			this.alert.setAlertHandlingStatusMessage("Failed to update event due to error in write-event API");
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to update write-event API");
		} else {
			this.alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"successfuly updated write event api with code: " + this.alert.getCurrentWriteEventCode() + " and flag " + this.alert.getFullClearStatus());
		}
		CallServiceDAOImplementation.upsertAlert(this.alert);
		
	}
	
	private String getCustomMessageIdFromAlarmEventId() {
		if(this.alert.getAlarmEventId().startsWith(Constants.ABNORMAL_OPENING_CODE_PREFIX)) {
			return Constants.ABNORMAL_OPENING_MESSAGE_ID;
		}
		return null;
	}

}
