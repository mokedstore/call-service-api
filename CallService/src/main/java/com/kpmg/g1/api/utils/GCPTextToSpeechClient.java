package com.kpmg.g1.api.utils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;

public class GCPTextToSpeechClient {
	
	final static Logger log = LogManager.getLogger(GCPTextToSpeechClient.class.getName());
	
	private static String localPathForDynamicAudioFiles = getLocalPathForDynamicAudioFiles();
	private static GoogleCredentials  credentials = getGoogleCredentials();
	
	private static GoogleCredentials getGoogleCredentials () {
		try {
			JSONObject gcpConf = JSONConfigurations.getInstance().getConfigurations().getJSONObject("gcpTextToSpeech");
			GoogleCredentials googleCredentials = GoogleCredentials
		            .fromStream(new FileInputStream(gcpConf.getString("credentialsFileLocation")))
		            .createScoped(Collections.singleton(gcpConf.getString("googleAuthScope")));
			return googleCredentials;
		} catch (Exception e) {
			log.error("getCredentials: Error Occurred while trying to get google credentials: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static String getAccessToken() {
		try {
			if (credentials == null) {
				log.error("getAccessToken: Could not get token since credentials object is null");
				return null;
			}
			credentials.refreshIfExpired();
			return credentials.getAccessToken().getTokenValue();
		} catch (Exception e) {
			log.error("getAccessToken: Failed to get access token: " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	private static String getLocalPathForDynamicAudioFiles() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getString("dynamicSpeechFilesLocation");
		} catch (Exception e) {
			log.error("Failed to get local path for dynamic audio files " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONObject convertTextToSpeech(String textAsSSML) {

	    JSONObject responseObj = new JSONObject();
	    CloseableHttpClient client = null;
	    CloseableHttpResponse response = null;

	    try {
	    	JSONObject gcpConf = JSONConfigurations.getInstance().getConfigurations().getJSONObject("gcpTextToSpeech");
	        client = HttpClientBuilder.create().build();
	        HttpPost post = new HttpPost(gcpConf.getString("url"));

	        String accessToken = getAccessToken();
	        if (accessToken == null) {
	        	try {
	        		Thread.sleep(1000);
	        		credentials = getGoogleCredentials();
	        		accessToken = getAccessToken();
	        		if (accessToken == null) {
	        			responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to get GCP token");
	        		}
	        	} catch (Exception e) {}
	        }
	        
	        post.setHeader("Authorization", "Bearer " + accessToken);
	        post.setHeader("Content-Type", "application/json");

	        JSONObject requestBody = new JSONObject();

	        requestBody.put("input", new JSONObject()
	                .put("ssml", textAsSSML));

	        requestBody.put("voice", new JSONObject()
	                .put("languageCode", gcpConf.getString("voiceLanguageCode"))
	                .put("name", gcpConf.getString("voiceName")));

	        requestBody.put("audioConfig", new JSONObject()
	                .put("audioEncoding", gcpConf.getString("audioEncoding"))
	                .put("sampleRateHertz", gcpConf.getInt("sampleRateHertz")));

	        post.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

	        response = client.execute(post);

	        if (response.getStatusLine().getStatusCode() == 200) {
	            String json = EntityUtils.toString(response.getEntity());
	            JSONObject obj = new JSONObject(json);

	            String audioContentBase64 = obj.getString("audioContent");
	            try {
	            	byte[] audioBytes = Base64.getDecoder().decode(audioContentBase64);
		            Path outputPath = Paths.get(
		            		localPathForDynamicAudioFiles,
		                UUID.randomUUID().toString() + ".mp3"
		            );
		            Files.write(outputPath, audioBytes);
		            responseObj.put("path", outputPath);
	            } catch (Exception ex) {
	            	log.error("convertTextToSpeech: Failed to write MP3 file to disk. Error: " + ExceptionUtils.getStackTrace(ex));
	    			responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to write file to disk");
	            }
	        } else {
	        	log.error("convertTextToSpeech: received unexpected status when trying to convert " + textAsSSML + " to MP3 file using Google Cloud Services. status: " + 
	        			String.valueOf(response.getStatusLine().getStatusCode()) + " response body: " + EntityUtils.toString(response.getEntity()));
				responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to convert text to audio file using Google Text to Speech Service");
	        }

	    } catch (Exception e) {
	    	log.error("convertTextToSpeech: Failed to convert " + textAsSSML + " to MP3 file using Google Cloud Services. Error: " + ExceptionUtils.getStackTrace(e));
			responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to convert text to audio file using Google Text to Speech Service");
	    } finally {
	        try {
	            if (response != null) response.close();
	            if (client != null) client.close();
	        } catch (Exception e) {
	        	log.error("convertTextToSpeech: Failed to close client or response " + ExceptionUtils.getStackTrace(e));
	        }
	    }
	    return responseObj;
	}

}
