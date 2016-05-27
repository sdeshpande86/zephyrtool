package com.appdynamics.tool.jwt;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import java.io.UnsupportedEncodingException;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import com.google.gson.Gson;

public class JwtBuilder {
	public static String generateJWTToken(String requestUrl, String canonicalUrl, String key, String sharedSecret)
			throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
		JwtClaims claims = new JwtClaims();
		claims.setIss(key);
		claims.setIat(System.currentTimeMillis() / 1000L);
		claims.setExp(claims.getIat() + 180L);

		claims.setQsh(getQueryStringHash(canonicalUrl));
		String jwtToken = sign(claims, sharedSecret);
		return jwtToken;
	}

	private static String sign(JwtClaims claims, String sharedSecret)
			throws InvalidKeyException, NoSuchAlgorithmException {
		String signingInput = getSigningInput(claims, sharedSecret);
		String signed256 = signHmac256(signingInput, sharedSecret);
		return signingInput + "." + signed256;
	}

	private static String getSigningInput(JwtClaims claims, String sharedSecret)
			throws InvalidKeyException, NoSuchAlgorithmException {
		JwtHeader header = new JwtHeader();
		header.alg = "HS256";
		header.typ = "JWT";
		Gson gson = new Gson();
		String headerJsonString = gson.toJson(header);
		String claimsJsonString = gson.toJson(claims);
		String signingInput = encodeBase64URLSafeString(headerJsonString.getBytes()) + "."
				+ encodeBase64URLSafeString(claimsJsonString.getBytes());
		return signingInput;
	}

	private static String signHmac256(String signingInput, String sharedSecret)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey key = new SecretKeySpec(sharedSecret.getBytes(), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		return encodeBase64URLSafeString(mac.doFinal(signingInput.getBytes()));
	}

	private static String getQueryStringHash(String canonicalUrl)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(canonicalUrl.getBytes("UTF-8"));
		byte[] digest = md.digest();
		return encodeHexString(digest);
	}
}