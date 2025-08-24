package com.kpmg.g1.api.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


public class AzureTextToSpeechClient {
	
	final static Logger log = LogManager.getLogger(AzureTextToSpeechClient.class.getName());

	private static String AzureServicesKey = getAzureServicesKey();
	private static String AzureTextToSpeechUrl = getAzureTextToSpeechUrl();
	private static String localPathForWAVFiles = getLocalPathForWAVFiles();
	
	private static String getAzureServicesKey() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("azureTextToSpeech").getString("key");
		} catch (Exception e) {
			log.error("Failed to get Azure Services Key " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	private static String getLocalPathForWAVFiles() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getString("speechFilesLocation");
		} catch (Exception e) {
			log.error("Failed to get local path for WAV files " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	private static String getAzureTextToSpeechUrl() {
		try {
			return JSONConfigurations.getInstance().getConfigurations().getJSONObject("azureTextToSpeech").getString("url");
		} catch (Exception e) {
			log.error("Failed to get Azure text to speech url " + ExceptionUtils.getStackTrace(e));
			return null;
		}
	}
	
	public static JSONObject convertTextToSpeech(String textAsSSML) {
		JSONObject responseObj = new JSONObject();
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(AzureTextToSpeechUrl);
			
			post.setHeader("X-Microsoft-OutputFormat", JSONConfigurations.getInstance().getConfigurations().getJSONObject("azureTextToSpeech").getString("outuputFormat"));
			post.setHeader("Content-Type", "application/ssml+xml");
			post.setHeader("Ocp-Apim-Subscription-Key", AzureServicesKey);
			
			post.setEntity(new StringEntity(textAsSSML, StandardCharsets.UTF_8));
			
			response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					Path wavOutputPath = Paths.get(localPathForWAVFiles, UUID.randomUUID().toString() + ".wav");
                    try (InputStream inputStream = entity.getContent();
                        FileOutputStream outputStream = new FileOutputStream(wavOutputPath.toString())) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        responseObj.put("path", wavOutputPath);
                    } catch(Exception err) {
                    	log.error("convertTextToSpeech: Failed to convert " + textAsSSML + " to WAV file using Azure Web Services. Error: " + ExceptionUtils.getStackTrace(err));
                    	responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to write WAV file to disk");
                    }
                }
			}
		} catch(Exception e) {
			log.error("convertTextToSpeech: Failed to convert " + textAsSSML + " to WAV file using Azure Web Services. Error: " + ExceptionUtils.getStackTrace(e));
			responseObj.put("error", "INTERNAL_SERVICE_ERROR").put("message", "Failed to convert text to WAV file using Azure Text to Speech Service");
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (Exception e) {
				log.error("convertTextToSpeech: Failed to close client or response " + ExceptionUtils.getStackTrace(e));
			}
			
		}
		return responseObj;
	}
	
	
}
