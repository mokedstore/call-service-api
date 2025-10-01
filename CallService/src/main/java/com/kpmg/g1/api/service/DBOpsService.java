package com.kpmg.g1.api.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Alert;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/db")
public class DBOpsService {
	
	final static Logger log = LogManager.getLogger(DBOpsService.class.getName());

	@Path("/get/kId/by/vonage/uuid")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKidByVonageUUID(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
		} catch (Exception e) {
			log.warn("getKidByVonageUUID: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
        String uuid = requestBodyJson.getString("uuid");
        try {
            String kId = CallServiceDAOImplementation.getKidByVonageUUID(uuid);
            if(kId == null) {
            	responseBodyJson.put("error", "NOT_FOUND").put("message", "Could not find matching kId for vonage uuid: " + uuid);
   			 	return Response.status(404).entity(responseBodyJson.toString()).build();
            }
            responseBodyJson.put("kId", kId);
            return Response.status(200).entity(responseBodyJson.toString()).build();

        } catch (Exception e) {
            responseBodyJson = new JSONObject();
            responseBodyJson.put("error", "INTERNAL_SERVER_ERROR").put("message",
                    "Failed to get kId. Check logs for more information");
            return Response.status(500).entity(responseBodyJson.toString()).build();
        }
    }
	
	@Path("/get/message/id/by/event/id")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessageIdByEventId(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
		} catch (Exception e) {
			log.warn("getMessageIdByEventId: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
        String eventId = requestBodyJson.getString("eventId");
        try {
            String messageId = CallServiceDAOImplementation.getMessageIdByEventIdForTextToSpeech(eventId);
            if(messageId == null) {
            	responseBodyJson.put("error", "NOT_FOUND").put("message", "Could not find matching messageId for eventId: " + eventId);
   			 	return Response.status(404).entity(responseBodyJson.toString()).build();
            }
            responseBodyJson.put("messageId", messageId);
            return Response.status(200).entity(responseBodyJson.toString()).build();

        } catch (Exception e) {
            responseBodyJson = new JSONObject();
            responseBodyJson.put("error", "INTERNAL_SERVER_ERROR").put("message",
                    "Failed to get messageId. Check logs for more information");
            return Response.status(500).entity(responseBodyJson.toString()).build();
        }
    }
	
	@Path("/get/open/alerts/by/site/numbers")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOpenAlertsBySiteNumbers(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
		} catch (Exception e) {
			log.warn("getOpenAlertsBySiteNumbers: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
        JSONArray siteNumbers = requestBodyJson.getJSONArray("siteNumbers");
        try {
        	Set<String> uniqueSiteNumbers = new HashSet<String>();
        	for (int i = 0; i < siteNumbers.length(); i++) {
        		uniqueSiteNumbers.add(siteNumbers.getString(i));
        	}
            List<Alert> openAlerts = CallServiceDAOImplementation.getOpenAlertsBySiteNumbers(uniqueSiteNumbers);
            for (Alert openAlert : openAlerts) {
            	System.out.println(openAlert.toString());
            }
            responseBodyJson.put("message", "ok");
            return Response.status(200).entity(responseBodyJson.toString()).build();

        } catch (Exception e) {
            responseBodyJson = new JSONObject();
            responseBodyJson.put("error", "INTERNAL_SERVER_ERROR").put("message",
                    "Failed to get open alerts. Check logs for more information");
            return Response.status(500).entity(responseBodyJson.toString()).build();
        }
    }
	
}
