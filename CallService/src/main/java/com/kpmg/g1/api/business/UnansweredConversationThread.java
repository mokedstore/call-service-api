package com.kpmg.g1.api.business;

import org.apache.commons.lang3.exception.ExceptionUtils;
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

public class UnansweredConversationThread extends Thread {
	
	final static Logger log = LogManager.getLogger(UnansweredConversationThread.class.getName());
	private String vonageUuid;
	
	public UnansweredConversationThread() {
		this.vonageUuid = "";
	}
	
	public UnansweredConversationThread(String vonageUuid) {
		this.vonageUuid = vonageUuid;
	}

	public String getVonageUuid() {
		return vonageUuid;
	}

	public void setVonageUuid(String vonageUuid) {
		this.vonageUuid = vonageUuid;
	}
	
	@Override
	public void run() {
		// fetch Alert object from DB by vonage uuid
		Alert alert = CallServiceDAOImplementation.getAlertByVonageUuid(this.vonageUuid);
		if (alert == null) {
			// may be due to multiple transactions to db. try to sleep and check again
			try {
				Thread.sleep(5000);
			} catch (Exception e) {}
			log.info("check again for uuid: " + this.vonageUuid);
			alert = CallServiceDAOImplementation.getAlertByVonageUuid(this.vonageUuid);
			if (alert == null) {
				log.warn("Received vonage UUID: " + this.vonageUuid + " which does not have a matching Alert object in Alerts table! check ASAP");
				return;
			}
		}
		// check if all numbers in contacts were tried without success or should try to call the next number
		try {
			boolean allContactsDidntAnswer = wereAllContactsDidntAnswer(alert.getContacts());
			if (allContactsDidntAnswer) {
				handleAllContactsDidntAnswer(alert);
			} else {
				callNextContact(alert);
			}
		} catch (Exception e) {
			log.error("Exception occurred while processing vonage unanswered UUID call: " + this.vonageUuid + " of alert: " + alert.toString() + " Error: " + ExceptionUtils.getStackTrace(e));
		}
	}
	
	private boolean wereAllContactsDidntAnswer(String contacts) {
		JSONArray contactsArr = new JSONArray(contacts);
		// the main contact (first location) has different number of tries than other contacts so check it first before checking others
		JSONObject mainContact = contactsArr.getJSONObject(0);
		if (mainContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("mainContactNumberOfTries")) {
			return false;
		}
		if (contactsArr.length() == 1) {
			return true;
		}
		for (int i = 1; i < contactsArr.length(); i++) {
			JSONObject currentContact = contactsArr.getJSONObject(i);
			if (currentContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("otherContactsNumberOfTries")) {
				return false;
			}
		}
		return true;
	}
	
	private void callNextContact(Alert alert) {
		// get current contact for logging
		JSONObject currentUnansweredContact = null;
		JSONObject nextContact = null;
		// check if should try to call again to main contact or continue to the next one
		JSONArray contacts = new JSONArray(alert.getContacts());
		JSONObject mainContact = contacts.getJSONObject(0);
		if (mainContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("mainContactNumberOfTries")) {
			// means should try again to call main contact
			currentUnansweredContact = mainContact;
			nextContact = mainContact;
		}
		// check if still haven't found contacts info 
		if (currentUnansweredContact == null) {
			// for sure there is a second contact in the list otherwise this function would not have been called so check if next contact is the one
			// that should be called or if he also was already called
			JSONObject secondContact = contacts.getJSONObject(1);
			if (secondContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("otherContactsNumberOfTries")) {
				// means that the second contact is the one to call next
				currentUnansweredContact = mainContact;
				nextContact = secondContact;
			}
		}
		// in case contacts still not found then search in other contacts
		if (currentUnansweredContact == null) {
			for (int i = 1; i < contacts.length() - 1; i++) {
				JSONObject potentialLastUnansweredContact = contacts.getJSONObject(i);
				JSONObject potentialNextContact = contacts.getJSONObject(i + 1);
				// in case current contact has number of db tries less than the configuration then it is also the next contact
				if (potentialLastUnansweredContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("otherContactsNumberOfTries")) {
					currentUnansweredContact = potentialLastUnansweredContact;
					nextContact = potentialLastUnansweredContact;
					break;
				}
				if (potentialLastUnansweredContact.getInt("numberOfTries") >= JSONConfigurations.getInstance().getConfigurations().getInt("otherContactsNumberOfTries")
						&& potentialNextContact.getInt("numberOfTries") < JSONConfigurations.getInstance().getConfigurations().getInt("otherContactsNumberOfTries")) {
					currentUnansweredContact = potentialLastUnansweredContact;
					nextContact = potentialNextContact;
					break;
				}
			}
		}
		// log data about unanswered contact and try to call the next contact
		alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"Didn't receive answer from number: " + currentUnansweredContact.getString("phone") + " with number of tries: " + 
				String.valueOf(currentUnansweredContact.getInt("numberOfTries")) + " with vonage uuid: " + this.vonageUuid);
		
