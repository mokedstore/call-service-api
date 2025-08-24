package com.kpmg.g1.api.objects.model;

import org.json.JSONObject;

public class Alert {
	
	private String kId;
	private String createdAt;
	private String updatedAt;
	private String siteNumber;
	private String systemNumber;
	private String alarmIncidentNumber;
	private String dispatchLocation;
	private String alaramEventId;
	private String currentWriteEventCode;
	private String fullClearStatus;
	private boolean isActiveAlert;
	private String alertHandlingStatusCode;
	private String alertHandlingStatusMessage;
	private String progressMessages;
	private String contacts;
	private String callGeneratedText;
	private String textToSpeechFileLocation;
	private String vonageCurrentConversationId;
	private String answeredPhoneNumber;
	private int orderOfAnsweredCall;
	private int vonageConversationLength;
	private String customerResponseToCall;
	
	
	public Alert() {
		this.kId = "";
		this.createdAt = "";
		this.updatedAt = "";
		this.siteNumber = "";
		this.systemNumber = "";
		this.alarmIncidentNumber = "";
		this.dispatchLocation = "";
		this.alaramEventId = "";
		this.currentWriteEventCode = "";
		this.fullClearStatus = "";
		this.isActiveAlert = false;
		this.alertHandlingStatusCode = "";
		this.alertHandlingStatusMessage = "";
		this.progressMessages = "";
		this.contacts = "";
		this.callGeneratedText = "";
		this.textToSpeechFileLocation = "";
		this.vonageCurrentConversationId = "";
		this.answeredPhoneNumber = "";
		this.orderOfAnsweredCall = 0;
		this.vonageConversationLength = 0;
		this.customerResponseToCall = "";
	}
	
	public Alert(String kId, String createdAt, String updatedAt, String siteNumber, String systemNumber,
			String alarmIncidentNumber, String dispatchLocation, String alaramEventId, String currentWriteEventCode,
			String fullClearStatus, boolean isActiveAlert, String alertHandlingStatusCode,
			String alertHandlingStatusMessage, String progressMessages, String contacts, String callGeneratedText,
			String textToSpeechFileLocation, String vonageCurrentConversationId, String answeredPhoneNumber,
			int orderOfAnsweredCall, int vonageConversationLength, String customerResponseToCall) {
		this.kId = kId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.siteNumber = siteNumber;
		this.systemNumber = systemNumber;
		this.alarmIncidentNumber = alarmIncidentNumber;
		this.dispatchLocation = dispatchLocation;
		this.alaramEventId = alaramEventId;
		this.currentWriteEventCode = currentWriteEventCode;
		this.fullClearStatus = fullClearStatus;
		this.isActiveAlert = isActiveAlert;
		this.alertHandlingStatusCode = alertHandlingStatusCode;
		this.alertHandlingStatusMessage = alertHandlingStatusMessage;
		this.progressMessages = progressMessages;
		this.contacts = contacts;
		this.callGeneratedText = callGeneratedText;
		this.textToSpeechFileLocation = textToSpeechFileLocation;
		this.vonageCurrentConversationId = vonageCurrentConversationId;
		this.answeredPhoneNumber = answeredPhoneNumber;
		this.orderOfAnsweredCall = orderOfAnsweredCall;
		this.vonageConversationLength = vonageConversationLength;
		this.customerResponseToCall = customerResponseToCall;
	}

	public String getkId() {
		return kId;
	}

