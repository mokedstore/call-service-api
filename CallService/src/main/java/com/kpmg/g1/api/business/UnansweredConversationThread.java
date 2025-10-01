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
			log.warn("Received vonage UUID: " + this.vonageUuid + " which does not have a matching Alert object in Alerts table! check ASAP");
			return;
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
					Constants.FULL_CLEAR_FLAG_NO, Constants.COMMENT_NO_ANSWER_PREFIX + currentUnansweredContact.getString("name") +"," + currentUnansweredContact.getString("phone"));
			if (updateEventOfUnansweredCallResponse == null) {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
						"Failed to update write-event API with status + of unasnwered call of contact: " 
						+ currentUnansweredContact.getString("name") + " with phone: " + currentUnansweredContact.getString("phone"));
				alert.setUpdatedAt(Utils.getTimestampFromDate(null));
				CallServiceDAOImplementation.upsertAlert(alert);
				return;
			} else {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
						"successfuly updated write event api with code: " + Constants.WRITE_EVENT_CODE_NO_ANSWER + " and flag  " + Constants.FULL_CLEAR_FLAG_NO
						+ " of contact: " + currentUnansweredContact.getString("name") + " with phone: " + currentUnansweredContact.getString("phone"));
			}
		}
		
		// start call with the next contact
		String phoneNumber = nextContact.getString("phone");
		JSONObject startVonageCallResponse = Utils.vonageStartCall(contacts.getJSONObject(0).getString("phone"), alert.getTextToSpeechFileLocation());
		if (startVonageCallResponse == null) {
			alert.setAlertHandlingStatusCode(Constants.FAILED_TO_START_VONAGE_CALL_STATUS_CODE);
			alert.setAlertHandlingStatusMessage("Failed to create vonage call");
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to create call to vonage to number: " + phoneNumber); 
			alert.setActiveAlert(false);
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
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
				alert.getFullClearStatus(), Constants.COMMENT_NO_ANSWER_PREFIX + lastContact.getString("name") +"," + lastContact.getString("phone"));
		if (updateEventOfUnansweredAlertResponse == null) {
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
					+ "of contact: " + lastContact.getString("name") + " with phone: " + lastContact.getString("phone"));
		}
		// send SMS message to each contact
		String messageId = CallServiceDAOImplementation.getMessageIdByEventIdForTextToSpeech(Constants.NO_RESPONSE_SMS_CONTENT_MESSAGE_ID);
		if (messageId == null) {
			log.error("Failed to get message id for send SMS after received no response from any contact for alert: " + alert.getAlarmIncidentNumber());
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to get message id for send SMS after received no response from any contact for alert");
		} else {
			int successSendMessageCounter = 0;
			for (int i = 0; i < contacts.length(); i++) {
				JSONObject currentContact = contacts.getJSONObject(i);
				JSONObject sendSmsResponseObject = Utils.sendMessage(alert.getSiteNumber(), alert.getSystemNumber(),
						alert.getAlarmIncidentNumber(), messageId, true, currentContact.getString("phone"));
				if (sendSmsResponseObject == null) {
					log.error("Failed to send SMS to contact: " + currentContact.getString("name") + " with phone: " + currentContact.getString("phone")
						+ " with alert: " + alert.getAlarmIncidentNumber());
					alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
							"Failed to send SMS to contact: " + currentContact.getString("name") + " with phone: " + currentContact.getString("phone"));
					
				} else {
					// send successful send message write event and increase counter
					JSONObject updateEventSendSMSResponse = Utils.updateEvent(alert.getSystemNumber(), alert.getAlarmIncidentNumber(), Constants.WRITE_EVENT_SUCCESSFUL_SMS_SENT,
							Constants.FULL_CLEAR_FLAG_YES, currentContact.getString("name") +"," + currentContact.getString("phone"));
					successSendMessageCounter++;
					if (updateEventSendSMSResponse == null) {
						log.error("Failed to send write event of successful SMS sending for unanswered alert " + 
									" to contact: " + currentContact.getString("name") + " with phone: " + currentContact.getString("phone") + " with alert: " + alert.getAlarmIncidentNumber());
						alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
								"Failed to send write event of successful SMS sending for unanswered alert: " + currentContact.getString("name") + " with phone: " + currentContact.getString("phone"));
					} else {
						alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
								"successfuly updated write event api with code: " + Constants.WRITE_EVENT_SUCCESSFUL_SMS_SENT + " and flag " + Constants.FULL_CLEAR_FLAG_YES
								+ "of contact: " + currentContact.getString("name") + " with phone: " + currentContact.getString("phone"));
					}
				}
			}
			if (successSendMessageCounter == contacts.length()) {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
						"Successfuly sent SMS to all contacts after received no answer by call"); 
			} else {
				alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
						"Successfuly sent " + String.valueOf(successSendMessageCounter) + " SMS out of " + String.valueOf(contacts.length()) + " Check logs for SMS send failures");
			}
		}
		
		alert.setUpdatedAt(Utils.getTimestampFromDate(null));
		CallServiceDAOImplementation.upsertAlert(alert);
		
	}
}