		// in case moved to new contact send write event with status +9 with current contact details indicating he didn't answer
		if ((!currentUnansweredContact.getString("phone").equals(nextContact.getString("phone"))) || (!currentUnansweredContact.getString("name").equals(nextContact.getString("name")))) {
			JSONObject updateEventOfUnansweredCallResponse = Utils.updateEvent(alert.getSystemNumber(), alert.getAlarmIncidentNumber(), Constants.WRITE_EVENT_CODE_NO_ANSWER,
					Constants.FULL_CLEAR_FLAG_NO,
					Constants.COMMENT_NO_ANSWER_PREFIX + currentUnansweredContact.getString("name") +"," + currentUnansweredContact.getString("phone"), Constants.FULL_CLEAR_FLAG_NO);
			if (updateEventOfUnansweredCallResponse == null) {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
						"Failed to update write-event API with status + of unasnwered call of contact: " 
						+ currentUnansweredContact.getString("name") + " with phone: " + currentUnansweredContact.getString("phone"));
				alert.setUpdatedAt(Utils.getTimestampFromDate(null));
				CallServiceDAOImplementation.upsertAlert(alert);
				return;
			} else {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
						"successfuly updated write event api with code: " + Constants.WRITE_EVENT_CODE_NO_ANSWER + " and flag " + Constants.FULL_CLEAR_FLAG_NO
						+ " of contact: " + currentUnansweredContact.getString("name") + " with phone: " + currentUnansweredContact.getString("phone"));
			}
		}
		
		// start call with the next contact
		String phoneNumber = nextContact.getString("phone");
		JSONObject startVonageCallResponse = Utils.vonageStartCall(phoneNumber, alert.getTextToSpeechFileLocation());
		if (startVonageCallResponse == null) {
			alert.setAlertHandlingStatusCode(Constants.FAILED_TO_START_VONAGE_CALL_STATUS_CODE);
			alert.setAlertHandlingStatusMessage("Failed to create vonage call");
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to create call to vonage to number: " + phoneNumber); 
			alert.setActiveAlert(false);
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			Utils.sendSmsDirect(Integer.parseInt(alert.getSiteNumber()), 
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationPhoneNumber"), alert.getCsNumber(),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationSubject"),
					JSONConfigurations.getInstance().getConfigurations().getString("errorNotificationMessage"));
			CallServiceDAOImplementation.upsertAlert(alert);
			return;
		} else {
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"Successfully started call with Vonage to number: " + phoneNumber + " attempt: " + String.valueOf(nextContact.getInt("numberOfTries") + 1));
			alert.setVonageCurrentConversationId(startVonageCallResponse.getString("uuid"));
			alert.setAlertHandlingStatusMessage(Constants.VONAGE_WAITING_FOR_CUSTOMER_RESPONSE);
			nextContact.put("numberOfTries", nextContact.getInt("numberOfTries") + 1);
			alert.setContacts(contacts.toString());
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			ConversationsUUIDCache.getInstance().addToCache(alert.getVonageCurrentConversationId(), alert.getkId());
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			CallServiceDAOImplementation.upsertAlert(alert);
		}
	}
	
	private void handleAllContactsDidntAnswer(Alert alert) {
		// update progress messages with the last contact didn't answer attempt
		JSONArray contacts = new JSONArray(alert.getContacts());
		JSONObject lastContact = contacts.getJSONObject(contacts.length() - 1);
		alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"Didn't receive answer from number: " + lastContact.getString("phone") + " with number of tries: " + 
				String.valueOf(lastContact.getInt("numberOfTries")) + " with vonage uuid: " + this.vonageUuid);
		alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"Tried to call all contacts but did not receive an answer. Ending alert with status +9"); 
		alert.setAlertHandlingStatusCode(Constants.VALID_ALERT_NO_ANSWER_STATUS_CODE);
		alert.setAlertHandlingStatusMessage("No answer was received from any contact");
		alert.setCurrentWriteEventCode(Constants.WRITE_EVENT_CODE_NO_ANSWER);
		alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
		// update event with G1 API to acknowledge it
		JSONObject updateEventOfUnansweredAlertResponse = Utils.updateEvent(alert.getSystemNumber(), alert.getAlarmIncidentNumber(), alert.getCurrentWriteEventCode(),
				alert.getFullClearStatus(), Constants.COMMENT_NO_ANSWER_PREFIX + lastContact.getString("name") +"," + lastContact.getString("phone"), Constants.FULL_CLEAR_FLAG_YES);
		if (updateEventOfUnansweredAlertResponse == null) {
			alert.setActiveAlert(false);
			alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			alert.setAlertHandlingStatusMessage("Failed to update event due to error in write-event API");
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to update write-event API");
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			CallServiceDAOImplementation.upsertAlert(alert);
			return;
		} else {
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"successfuly updated write event api with code: " + alert.getCurrentWriteEventCode() + " and flag " + alert.getFullClearStatus()
					+ " of contact: " + lastContact.getString("name") + " with phone: " + lastContact.getString("phone"));
		}
		// Get message id in order to create text to speech file
		String messageId = CallServiceDAOImplementation.getMessageIdByEventIdForTextToSpeech(alert.getAlarmEventId());
		// in case message id is null check for custom rules
		if (messageId == null) {
			messageId = getCustomMessageIdFromAlarmEventId(alert);
		}
		if (messageId == null) {
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to to find matching messageId for eventId: " + alert.getAlarmEventId() + " for sending SMS. Will not send SMS Message to non-answering contacts");
			log.error("Failed to to find matching messageId for eventId: " + alert.getAlarmEventId() + " for sending SMS. Will not send SMS Message to non-answering contacts");
		} else {
			JSONObject mainContact = contacts.getJSONObject(0);
			JSONObject sendSmsResponseObject = Utils.sendMessage(alert.getSiteNumber(), alert.getSystemNumber(),
					alert.getAlarmIncidentNumber(), messageId, true, mainContact.getString("phone"));
			if (sendSmsResponseObject == null) {
				log.error("Failed to send SMS to contact: " + mainContact.getString("name") + " with phone: " + mainContact.getString("phone")
					+ " with alert: " + alert.getAlarmIncidentNumber());
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
						"Failed to send SMS to contact: " + mainContact.getString("name") + " with phone: " + mainContact.getString("phone"));
				
			} else {
				// send successful send message write event and increase counter
				JSONObject updateEventSendSMSResponse = Utils.updateEvent(alert.getSystemNumber(), alert.getAlarmIncidentNumber(), Constants.WRITE_EVENT_SUCCESSFUL_SMS_SENT,
						Constants.FULL_CLEAR_FLAG_YES, mainContact.getString("name") +"," + mainContact.getString("phone"), Constants.FULL_CLEAR_FLAG_YES);
				if (updateEventSendSMSResponse == null) {
					log.error("Failed to send write event of successful SMS sending for unanswered alert " + 
								" to contact: " + mainContact.getString("name") + " with phone: " + mainContact.getString("phone") + " with alert: " + alert.getAlarmIncidentNumber());
					alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
							"Failed to send write event of successful SMS sending for unanswered alert: " + mainContact.getString("name") + " with phone: " + mainContact.getString("phone"));
				} else {
					alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
							"successfuly updated write event api with code: " + Constants.WRITE_EVENT_SUCCESSFUL_SMS_SENT + " and clear flag " + Constants.FULL_CLEAR_FLAG_YES
							+ " and all_system flag" + Constants.FULL_CLEAR_FLAG_YES + " of contact: " + mainContact.getString("name") + " with phone: " + mainContact.getString("phone"));
				}
			}
		}
		
		alert.setActiveAlert(false);
		alert.setUpdatedAt(Utils.getTimestampFromDate(null));
		CallServiceDAOImplementation.upsertAlert(alert);
		
	}
		
	private static String getCustomMessageIdFromAlarmEventId(Alert alert) {
		if(alert.getAlarmEventId().startsWith(Constants.ABNORMAL_OPENING_CODE_PREFIX)) {
			return Constants.ABNORMAL_OPENING_MESSAGE_ID;
		}
		return null;
	}
}
