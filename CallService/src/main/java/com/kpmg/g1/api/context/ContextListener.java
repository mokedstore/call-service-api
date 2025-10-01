package com.kpmg.g1.api.context;

import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.business.AlertsFetcherThread;
import com.kpmg.g1.api.business.AlertsOpenForTooLongThread;
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
	private static AlertsFetcherThread alertsFetcherThread;
	private static AlertsOpenForTooLongThread alertsOpenForTooLongThread;
	
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
		
		alertsFetcherThread = new AlertsFetcherThread();
		alertsFetcherThread.setName("alertsFetcher-Thread");
		alertsFetcherThread.start();
		
		alertsOpenForTooLongThread = new AlertsOpenForTooLongThread();
		alertsOpenForTooLongThread.setName("alertsOpenForTooLong-Thread");
		alertsOpenForTooLongThread.start();
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
    		ConversationsUUIDCacheThread.changeClearCacheLoop(false);
    	}
    	
    	if (alertsFetcherThread != null) {
    		AlertsFetcherThread.changeGetAlertLoop(false);
    	}
    	if (alertsOpenForTooLongThread != null) {
    		AlertsOpenForTooLongThread.changeGetAlertLoop(false);
    	}
    	try { Thread.sleep(Constants.KILL_THREADS_WAIT_TIME_IN_MILLIS); } catch (Exception e) {}
    }
}
