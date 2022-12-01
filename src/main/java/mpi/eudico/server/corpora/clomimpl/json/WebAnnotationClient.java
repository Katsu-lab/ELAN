package mpi.eudico.server.corpora.clomimpl.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.logging.Level;

import org.json.JSONObject;
import static mpi.eudico.server.corpora.util.ServerLogger.LOG;


/**
 * Client class to carry out the http requests to the annotation server 
 * The HttpClient is used to send requests and retrieve their responses.
 * 
 */
public class WebAnnotationClient {
	
	public static final String ACCEPT = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
	
	public static final String CONTENT_TYPE = "application/ld+json; profile=\"http://www.w3.org/ns/anno.jsonld\"";
	
	public static String URI ;
	
	public static final int HTTP_CREATED = 201;
			
	HttpClient httpClient = HttpClient.newBuilder()
	        .version(HttpClient.Version.HTTP_1_1)
	        .connectTimeout(Duration.ofSeconds(20))
	        .build();
	
	static {
		URI = System.getProperty("AnnotationServer.URL");
		if (URI == null || URI.isEmpty()) {
			//URI = "http://localhost:8080/annotation/w3c/";
			URI = "https://elucidate.dev.clariah.nl/annotation/w3c/";
		}
	}
	
	/**
	 * Annotation collection post request
	 * @param annotatioCollectionJson
	 * @return
	 */
	public String exportAnnotationCollection(String annotatioCollectionJson) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					  .uri(new URI(URI))
					  .headers("Accept", ACCEPT, "Content-Type", CONTENT_TYPE)
					  .POST(HttpRequest.BodyPublishers.ofString(annotatioCollectionJson))
					  .build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
						
			if (response.statusCode() == HTTP_CREATED) {
				JSONObject annotationCollectionResponse = new JSONObject(response.body());
				String collectionId = (String) annotationCollectionResponse.get("id");
				return collectionId;
			} else {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Unable to export tier or collection to server. The server responded with status code: " + response.statusCode());
				}
				return "";
			}
			
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when exporting tier to annotation server ");
			}
			return "";
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO error while connecting to annotation server " + e);
			}
			return "";
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when exporting tier to annotation server");
			}
			return "";
		}
		
	}
	
	/**
	 * annotation post request
	 * @param annotationJson
	 * @param collectionIDURI
	 * @return
	 */
	public String exportAnnotation(String annotationJson, String collectionIDURI) {
		
		try {
			HttpRequest request = HttpRequest.newBuilder()
							.uri(new URI(collectionIDURI))
							 .headers("Accept", ACCEPT, "Content-Type", CONTENT_TYPE)
							 .POST(HttpRequest.BodyPublishers.ofString(annotationJson))
							 .build();
			
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
									
			if (response.statusCode() == HTTP_CREATED) {
				JSONObject annotationResponse = new JSONObject(response.body());
				String annotationID = (String) annotationResponse.get("id");
				return annotationID;
			} else {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Unable to export annotation to server. The server responded with status code: " + response.statusCode());
				}
				return "";
			}
		} catch (URISyntaxException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "URI Syntax Exception when exporting annotation to annotation server ");
			}
			return "";
		} catch (IOException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "IO exception when exporting annotation to annotation server");
			}
			return "";
		} catch (InterruptedException e) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "InterruptedException when exporting annotation to annotation server");
			}
			return "";
		}
	}

}
