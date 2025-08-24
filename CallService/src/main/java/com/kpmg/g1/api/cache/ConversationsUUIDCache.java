package com.kpmg.g1.api.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConversationsUUIDCache {
	final static Logger log = LogManager.getLogger(ConversationsUUIDCache.class.getName());
	
	private static ConversationsUUIDCache instance;
	private Map<String, String> conversationToKidCache;
	
	private ConversationsUUIDCache(Map<String, String> conversationToKidCache) {
		this.conversationToKidCache = conversationToKidCache;
	}
	
	public static ConversationsUUIDCache getInstance() {
		if (instance == null) {
			instance = new ConversationsUUIDCache(new HashMap<String, String>());
			return instance;
		}
		return instance;
	}
	
	public Map<String, String> getConversationToKidCache() {
		return conversationToKidCache;
	}
	
	public void addToCache(String key, String value) {
		this.conversationToKidCache.put(key, value);
	}
	
	public void clearCache() {
		log.info("Cleaning conversation to K_ID cache");
		this.conversationToKidCache.clear();
	}
	
	
}
