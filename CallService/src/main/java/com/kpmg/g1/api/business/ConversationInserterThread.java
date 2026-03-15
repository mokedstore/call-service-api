package com.kpmg.g1.api.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Conversation;

public class ConversationInserterThread extends Thread  {
	
	final static Logger log = LogManager.getLogger(ConversationInserterThread.class.getName());
	
	private Conversation conversation;
	private String traceId;
	
	
	public ConversationInserterThread() {
		this.conversation = null;
		this.traceId = "";
	}
	
	public ConversationInserterThread(Conversation conversation, String traceId) {
		this.conversation = new Conversation(conversation);
		this.traceId = traceId;
	}
	
	@Override
	public void run() {
		CallServiceDAOImplementation.insertConversation(this.conversation);
		log.trace("Answer - Inserted Conversation - traceId: " + this.traceId + " uuid: " + this.conversation.getUuid()
			 + "ConversationId: " + this.conversation.getConversationId() + " kId: " + this.conversation.getkId());
	}
	
}
