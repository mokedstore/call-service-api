package com.kpmg.g1.api.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.kpmg.g1.api.business.AlertsFetcherThread;
import com.kpmg.g1.api.business.AlertsOpenForTooLongThread;
import com.kpmg.g1.api.utils.AzureTextToSpeechClient;
//import com.kpmg.g1.api.utils.JSONConfigurations;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/action")
public class CallServiceMainService {
	
	final static Logger log = LogManager.getLogger(CallServiceMainService.class.getName());
	
	@Path("/ping")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkStatus() {
		JSONObject response = new JSONObject();
		log.info("ping was tested");
		response.put("status", "up and running");
		return Response.status(200).entity(response.toString()).build();
	}
	
	@Path("/manually/handle/alerts/open/for/too/long")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response manuallyHandleAlertsOpenForTooLong() {
		AlertsOpenForTooLongThread.handleOpenAlertsForTooLongManually();
		JSONObject response = new JSONObject();
		log.info("Received request to manually handle alerts open for too long");
		response.put("message", "sent request to manually handle alerts open for too long");
		return Response.status(200).entity(response.toString()).build();
	}
	
	@Path("/stop/alert/fetching")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopAlertFetching() {
		JSONObject response = new JSONObject();
		log.info("Received request to manually stop alerts fetching");
		if (AlertsFetcherThread.getGetAlertsLoop()) {
			AlertsFetcherThread.setIsAlertLoopMaster(false);
			AlertsFetcherThread.changeGetAlertLoop(false);
			response.put("message", "stopped alerts fetching");
			return Response.status(200).entity(response.toString()).build();
		} else {
			response.put("message", "alerts fetching is already stopped");
			return Response.status(200).entity(response.toString()).build();
		}
	}
	
	@Path("/start/alert/fetching")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response startAlertFetching() {
		JSONObject response = new JSONObject();
		log.info("Received request to manually start alerts fetching");
		if (!AlertsFetcherThread.getGetAlertsLoop()) {
			AlertsFetcherThread.setIsAlertLoopMaster(true);
			AlertsFetcherThread.changeGetAlertLoop(true);
			AlertsFetcherThread alertsFetcherThread = new AlertsFetcherThread();
			alertsFetcherThread.setName("alertsFetcher-Thread");
			alertsFetcherThread.start();
			response.put("message", "started alerts fetching");
			return Response.status(200).entity(response.toString()).build();
		} else {
			response.put("message", "alerts fetching is already running");
			return Response.status(200).entity(response.toString()).build();
		}
	}
	
	@Path("/text/to/speech")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response textToSpeech(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
		} catch (Exception e) {
			log.warn("textToSpeech: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
        String ssml = requestBodyJson.getString("ssml");
        try {
        	    //JSONObject textToSpeechResponse = new JSONObject(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONObject("textToSpeech").toString());
              JSONObject textToSpeechResponse = AzureTextToSpeechClient.convertTextToSpeech(ssml);
              if(textToSpeechResponse.has("error")) {
            	  return Response.status(500).entity(textToSpeechResponse.toString()).build();
              }
            return Response.status(200).entity(textToSpeechResponse.toString()).build();

        } catch (Exception e) {
            responseBodyJson = new JSONObject();
            responseBodyJson.put("error", "INTERNAL_SERVER_ERROR").put("message",
                    "Failed to convert text to speech. Check logs for more information");
            return Response.status(500).entity(responseBodyJson.toString()).build();
        }
    }

}
