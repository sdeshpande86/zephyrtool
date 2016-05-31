package com.appdynamics.tool.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class Test {
	public static void main(String args[]) {
		try {
			String filepath = downloadImage("https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg",
	                new File("").getAbsolutePath());
			Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
			final FileDataBodyPart filePart = new FileDataBodyPart("file",
					new File(filepath));
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.bodyPart(filePart);

			final WebTarget target = client.target("https://singularity.jira.com/rest/api/2/issue/ZEP-94/attachments");
			final Response response = target.request().header("X-Atlassian-Token", "no-check")
					.header("Authorization", Creds.authorizationString)
					.post(Entity.entity(multipart, multipart.getMediaType()));

			// Use response object to verify upload success
			System.out.println("Attachment upload status: " + response.getStatus());

			formDataMultiPart.close();
			multipart.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String downloadImage(String sourceUrl, String targetDirectory)
			throws MalformedURLException, IOException, FileNotFoundException {
		System.out.println(targetDirectory);
		URL imageUrl = new URL(sourceUrl);
		try (InputStream imageReader = new BufferedInputStream(imageUrl.openStream());
				OutputStream imageWriter = new BufferedOutputStream(
						new FileOutputStream(targetDirectory + File.separator + "downloadedimage.png"));) {
			int readByte;

			while ((readByte = imageReader.read()) != -1) {
				imageWriter.write(readByte);
			}
		}
		return targetDirectory + File.separator + "downloadedimage.png";
	}
}