	public void setkId(String kId) {
		this.kId = kId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getSiteNumber() {
		return siteNumber;
	}

	public void setSiteNumber(String siteNumber) {
		this.siteNumber = siteNumber;
	}

	public String getSystemNumber() {
		return systemNumber;
	}

	public void setSystemNumber(String systemNumber) {
		this.systemNumber = systemNumber;
	}

	public String getAlarmIncidentNumber() {
		return alarmIncidentNumber;
	}

	public void setAlarmIncidentNumber(String alarmIncidentNumber) {
		this.alarmIncidentNumber = alarmIncidentNumber;
	}

	public String getDispatchLocation() {
		return dispatchLocation;
	}

	public void setDispatchLocation(String dispatchLocation) {
		this.dispatchLocation = dispatchLocation;
	}

	public String getAlaramEventId() {
		return alaramEventId;
	}

	public void setAlaramEventId(String alaramEventId) {
		this.alaramEventId = alaramEventId;
	}

	public String getCurrentWriteEventCode() {
		return currentWriteEventCode;
	}

	public void setCurrentWriteEventCode(String currentWriteEventCode) {
		this.currentWriteEventCode = currentWriteEventCode;
	}

	public String getFullClearStatus() {
		return fullClearStatus;
	}

	public void setFullClearStatus(String fullClearStatus) {
		this.fullClearStatus = fullClearStatus;
	}

	public boolean isActiveAlert() {
		return isActiveAlert;
	}

	public void setActiveAlert(boolean isActiveAlert) {
		this.isActiveAlert = isActiveAlert;
	}

	public String getAlertHandlingStatusCode() {
		return alertHandlingStatusCode;
	}

	public void setAlertHandlingStatusCode(String alertHandlingStatusCode) {
		this.alertHandlingStatusCode = alertHandlingStatusCode;
	}

	public String getAlertHandlingStatusMessage() {
		return alertHandlingStatusMessage;
	}

	public void setAlertHandlingStatusMessage(String alertHandlingStatusMessage) {
		this.alertHandlingStatusMessage = alertHandlingStatusMessage;
	}

	public String getProgressMessages() {
		return progressMessages;
	}

	public void setProgressMessages(String progressMessages) {
		this.progressMessages = progressMessages;
	}

	public String getContacts() {
		return contacts;
	}

	public void setContacts(String contacts) {
		this.contacts = contacts;
	}

	public String getCallGeneratedText() {
		return callGeneratedText;
	}

	public void setCallGeneratedText(String callGeneratedText) {
		this.callGeneratedText = callGeneratedText;
	}

	public String getTextToSpeechFileLocation() {
		return textToSpeechFileLocation;
	}

	public void setTextToSpeechFileLocation(String textToSpeechFileLocation) {
		this.textToSpeechFileLocation = textToSpeechFileLocation;
	}

	public String getVonageCurrentConversationId() {
		return vonageCurrentConversationId;
	}

	public void setVonageCurrentConversationId(String vonageCurrentConversationId) {
		this.vonageCurrentConversationId = vonageCurrentConversationId;
	}

	public String getAnsweredPhoneNumber() {
		return answeredPhoneNumber;
	}

	public void setAnsweredPhoneNumber(String answeredPhoneNumber) {
		this.answeredPhoneNumber = answeredPhoneNumber;
	}

	public int getOrderOfAnsweredCall() {
		return orderOfAnsweredCall;
	}

	public void setOrderOfAnsweredCall(int orderOfAnsweredCall) {
		this.orderOfAnsweredCall = orderOfAnsweredCall;
	}

	public int getVonageConversationLength() {
		return vonageConversationLength;
	}

	public void setVonageConversationLength(int vonageConversationLength) {
		this.vonageConversationLength = vonageConversationLength;
	}

	public String getCustomerResponseToCall() {
		return customerResponseToCall;
	}

	public void setCustomerResponseToCall(String customerResponseToCall) {
		this.customerResponseToCall = customerResponseToCall;
	}
	
	public JSONObject parseJson() {
		JSONObject json = new JSONObject();
		json.put("kId", this.kId).put("createdAt", this.createdAt).put("updatedAt", this.updatedAt).put("siteNumber", this.siteNumber)
			.put("systemNumber", this.systemNumber).put("alarmIncidentNumber", this.alarmIncidentNumber).put("dispatchLocation", this.dispatchLocation).put("alaramEventId", this.alaramEventId)
			.put("currentWriteEventCode", this.currentWriteEventCode).put("fullClearStatus", this.fullClearStatus).put("isActiveAlert", this.isActiveAlert)
			.put("alertHandlingStatusCode", this.alertHandlingStatusCode).put("alertHandlingStatusMessage", this.alertHandlingStatusMessage).put("progressMessages", this.progressMessages)
			.put("contacts", this.contacts).put("callGeneratedText", this.callGeneratedText).put("textToSpeechFileLocation", this.textToSpeechFileLocation)
			.put("vonageCurrentConversationId", this.vonageCurrentConversationId).put("answeredPhoneNumber", this.answeredPhoneNumber).put("orderOfAnsweredCall", this.orderOfAnsweredCall)
			.put("vonageConversationLength", this.vonageConversationLength).put("customerResponseToCall", this.customerResponseToCall);
		return json;
	}
	

}
