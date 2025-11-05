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

import com.kpmg.g1.api.business.AnsweredConversationThread;
import com.kpmg.g1.api.business.UnansweredConversationThread;
import com.kpmg.g1.api.dao.CallServiceDAOImplementation;
import com.kpmg.g1.api.objects.model.Conversation;
import com.kpmg.g1.api.utils.Constants;
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
	
	private static String localPathForDynamicAudioFiles = getLocalPathForDynamicAudioFiles();
	private static String localPathForStaticAudioFiles = getLocalPathForStaticAudioFiles();
	
	private static String getLocalPathForDynamicAudioFiles() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getString("dynamicSpeechFilesLocation");
		} catch (Exception e) {
			log.error("Failed to get local path to dynamic audio files " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	private static String getLocalPathForStaticAudioFiles() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getString("staticSpeechFilesLocation");
		} catch (Exception e) {
			log.error("Failed to get local path to static audio files " + ExceptionUtils.getStackTrace(e));
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
	@Path("/start/call/audio/wav/{filename}")
	@Produces("audio/wav")
	public Response startCallWavGetAudio(@PathParam("filename") String filename) {
		// Basic sanitization to avoid path traversal
		if (filename.contains("..") || !filename.endsWith(".wav")) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		File file = new File(localPathForDynamicAudioFiles, filename);
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
	
	@GET
	@Path("/start/call/audio/mp3/{filename}")
	@Produces("audio/mpeg")
	public Response startCallMp3GetAudio(@PathParam("filename") String filename) {
		// Basic sanitization to avoid path traversal
		if (filename.contains("..") || !filename.endsWith(".mp3")) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		File file = new File(localPathForDynamicAudioFiles, filename);
		if (!file.exists() || !file.isFile()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		try {
			InputStream inputStream = new FileInputStream(file);
			return Response.ok(inputStream).type("audio/mpeg").header("Content-Length", file.length())
					.header("Accept-Ranges", "bytes") // Optional but useful
					.build();
		} catch (IOException e) {
			log.error("getAudio: error occured while trying to server " + filename + " Error: " + ExceptionUtils.getStackTrace(e));
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("/get/static/audio/mp3/{filename}")
	@Produces("audio/mpeg")
	public Response getStaticAudioMp3(@PathParam("filename") String filename) {
		// Basic sanitization to avoid path traversal
		if (filename.contains("..") || !filename.endsWith(".mp3")) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		File file = new File(localPathForStaticAudioFiles, filename);
		if (!file.exists() || !file.isFile()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		try {
			InputStream inputStream = new FileInputStream(file);
			return Response.ok(inputStream).type("audio/mpeg").header("Content-Length", file.length())
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
			log.error("Falied to build conversation object in /vonage/answer with body " + requestBody + " event will be ignored");
			JSONArray ncco = new JSONArray().put(new JSONObject());
			return Response.status(200).entity(ncco.toString()).build();
		} else {
		  CallServiceDAOImplementation.insertConversation(conversationObject);
		  // continue business use case based on event status (only relevant events are timeout/hangup or answered)
		  if (requestBodyObj.optString("status", "").equals("timeout") || requestBodyObj.optString("status", "").equals("unanswered") ||
				  (requestBodyObj.optString("status", "").equals("completed") && requestBodyObj.optString("detail", "").equals("remote_busy"))
				  || (requestBodyObj.optString("status", "").equals("busy") && requestBodyObj.optString("detail", "").equals("remote_busy"))){
			  UnansweredConversationThread unansweredConversationThread = new UnansweredConversationThread(requestBodyObj.getString("uuid"));
			  unansweredConversationThread.start();
		  } else if (requestBodyObj.optString("status", "").equals("completed") && requestBodyObj.optString("detail", "").equals("ok")) {
			  // in case of a completed call that was unanswered or timeout it will be handled by the above if clause. Treat only answered calls
			  String isConversationConsideredAsAnswered = checkCompletedConversationForContactResponse(conversationObject);
			  if (isConversationConsideredAsAnswered.equals(Constants.CONVERSATION_NO_ANSWER_LONG_CALL_STATUS) ||
					  isConversationConsideredAsAnswered.equals(Constants.CONVERSATION_APPROVED_STATUS) || isConversationConsideredAsAnswered.equals(Constants.CONVERSATION_TRANSFER_STATUS)) {
				  AnsweredConversationThread answeredConversationThread = new AnsweredConversationThread(conversationObject.getUuid()
						  , isConversationConsideredAsAnswered, conversationObject.getToNo(), conversationObject.getDuration());
				  answeredConversationThread.start();
			  } else {
				  UnansweredConversationThread unansweredConversationThread = new UnansweredConversationThread(conversationObject.getUuid());
				  unansweredConversationThread.start();
			  }
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
		JSONArray ncco = new JSONArray();
		try {
			System.out.println("in event");
			JSONObject requestBodyObj = new JSONObject(requestBody);
			System.out.println(requestBodyObj.toString(2));
			Conversation conversationObject = Utils.buildConversationObjectFromVonageEvent(requestBodyObj);
			if (conversationObject == null) {
				log.error("In Vonage Event: received request body which could not be converted to conversation object. Will not proceed with call! data: " + requestBodyObj.toString());
				ncco.put(new JSONObject());
				return Response.status(500).entity(ncco.toString()).build();
			}
			if (requestBodyObj.has("uuid") && requestBodyObj.isNull("uuid")) {
				if (requestBodyObj.has("dtmf") && requestBodyObj.getJSONObject("dtmf").getBoolean("timed_out")) {
					conversationObject.setStatus("dtmf:noAnswer");
					// in case number of responses for this conversation is too high conclude it
					int numberOfResponsesInThisConversation = CallServiceDAOImplementation.getNumberOfResponsesPerConversation(conversationObject.getUuid(), conversationObject.getConversationId());
					if (numberOfResponsesInThisConversation <= JSONConfigurations.getInstance().getConfigurations().getInt("maxResponsesPerConversation")) {
						String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
								"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("noInput");
						String nccoStr = Constants.VONAGE_CALL_FOR_ACTION_NCCO_EVENT
								.replace("$vonageStreamUrl$", fileToStreamEndpoint)
								.replace("$vonageEventUrl$", JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("eventUrlEndpoint"))
								.replace("$clientResponseTimeout$", String.valueOf(JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getInt("clientResponseTimeoutSeconds")));
						ncco = new JSONArray(nccoStr);
					} else {
						log.info("Received too many responses: " + String.valueOf(numberOfResponsesInThisConversation) + " for conversation with uuid: " + conversationObject.getUuid() +
								" and conversation id: " + conversationObject.getConversationId() + " with number: " + conversationObject.getFromNo() + ". Hanging the call");
						ncco.put(new JSONObject());
					}
				} else {
					log.error("In Vonage Event: Received unexpected event with null uuid and no dtmf with time out: " + requestBodyObj.toString());
					ncco.put(new JSONObject());
				}
			} else if(requestBodyObj.has("dtmf")) {
				// get the digit pressed by the user and act accordingly
				String pressedDigits = requestBodyObj.getJSONObject("dtmf").getString("digits");
				conversationObject.setStatus("dtmf:" + pressedDigits);
				if (pressedDigits.equals("1")) {
					String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
							"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("goodbye");
					String nccoStr = Constants.VONAGE_GOODBYE_NCCO_EVENT
							.replace("$vonageStreamUrl$", fileToStreamEndpoint);
					ncco = new JSONArray(nccoStr);
				} else if (pressedDigits.equals("2")) {
					// check if transfer to dispatch should be a valid option
					JSONObject dispatchLocationAndAlarmEventId = CallServiceDAOImplementation.getAlarmCodeAndDispatchLocationByUuid(conversationObject.getUuid());
					if (dispatchLocationAndAlarmEventId == null) {
						// in case failed to get any information allow to send request to default number
						String dispatchPhoneNumber = JSONConfigurations.getInstance().getConfigurations().getString("defaultDispatchPhoneNumber");
						String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
								"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("transfer");
						JSONObject vonageObject = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage");
						String nccoStr = Constants.VONAGE_TRANSFER_CALL_NCCO_EVENT
								.replace("$vonageStreamUrl$", fileToStreamEndpoint)
								.replace("$vonageEventUrl$", vonageObject.getString("eventUrlEndpoint"))
								.replace("$transferCallRingTimeout$", String.valueOf(vonageObject.getInt("transferCallRingTimeout")))
								.replace("$dispactchNumber$", dispatchPhoneNumber);
						ncco = new JSONArray(nccoStr);
					} else {
						String alarmEventId = dispatchLocationAndAlarmEventId.getString("alarmEventId");
						// setting 
						boolean transferToDispatch = Utils.allowTransferToDispatchAllAlertsTime(alarmEventId, true);
						if (transferToDispatch) {
							String dispatchPhoneNumber = "";
							String dispatchLocationId = dispatchLocationAndAlarmEventId.getString("dispatchLocation");
							if (dispatchLocationId == null) {
								dispatchPhoneNumber = JSONConfigurations.getInstance().getConfigurations().getString("defaultDispatchPhoneNumber");
							} else {
								dispatchPhoneNumber = CallServiceDAOImplementation.getDispatchLocationNumber(dispatchLocationId);
								if (dispatchPhoneNumber == null) {
									dispatchPhoneNumber = JSONConfigurations.getInstance().getConfigurations().getString("defaultDispatchPhoneNumber");
								}
							}
							String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
									"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("transfer");
							JSONObject vonageObject = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage");
							String nccoStr = Constants.VONAGE_TRANSFER_CALL_NCCO_EVENT
									.replace("$vonageStreamUrl$", fileToStreamEndpoint)
									.replace("$vonageEventUrl$", vonageObject.getString("eventUrlEndpoint"))
									.replace("$transferCallRingTimeout$", String.valueOf(vonageObject.getInt("transferCallRingTimeout")))
									.replace("$dispactchNumber$", dispatchPhoneNumber);
							ncco = new JSONArray(nccoStr);
						} else {
							int numberOfResponsesInThisConversation = CallServiceDAOImplementation.getNumberOfResponsesPerConversation(conversationObject.getUuid(), conversationObject.getConversationId());
							if (numberOfResponsesInThisConversation <= JSONConfigurations.getInstance().getConfigurations().getInt("maxResponsesPerConversation")) {
								String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
										"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("invalidOption");
								String nccoStr = Constants.VONAGE_CALL_FOR_ACTION_NCCO_EVENT
										.replace("$vonageStreamUrl$", fileToStreamEndpoint)
										.replace("$vonageEventUrl$", JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("eventUrlEndpoint"))
										.replace("$clientResponseTimeout$", String.valueOf(JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getInt("clientResponseTimeoutSeconds")));
								ncco = new JSONArray(nccoStr);
							} else {
								log.info("Received too many responses: " + String.valueOf(numberOfResponsesInThisConversation) + " for conversation with uuid: " + conversationObject.getUuid() +
										" and conversation id: " + conversationObject.getConversationId() + " with number: " + conversationObject.getFromNo() + ". Hanging the call");
								ncco.put(new JSONObject());
							}
						}
					}
				} else if (pressedDigits.equals("9")) {
					int numberOfResponsesInThisConversation = CallServiceDAOImplementation.getNumberOfResponsesPerConversation(conversationObject.getUuid(), conversationObject.getConversationId());
					if (numberOfResponsesInThisConversation <= JSONConfigurations.getInstance().getConfigurations().getInt("maxResponsesPerConversation")) {
						// get original file to replay it
						String speechFileLocation = CallServiceDAOImplementation.getSpeechFileLocationByVonageUUID(conversationObject.getUuid());
						if (speechFileLocation == null) {
							log.error("Could not find speech file for play on repeat by uuid: " + conversationObject.getUuid() + " with conversation: " + conversationObject.getConversationId()
							+ " with phone number:" + conversationObject.getToNo() + ". hanging up the call");
							conversationObject.setStatus("dtmf:noAnswer");
							ncco.put(new JSONObject());
						} else {
							java.nio.file.Path pathToSpeechFileObject = Paths.get(speechFileLocation);
					        String fileName = pathToSpeechFileObject.getFileName().toString();
							String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlEndpoint") + "/" + fileName;
							String nccoStr = Constants.VONAGE_CALL_FOR_ACTION_NCCO_EVENT
									.replace("$vonageStreamUrl$", fileToStreamEndpoint)
									.replace("$vonageEventUrl$", JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("eventUrlEndpoint"))
									.replace("$clientResponseTimeout$", String.valueOf(JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getInt("clientResponseTimeoutSeconds")));
							ncco = new JSONArray(nccoStr);
						}
					} else {
						log.info("Received too many responses: " + String.valueOf(numberOfResponsesInThisConversation) + " for conversation with uuid: " + conversationObject.getUuid() +
								" and conversation id: " + conversationObject.getConversationId() + " with number: " + conversationObject.getFromNo() + ". Hanging the call");
						ncco.put(new JSONObject());
					}
				} else {
					int numberOfResponsesInThisConversation = CallServiceDAOImplementation.getNumberOfResponsesPerConversation(conversationObject.getUuid(), conversationObject.getConversationId());
					if (numberOfResponsesInThisConversation <= JSONConfigurations.getInstance().getConfigurations().getInt("maxResponsesPerConversation")) {
						String fileToStreamEndpoint = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("streamUrlStaticEndpoint") + 
								"/" + JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getJSONObject("staticFilesNames").getString("invalidOption");
						String nccoStr = Constants.VONAGE_CALL_FOR_ACTION_NCCO_EVENT
								.replace("$vonageStreamUrl$", fileToStreamEndpoint)
								.replace("$vonageEventUrl$", JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getString("eventUrlEndpoint"))
								.replace("$clientResponseTimeout$", String.valueOf(JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage").getInt("clientResponseTimeoutSeconds")));
						ncco = new JSONArray(nccoStr);
					} else {
						log.info("Received too many responses: " + String.valueOf(numberOfResponsesInThisConversation) + " for conversation with uuid: " + conversationObject.getUuid() +
								" and conversation id: " + conversationObject.getConversationId() + " with number: " + conversationObject.getFromNo() + ". Hanging the call");
						ncco.put(new JSONObject());
					}
				}
			} else {
				log.error("In Vonage Event. Received unexpected event: " + requestBodyObj.toString());
				ncco.put(new JSONObject());
			}
			CallServiceDAOImplementation.insertConversation(conversationObject);
			return Response.status(200).entity(ncco.toString()).build();
		} catch (Exception e) {
			log.error("Error occured In event with body " + requestBody + " Error: " + e.getMessage());
			ncco.put(new JSONObject());
			return Response.status(200).entity(ncco.toString()).build();
		}
	}
	
	private static String checkCompletedConversationForContactResponse(Conversation conversation) {
		// try to get conversation details from Database and check for dtmf events indicating answers
		String completedConversationAnsweredStatus = CallServiceDAOImplementation.getAnsweredConversationStatus(conversation.getUuid(), conversation.getConversationId());
		if (completedConversationAnsweredStatus == null || completedConversationAnsweredStatus.isEmpty()) {
			// no answer was found check if conversation was long enough to consider valid response
			if (conversation.getDuration() < JSONConfigurations.getInstance().getConfigurations()
					.optInt("considerConversationAsApprovedIfLongerThanInSeconds", Constants.DEFAULT_CONVERSATION_AS_ANSWERED_IN_SECONDS)) {
				return Constants.CONVERSATION_NO_ANSWER_STATUS;
			} else {
				return Constants.CONVERSATION_NO_ANSWER_LONG_CALL_STATUS;
			}
		} else {
			return completedConversationAnsweredStatus;
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
