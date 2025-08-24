package com.kpmg.g1.api.context;

import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.cache.ConversationsUUIDCacheThread;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.FileWatcherThread;
import com.kpmg.g1.api.utils.JSONConfigurations;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
	
	final static Logger log = LogManager.getLogger(ContextListener.class.getName());
	private static FileWatcherThread fileWatcer;
	private static ConversationsUUIDCacheThread conversationsUUIDCacheThread;
	
	@Override
    public void contextInitialized(ServletContextEvent event) {
		JSONConfigurations.getInstance();
		// start file watcher thread
		fileWatcer = new FileWatcherThread();
		// name thread
		fileWatcer.setName("FileWatcher-Thread");
		fileWatcer.start();
		
		conversationsUUIDCacheThread = new ConversationsUUIDCacheThread();
		conversationsUUIDCacheThread.setName("conversationsUUIDCache-Thread");
		conversationsUUIDCacheThread.start();
	}
	
	@Override
    public void contextDestroyed(ServletContextEvent event) {
    	// unregister all sql drivers
	    Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
	    while (drivers.hasMoreElements()) {
	       java.sql.Driver driver = drivers.nextElement();
	       try {
	          java.sql.DriverManager.deregisterDriver(driver);
	      }  catch (Throwable t) {}
	    }
	    
	    // stop filewatcher
    	if (fileWatcer != null) {
    		fileWatcer.interrupt();
    	}
    	
    	if (conversationsUUIDCacheThread != null) {
    		conversationsUUIDCacheThread.interrupt();
    	}
    	try { Thread.sleep(Constants.KILL_THREADS_WAIT_TIME_IN_MILLIS); } catch (Exception e) {}
    }
}
