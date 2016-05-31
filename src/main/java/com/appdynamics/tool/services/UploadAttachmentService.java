package com.appdynamics.tool.services;

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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import com.appdynamics.tool.app.Creds;

@Path("uploadattachment")
public class UploadAttachmentService {
	@GET
	@Produces("application/json")
	public String uploadAttachment(@QueryParam("url") String url, @QueryParam("issuekey") String issueKey) {
		try {
			String filepath = downloadImage(url, issueKey, new File("").getAbsolutePath());
			Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
			final FileDataBodyPart filePart = new FileDataBodyPart("file", new File(filepath));
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.bodyPart(filePart);

			final WebTarget target = client
					.target("https://singularity.jira.com/rest/api/2/issue/" + issueKey + "/attachments");
			final Response response = target.request().header("X-Atlassian-Token", "no-check")
					.header("Authorization", Creds.authorizationString)
					.post(Entity.entity(multipart, multipart.getMediaType()));

			// Use response object to verify upload success
			System.out.println("Attachment upload status: " + response.getStatus());

			formDataMultiPart.close();
			multipart.close();
			return "Attachment uploaded successfully";
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to upload attachment";
		}
	}

	private String downloadImage(String sourceUrl, String issueKey, String targetDirectory)
			throws MalformedURLException, IOException, FileNotFoundException {
		URL imageUrl = new URL(sourceUrl);
		String filepath = targetDirectory + File.separator + issueKey + "_" + getName(sourceUrl);
		try (InputStream imageReader = new BufferedInputStream(imageUrl.openStream()); OutputStream imageWriter = new BufferedOutputStream(new FileOutputStream(filepath));) {
			int readByte;
			while ((readByte = imageReader.read()) != -1) {
				imageWriter.write(readByte);
			}
		}
		return filepath;
	}
	
	private String getName(String filename) {
		final int lastUnixPos = filename.lastIndexOf("/");
        final int lastWindowsPos = filename.lastIndexOf("\\");
        return filename.substring(Math.max(lastUnixPos, lastWindowsPos) + 1);
	}
}
