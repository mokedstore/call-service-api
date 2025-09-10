package com.kpmg.g1.api.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kpmg.g1.api.utils.JSONConfigurations;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/mock")
public class MockService {
	
	final static Logger log = LogManager.getLogger(MockService.class.getName());

	@Path("/api/alerts")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlerts() {
        JSONArray response = new JSONArray(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONArray("alerts").toString());
        return Response.status(200).entity(response.toString()).build();
    }
	
	@Path("/api/write-event")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeEvent(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
			log.info(requestBodyJson);
		} catch (Exception e) {
			log.warn("writeEvent: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
		JSONObject response = new JSONObject(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONObject("writeEvent").toString());
        return Response.status(200).entity(response.toString()).build();
    }
	
	@Path("/api/calllist")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCallList(@QueryParam("site_no") String siteNumber, @QueryParam("system_no") String systemNumber,
			@QueryParam("zone_id") String zoneId) {
		log.info("Received get call list request with values - site: " + siteNumber + ", System: " + systemNumber + ", zone: " + zoneId);
		JSONObject responseObject = new JSONObject(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONObject("callList").toString());
		return Response.status(200).entity(responseObject.toString()).build();
	}
	
	@Path("/api/send-message")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(String requestBody) {
		JSONObject requestBodyJson = null;
		JSONObject responseBodyJson = new JSONObject();
		try {
			requestBodyJson = new JSONObject(requestBody);
			log.info(requestBodyJson);
		} catch (Exception e) {
			log.warn("sendMessage: received invalid request body. " + requestBody + " Only JSON is supported. " + ExceptionUtils.getStackTrace(e));
			responseBodyJson.put("error", "BAD_REQUEST").put("message", "Invalid request body. Only JSON is supported");
			 return Response.status(400).entity(responseBodyJson.toString()).build();
			
		}
		JSONObject response = null;
		if (requestBodyJson.getBoolean("send_message")) {
			 response = new JSONObject(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONObject("sendMessageTrue").toString());
		} else {
			response = new JSONObject(JSONConfigurations.getInstance().getConfigurations().getJSONObject("mock").getJSONObject("sendMessageFalse").toString());
		}
        return Response.status(200).entity(response.toString()).build();
    }
	
}
