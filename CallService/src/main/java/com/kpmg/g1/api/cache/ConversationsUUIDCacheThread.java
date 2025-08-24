package com.kpmg.g1.api.cache;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConversationsUUIDCacheThread extends Thread {
	
	final static Logger log = LogManager.getLogger(ConversationsUUIDCacheThread.class.getName());
	private static boolean clearCacheLoop = true;
	
	@Override
	public void run() {
		while(clearCacheLoop) {
			if(Thread.currentThread().isInterrupted()) {
				log.info("ConversationsUUIDCacheThread was interrupted - If this is not due to service restart this not a expected behavior");
			}
			try {
				Thread.sleep(10000);
				ConversationsUUIDCache.getInstance().clearCache();
			} catch(Exception e) {
				log.error("Error occurred inside ConversationsUUIDCacheThread " + ExceptionUtils.getStackTrace(e));
			}
		}
		log.info("ConversationsUUIDCacheThread was gracefully terminated");
	}
	
	public static void changeClearCacheLoop(boolean clearCacheLoopValue) {
		clearCacheLoop = clearCacheLoopValue;
	}
	
}
