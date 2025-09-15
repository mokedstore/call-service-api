package com.kpmg.g1.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Conversation;
import com.kpmg.g1.api.utils.JSONConfigurations;
import com.kpmg.g1.api.utils.Utils;
import com.vonage.jwt.Jwt;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/vonage")
public class Vonage {
	
	final static Logger log = LogManager.getLogger(Vonage.class.getName());
	
	private static String localPathForWAVFiles = getLocalPathForWAVFiles();
	
	private static String getLocalPathForWAVFiles() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getString("speechFilesLocation");
		} catch (Exception e) {
			log.error("Failed to get local path for WAV files " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	@Path("/generate/token")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateTokenSVC() {
        JSONObject response = new JSONObject();
        
    	String token = generateToken();
    	if (token != null) {
    		response.put("jwt", token);
    		return Response.status(200).entity(response.toString()).build();
    	}
    	response.put("error", "INTERNAL_SERVER_ERROR").put("message", "Failed to generate token for vonage");
        return Response.status(500).entity(response.toString()).build();
    }
	
	@GET
	@Path("/start/call/audio/{filename}")
	@Produces("audio/wav")
	public Response startCallGetAudio(@PathParam("filename") String filename) {
		// Basic sanitization to avoid path traversal
		if (filename.contains("..") || !filename.endsWith(".wav")) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		File file = new File(localPathForWAVFiles, filename);
		if (!file.exists() || !file.isFile()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		try {
			InputStream inputStream = new FileInputStream(file);
			return Response.ok(inputStream).type("audio/wav").header("Content-Length", file.length())
					.header("Accept-Ranges", "bytes") // Optional but useful
					.build();
		} catch (IOException e) {
			log.error("getAudio: error occured while trying to server " + filename + " Error: " + ExceptionUtils.getStackTrace(e));
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@Path("/answer")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response answer(String requestBody) {
		System.out.println("I was Answered");
		JSONObject requestBodyObj = new JSONObject(requestBody);
		System.out.println(requestBodyObj.toString(2));
		Conversation conversationObject = Utils.buildConversationObjectFromVonageEvent(requestBodyObj);
		if (conversationObject == null) {
			// TODO: Get alert object using uuid and update its status 
		} else {
		  CallServiceDAOImplementation.insertConversation(conversationObject);
		  // continue business use case based on event status (only relevant events are timeout/hangup or answered)
		  if (requestBodyObj.optString("status", "").equals("timeout") || requestBodyObj.optString("status", "").equals("unanswered")) {
			  
		  } else if (requestBodyObj.optString("status", "").equals("answered")) {
			  
		  }
		}
		JSONArray ncco = new JSONArray().put(new JSONObject());
		return Response.status(200).entity(ncco.toString()).build();
	}

	@Path("/event")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response eventApi(String requestBody) {
		JSONObject response = null;
		try {
			System.out.println("in event");
			JSONObject requestBodyObj = new JSONObject(requestBody);
			System.out.println(requestBodyObj.toString(2));

//			String nccoStr = "[\n" + "	{\n" + "	  \"action\": \"talk\",\n" + "	  \"text\": \"You pressed 2\"\n"
//					+ "	},\n" + "	{\n" + "	  \"action\": \"input\",\n" + "	  \"dtmf\": {\n"
//					+ "		\"maxDigits\": 2,\n" + "		\"timeout\": 5\n" + "	 }\n" + "}]";
//			JSONArray ncco = new JSONArray(nccoStr);
			JSONArray ncco = new JSONArray().put(new JSONObject());
			return Response.status(200).entity(ncco.toString()).build();
		} catch (Exception e) {
			log.error("Error occured In event with body " + requestBody + " Error: " + e.getMessage());
			response = new JSONObject().put("status", "internal service error.");
			return Response.status(500).entity(response.toString()).build();
		}
	}
	
	private static String generateToken() {
		try {
			JSONObject vonageObject = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage");
			ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
			ZonedDateTime thirtyMinsFromNow = nowUtc.plusMinutes(vonageObject.getLong("jwtValidInMinutes"));
        	String token = Jwt.builder()
            	    .applicationId(vonageObject.getString("applicationId"))
            	    .privateKeyPath(Paths.get(vonageObject.getString("privateKeyFilePath")))
            	    .issuedAt(nowUtc)
            	    .expiresAt(thirtyMinsFromNow)
            	    .addClaim("acl", Map.of(
            	        "paths", Map.of(
            	            "/*/calls/**", Map.of()
            	        )
            	    ))
            	    .build()
            	    .generate();
        	return token;
        } catch(Exception e) {
        	log.error("generateToken: Failed to get token for Vonage: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
	}
	
}
