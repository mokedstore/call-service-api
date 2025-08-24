package com.kpmg.g1.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.kpmg.g1.api.utils.JSONConfigurations;
import com.vonage.jwt.Jwt;

import jakarta.ws.rs.GET;
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
	@Path("/audio/{filename}")
	@Produces("audio/wav")
	public Response getAudio(@PathParam("filename") String filename) {
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
	
	public static String generateToken() {
		try {
			JSONObject vonageObject = JSONConfigurations.getInstance().getConfigurations().getJSONObject("vonage");
        	String token = Jwt.builder()
            	    .applicationId(vonageObject.getString("applicationId"))
            	    .privateKeyPath(Paths.get(vonageObject.getString("privateKeyFilePath")))
            	    .issuedAt(ZonedDateTime.now())
            	    .expiresAt(ZonedDateTime.now().plusMinutes(vonageObject.getInt("jwtValidInMinutes")))
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
