package com.kpmg.g1.api.business;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;
import com.kpmg.g1.api.utils.Constants;
import com.kpmg.g1.api.utils.JSONConfigurations;
import com.kpmg.g1.api.utils.Utils;

public class AlertsOpenForTooLongThread extends Thread {
	
	final static Logger log = LogManager.getLogger(AlertsOpenForTooLongThread.class.getName());
	private static boolean getAlertsLoop = true;
	private static boolean isAlertLoopMaster = checkAlertLoopMaster();
	
	@Override
	public void run() {
		if (isAlertLoopMaster) {
			log.info("Found that this instance is master for open alerts for too long handling");
			while(getAlertsLoop) {
				try {
					Thread.sleep(JSONConfigurations.getInstance().getConfigurations().getLong("waitTimeBetweenCheckAlertOpenForTooLongInMilliseconds"));
				} catch(Exception e) {
					log.error("Error occurred while trying to sleep in AlertsFetcherThread between alert calls. " + ExceptionUtils.getStackTrace(e));
				}
				List<Alert> alertOpenForTooLong = CallServiceDAOImplementation.getAlertsOpenForTooLong();
				if (alertOpenForTooLong == null || alertOpenForTooLong.size() == 0) {
					return;
				}
				for (Alert openAlert : alertOpenForTooLong) {
					openAlert.addProgressMessage(Utils.getTimestampFromDate(null), Constants.LOG_LEVEL_INFO, "Alert is open for too long without update. declaring it as closed.");
					openAlert.setActiveAlert(false);
					openAlert.setAlertHandlingStatusCode(Constants.OPEN_FOR_TOO_LONG);
					openAlert.setAlertHandlingStatusMessage("open for too long");
					openAlert.setUpdatedAt(Utils.getTimestampFromDate(null));
					CallServiceDAOImplementation.upsertAlert(openAlert);
				}
			}
			log.info("AlertsOpenForTooLongThread was gracefully terminated");
		} else {
			log.info("Found that this instance is not master for alerts open for too long handling");
		}
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

}
