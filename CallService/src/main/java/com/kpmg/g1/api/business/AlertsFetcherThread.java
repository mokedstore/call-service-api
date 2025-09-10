package com.kpmg.g1.api.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.JSONConfigurations;
import com.kpmg.g1.api.utils.Utils;


public class AlertsFetcherThread extends Thread {
	
	final static Logger log = LogManager.getLogger(AlertsFetcherThread.class.getName());
	private static boolean getAlertsLoop = true;
	private static boolean isAlertLoopMaster = checkAlertLoopMaster();
	
	@Override
	public void run() {
		
		if (isAlertLoopMaster) {
			log.info("Found that this instance is master for open alerts fetching. Start checking for open alerts");
			while(getAlertsLoop) {
				try {
					Thread.sleep(JSONConfigurations.getInstance().getConfigurations().getLong("waitTimeBetweenGetOpenAlertsCallsInMilliseconds"));
				} catch(Exception e) {
					log.error("Error occurred while trying to sleep in AlertsFetcherThread between alert calls. " + ExceptionUtils.getStackTrace(e));
				}
				JSONArray openAlerts = Utils.getOpenAlerts();
				log.info("Found " + openAlerts.length() + " open alerts.");
				if (openAlerts.length() > 0) {
					handleNewOpenAlerts(openAlerts);
				}
				log.info("Finished handling current new alerts");
			}
			
		} else {
			log.info("Found that this instance is not master for open alerts fetching");
		}
		log.info("AlertsFetcherThread was gracefully terminated");
	}
	
	private static boolean checkAlertLoopMaster() {
		try {
			String isAlertFetcher = System.getenv(Constants.IS_ALERT_FETCHER);
			if (isAlertFetcher.equals("true")) {
				return true;
			}
			return false;
		} catch(Exception e) {
			log.error("Error occured while trying to check if this instance should get open Alerts. This probably mean that IS_ALERT_FETCHER environment " +
					"variable was not set. HANDLE ASAP AS THIS MEANS ALERTS ARE NOT BEING TREATED. Error: " + ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
	
	public static void changeGetAlertLoop(boolean getAlertsLoopValue) {
		getAlertsLoop = getAlertsLoopValue;
	}
	
	private static void handleNewOpenAlerts(JSONArray openAlerts) {
		// get unique values of site numbers to check if any new alert is not relevant since there is already open alert in database
		Set<String> uniqueSiteNumbersFromOpenAlerts = new HashSet<String>();
		// run through open alerts and populate site numbers and create Alert Objects
		List<Alert> openAlersObjects = new ArrayList<Alert>();
		for (int i = 0; i < openAlerts.length(); i++) {
			JSONObject currentOpenAlert = openAlerts.getJSONObject(i);
			Alert newOpenAlert = Utils.buildAlertObjectFromOpenAlertsApi(currentOpenAlert);
			if (newOpenAlert == null) {
				log.error("Received alert object from alarms api that could not be marshalled to Alert object. Data object: " + currentOpenAlert + " Skipping its handling");
				continue;
			}
			uniqueSiteNumbersFromOpenAlerts.add(newOpenAlert.getSiteNumber());
			openAlersObjects.add(newOpenAlert);
		}
		List<Alert> alreadyOpenedAlertsOnCurrentAlertsSites = null;
		try {
			// try to get open alerts on the same sites
			alreadyOpenedAlertsOnCurrentAlertsSites = CallServiceDAOImplementation.getOpenAlertsBySiteNumbers(uniqueSiteNumbersFromOpenAlerts);
		} catch (Exception e) {
			log.error("Error Occurred while trying to get already opened alerts from SQL. Can't proceed with open alerts process. Error: " + ExceptionUtils.getStackTrace(e));
			return;
		}
		// loop over alerts and check if there are any opened alerts from this site
		for(Alert alert : openAlersObjects) {
			String alreadyOpenedAlertIncidentNumber = getOpenedAlertFromSite(alert.getSiteNumber(), alreadyOpenedAlertsOnCurrentAlertsSites);
			if (alreadyOpenedAlertIncidentNumber != null) {
				if (alreadyOpenedAlertIncidentNumber.equals(alert.getAlarmIncidentNumber())) {
					log.info("Received alarm incident number: " + alert.getAlarmIncidentNumber() +
							" which is already in progress. Skipping it has it is currently handled and should not be returned from alarms API");
					alert.setAlertInitStatus(Constants.NEW_ALERT_INIT_STATUS_DUPLICATE_IN_PROGRESS);
					alert.setWasAlertAlreadyHandled(true);
				} else {
					alert.setAlertInitStatus(Constants.NEW_ALERT_INIT_STATUS_DUPLICATE);
					alert.setActiveAlert(false);
					alert.setInCaseOfDuplicateCurrentAlertId(alreadyOpenedAlertIncidentNumber);
					alert.setWasAlertAlreadyHandled(true);
				}
			}
		}
		// create a map to hold site numbers that already seen in the current context that does not have already opened alerts in SQL
		Map<String, String> alreadySeenSiteNumbersInCurrentContext = new HashMap<String, String>();
		// for alerts that were not handled check if there are duplicates or no by checking alreadySeenSiteNumbersInCurrentContext
		for(Alert alert : openAlersObjects) {
			if (alert.isWasAlertAlreadyHandled()) { continue; }
			if (alreadySeenSiteNumbersInCurrentContext.containsKey(alert.getSiteNumber())) {
				alert.setAlertInitStatus(Constants.NEW_ALERT_INIT_STATUS_DUPLICATE);
				alert.setActiveAlert(false);
				alert.setInCaseOfDuplicateCurrentAlertId(alreadySeenSiteNumbersInCurrentContext.get(alert.getSiteNumber()));
				alert.setWasAlertAlreadyHandled(true);
			} else {
				alert.setAlertInitStatus(Constants.NEW_ALERT_INIT_STATUS_NEW);
				alert.setWasAlertAlreadyHandled(true);
				alreadySeenSiteNumbersInCurrentContext.put(alert.getSiteNumber(), alert.getAlarmIncidentNumber());
				
			}
		}
		// start new Thread for each new Alert (except exact duplicate by alarm incident number of an opened alert)
		for(Alert alert : openAlersObjects) {
			OpenAlertHandler openAlertHandler = new OpenAlertHandler(alert);
			openAlertHandler.start();
		}
		
	}
	
	// tries to get an open alert id from certain site. If not found return null indicating there is no open alert for this site
	private static String getOpenedAlertFromSite(String siteNumber, List<Alert> alreadyOpenedAlertsOnCurrentAlertsSites) {
		for (Alert alreadyOpenedAlert : alreadyOpenedAlertsOnCurrentAlertsSites) {
			if (alreadyOpenedAlert.getSiteNumber().equals(siteNumber)) {
				return alreadyOpenedAlert.getAlarmIncidentNumber();
			}
		}
		return null;
	}
	
	
}
