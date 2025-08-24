package com.kpmg.g1.api.utils;

public class Constants {
	public final static String CONFIGURATION_DIR_PATH = "CALL_SERVICE_CONFIGURATION_DIR_PATH";
	public final static String CONFIGURATION_FILE_PATH = "CALL_SERVICE_CONFIGURATION_FILE_PATH";
	public final static long KILL_THREADS_WAIT_TIME_IN_MILLIS = 3000l;
	
	public final static String SQL_COLUMN_KID = "kId";
	
	public final static String GET_KID_BY_VONAGE_UUID = "SELECT kId FROM Alerts WHERE vonageCurrentConversationId = ?";
}
