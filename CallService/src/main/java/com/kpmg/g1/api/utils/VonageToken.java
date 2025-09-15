package com.kpmg.g1.api.utils;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.vonage.jwt.Jwt;

public class VonageToken {
	
	final static Logger log = LogManager.getLogger(VonageToken.class.getName());
	
	private static VonageToken instance;
	private String tokenValue;
	private ZonedDateTime tokenExpirationDate;
	
	private VonageToken(String tokenValue, ZonedDateTime tokenExpirationDate) {
		this.tokenValue = tokenValue;
		this.tokenExpirationDate = tokenExpirationDate;
	}
	
	public static VonageToken getInstance() {
		if (instance == null || instance.getTokenExpirationDate().isAfter(ZonedDateTime.now(ZoneId.of("UTC")))) {
			VonageToken newTokenData = generateToken();
			if (newTokenData == null) {
				return null;
			}
			instance = new VonageToken(newTokenData.getTokenValue(), newTokenData.getTokenExpirationDate());
		}
		return instance;
	}
	
	public ZonedDateTime getTokenExpirationDate() {
		return tokenExpirationDate;
	}

	public void setTokenExpirationDate(ZonedDateTime tokenExpirationDate) {
		this.tokenExpirationDate = tokenExpirationDate;
	}

	public String getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}
	
	public static void manuallyRenewToken() {
		VonageToken newTokenData = generateToken();
		if (instance == null) {
			if (newTokenData == null) {
				return;
			}
			instance = new VonageToken(newTokenData.getTokenValue(), newTokenData.getTokenExpirationDate());
		} else {
			instance.setTokenValue(newTokenData.getTokenValue());
			instance.setTokenExpirationDate(newTokenData.getTokenExpirationDate());
		}
	}

	public static VonageToken generateToken() {
		try {
			log.info("Trying to generate new token for Vonage..");
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
        	return new VonageToken(token, thirtyMinsFromNow);
        } catch(Exception e) {
        	log.error("generateToken: Failed to get token for Vonage: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
	}

}
