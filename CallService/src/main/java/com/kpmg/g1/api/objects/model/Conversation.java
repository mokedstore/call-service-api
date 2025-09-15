package com.kpmg.g1.api.objects.model;

import org.json.JSONObject;

public class Conversation {
	private String conversationId;
	private String uuid;
	private String fromNo;
	private String toNo;
	private String eventTimestamp;
	private String disconnectedBy;
	private int duration;
	private double rate;
	private double price;
	private String rawEvent;
	private String startTime;
	private String endTime; 
	private String kId;
	private String status;
	

	public Conversation() {
		this.conversationId = "";
		this.uuid = "";
		this.fromNo = "";
		this.toNo = "";
		this.eventTimestamp = "";
		this.disconnectedBy = "";
		this.duration = 0;
		this.rate = 0.0d;
		this.price = 0.0d;
		this.rawEvent = "";
		this.startTime = "";
		this.endTime = "";
		this.kId = "";
		this.status = "";
	}
	
	public Conversation(String conversationId, String uuid, String fromNo, String toNo, String eventTimestamp,
			String disconnectedBy, int duration, double rate, double price, String rawEvent, String startTime, String endTime, String kId, String status) {
		this.conversationId = conversationId;
		this.uuid = uuid;
		this.fromNo = fromNo;
		this.toNo = toNo;
		this.eventTimestamp = eventTimestamp;
		this.disconnectedBy = disconnectedBy;
		this.duration = duration;
		this.rate = rate;
		this.price = price;
		this.rawEvent = rawEvent;
		this.startTime = startTime;
		this.endTime = endTime;
		this.kId = kId;
		this.status = status;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFromNo() {
		return fromNo;
	}

	public void setFromNo(String fromNo) {
		this.fromNo = fromNo;
	}

	public String getToNo() {
		return toNo;
	}

	public void setToNo(String toNo) {
		this.toNo = toNo;
	}

	public String getEventTimestamp() {
		return eventTimestamp;
	}

	public void setEventTimestamp(String eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public String getDisconnectedBy() {
		return disconnectedBy;
	}

	public void setDisconnectedBy(String disconnectedBy) {
		this.disconnectedBy = disconnectedBy;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getRawEvent() {
		return rawEvent;
	}

	public void setRawEvent(String rawEvent) {
		this.rawEvent = rawEvent;
	}
	
	public String getkId() {
		return kId;
	}

	public void setkId(String kId) {
		this.kId = kId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Conversation [conversationId=" + conversationId + ", uuid=" + uuid + ", fromNo=" + fromNo + ", toNo="
				+ toNo + ", eventTimestamp=" + eventTimestamp + ", disconnectedBy=" + disconnectedBy + ", duration="
				+ duration + ", rate=" + rate + ", price=" + price + ", rawEvent=" + rawEvent + ", startTime="
				+ startTime + ", endTime=" + endTime + ", kId=" + kId + ", status=" + status + "]";
	}

	public JSONObject parseJson() {
		JSONObject json = new JSONObject();
		json.put("conversationId", this.conversationId).put("uuid", this.uuid).put("fromNo", this.fromNo).put("toNo", this.toNo)
			.put("eventTimestamp", this.eventTimestamp).put("disconnectedBy", this.disconnectedBy).put("disconnectedBy", this.disconnectedBy).put("rate", this.rate)
			.put("price", this.price).put("rawEvent", this.rawEvent).put("kId", this.kId);
		return json;
	}
	
	
}
