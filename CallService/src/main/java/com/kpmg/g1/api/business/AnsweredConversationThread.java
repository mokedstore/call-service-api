package com.kpmg.g1.api.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.Utils;

public class AnsweredConversationThread extends Thread {

	final static Logger log = LogManager.getLogger(UnansweredConversationThread.class.getName());
	private String vonageUuid;
	private String contactResponse;
	private String answeredNumber;
	private int conversationDuration;
	
	public AnsweredConversationThread() {
		this.vonageUuid = "";
		this.contactResponse = "";
		this.answeredNumber = "";
		this.conversationDuration = 0;
	}
	
	public AnsweredConversationThread(String vonageUuid, String contactResponse, String answeredNumber, int conversationDuration) {
		this.vonageUuid = vonageUuid;
		this.contactResponse = contactResponse;
		this.answeredNumber = answeredNumber;
		this.conversationDuration = conversationDuration;
	}

	public String getVonageUuid() {
		return vonageUuid;
	}

	public void setVonageUuid(String vonageUuid) {
		this.vonageUuid = vonageUuid;
	}

	public String getContactResponse() {
		return contactResponse;
	}

	public void setContactResponse(String contactResponse) {
		this.contactResponse = contactResponse;
	}
	
	public String getAnsweredNumber() {
		return answeredNumber;
	}

	public void setAnsweredNumber(String answeredNumber) {
		this.answeredNumber = answeredNumber;
	}

	public int getConversationDuration() {
		return conversationDuration;
	}

	public void setConversationDuration(int conversationDuration) {
		this.conversationDuration = conversationDuration;
	}

	@Override
	public void run() {
		// fetch Alert object from DB by vonage uuid
		Alert alert = CallServiceDAOImplementation.getAlertByVonageUuid(this.vonageUuid);
		if (alert == null) {
			log.warn("Received vonage UUID: " + this.vonageUuid + " which does not have a matching Alert object in Alerts table! check ASAP");
			return;
		}
		alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
				"Received answer from contact with number: " + this.answeredNumber + " with value: " + this.contactResponse + ". Ending alert with status +23"); 
		alert.setAlertHandlingStatusMessage("Received Contact Answer");
		String answeredContactPhone = "";
		String answeredContactName = "";
		int answeredContactOrder = 0;
		JSONArray contacts = new JSONArray(alert.getContacts());
		JSONObject answeredContactData = getAnsweredContactDetails(contacts);
		if (answeredContactData != null) {
			answeredContactPhone = answeredContactData.getString("phone");
			answeredContactName = answeredContactData.getString("name");
			answeredContactOrder = answeredContactData.getInt("order");
		}
		alert.setAnsweredPhoneNumber(answeredContactPhone);
		alert.setCustomerResponseToCall(this.contactResponse.replace("dtmf:", ""));
		alert.setVonageConversationLength(this.conversationDuration);
		alert.setOrderOfAnsweredCall(answeredContactOrder);
		alert.setActiveAlert(false);
		alert.setCurrentWriteEventCode(Constants.WRITE_EVENT_RECEIVED_USER_ANSWER);
		alert.setFullClearStatus(Constants.FULL_CLEAR_FLAG_YES);
		String writeEventComment = Constants.COMMENT_ANSWER_PREFIX + answeredContactName + "," + answeredContactPhone;
		JSONObject updateEventOfValidAlertResponse = Utils.updateEvent(alert.getSystemNumber(), alert.getAlarmIncidentNumber(), alert.getCurrentWriteEventCode(),
				alert.getFullClearStatus(), writeEventComment);
		if (updateEventOfValidAlertResponse == null) {
			alert.setAlertHandlingStatusCode(Constants.GENERAL_G1_RUNTIME_ERROR);
			alert.setAlertHandlingStatusMessage("Failed to update event due to error in write-event API");
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_ERROR,
					"Failed to update write-event API");
			alert.setUpdatedAt(Utils.getTimestampFromDate(null));
			CallServiceDAOImplementation.upsertAlert(alert);
			return;
		} else {
			alert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO,
					"successfuly updated write event api with code: " + alert.getCurrentWriteEventCode() + " and flag " + alert.getFullClearStatus() + ". Alert was handled successfully");
		}
		alert.setUpdatedAt(Utils.getTimestampFromDate(null));
		CallServiceDAOImplementation.upsertAlert(alert);
	}
	
	private JSONObject getAnsweredContactDetails(JSONArray contacts) {
		if (this.answeredNumber == null || answeredNumber.length() < 3) {
			log.warn("getAnsweredContactDetails: answered number is null or empty. Can't get contact info");
			return null;
		}
		JSONObject contactData = null;
		// get value without country code
		String contactNumber = this.answeredNumber.substring(3);
		for (int i = 0; i < contacts.length(); i++) {
			JSONObject currentContact = contacts.getJSONObject(i);
			if (currentContact.getString("phone").contains(contactNumber)) {
				contactData = new JSONObject().put("phone", currentContact.getString("phone")).put("name", currentContact.getString("name")).put("order", i +1);
				return contactData;
			}
		}
		return contactData;
	}
	
}
