package org.jenkinsci.plugins.repoclient.client;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * http://localhost:8081/nexus/nexus-core-documentation-plugin/core/docs/index.
 * html
 * 
 * @author Marcel Birkner
 * @author Elimane GUEYE elimane.gueye@gmail.com
 */
public class NexusTestClient {

	private static Logger log = Logger.getLogger(NexusTestClient.class
			.getSimpleName());

	// private static String user = "refadm";
	// private static String password = "refadm123";
	private static String user = "";
	private static String password = "";
	//private static String url = "https://std.le500.loto-quebec.com/nexus/";
	private static String url = "https://lcom501d/nexus/";

	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {
		NexusTestClient client = new NexusTestClient();
		// client.run0();
		//client.search("jsf-api");
		client.search("booking-web");
	}

	public NexusTestClient() {

	}

	private void searchFull(String key) throws JsonParseException,
	JsonMappingException, IOException {

List<String> artifactVersions;
try {

	WebResource service = getService();
	log.info("Retrieving Nexus server status... :" + url);
	String nexusStatus = service.path("service").path("local")
			.path("status").accept(MediaType.APPLICATION_JSON)
			.get(ClientResponse.class).toString();

	if (!nexusStatus.contains("200 OK")) {
		log.severe("Unable to get Nexus status: " + nexusStatus);
		return;
	}
	log.info("Performing search on " + key + " against Nexus server "
			+ url);
	String searchResult = service.path("service").path("local")
			.path("lucene").path("search").queryParam("a", key)
			.accept(MediaType.APPLICATION_JSON).get(String.class)
			.toString();
	//log.info(searchResult);

	if ((searchResult != null) && (!searchResult.isEmpty())
			&& (searchResult.startsWith("{"))) {

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(searchResult
				.getBytes());
		JsonNode idNode = rootNode.path("totalCount");
		log.info("Found " + idNode.asInt() + " results for key "+ key+ " against Nexus server "
				+ url);
		JsonNode data = rootNode.path("data");
		artifactVersions = data.findValuesAsText("version");
		Iterator<String> it = artifactVersions.iterator();
		while (it.hasNext()) {
			System.out.println(">>>> List<String> = " + it.next());
		}
		// /com/lq/config/config-file/0.0.9-SNAPSHOT/config-file-0.0.9-20140117.162128-2.jar
		/*
		  Iterator<JsonNode> elements = data.getElements();
		  while(elements.hasNext()){ 
			  JsonNode currentDataNode =elements.next(); 
			  String groupid =
			  currentDataNode.path("groupId").asText(); String artifactid =
			  currentDataNode.path("artifactId").asText(); String version =
			  currentDataNode.path("version").asText();
			  
			  System.out.println("*** DATA/groupid = "+ groupid);
			  System.out.println("*** DATA/artifactid = "+ artifactid);
			  System.out.println("*** DATA/version = "+ version);
			  artifactVersions.add(version);
			}
			*/
		 
	}
} catch (JsonGenerationException e) {

	e.printStackTrace();

} catch (JsonMappingException e) {

	e.printStackTrace();

} catch (IOException e) {

	e.printStackTrace();

}
}
	private void search(String key) throws JsonParseException,
			JsonMappingException, IOException {

		List<String> artifactVersions;
		try {

			WebResource service = getService();
			log.info("Retrieving Nexus server status... :" + url);
			String nexusStatus = service.path("service").path("local")
					.path("status").accept(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class).toString();

			if (!nexusStatus.contains("200 OK")) {
				log.severe("Unable to get Nexus status: " + nexusStatus);
				return;
			}
			log.info("Performing search on " + key + " against Nexus server "
					+ url);
			String searchResult = service.path("service").path("local")
					.path("lucene").path("search").queryParam("a", key)
					.accept(MediaType.APPLICATION_JSON).get(String.class)
					.toString();
			//log.info(searchResult);

			if ((searchResult != null) && (!searchResult.isEmpty())
					&& (searchResult.startsWith("{"))) {

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(searchResult
						.getBytes());
				JsonNode idNode = rootNode.path("totalCount");
				log.info("Found " + idNode.asInt() + " results for key "+ key+ " against Nexus server "
						+ url);
				JsonNode data = rootNode.path("data");
				artifactVersions = data.findValuesAsText("version");
				Iterator<String> it = artifactVersions.iterator();
				while (it.hasNext()) {
					System.out.println(">>>> List<String> = " + it.next());
				}
				// /com/lq/config/config-file/0.0.9-SNAPSHOT/config-file-0.0.9-20140117.162128-2.jar
				/*
				  Iterator<JsonNode> elements = data.getElements();
				  while(elements.hasNext()){ 
					  JsonNode currentDataNode =elements.next(); 
					  String groupid =
					  currentDataNode.path("groupId").asText(); String artifactid =
					  currentDataNode.path("artifactId").asText(); String version =
					  currentDataNode.path("version").asText();
					  
					  System.out.println("*** DATA/groupid = "+ groupid);
					  System.out.println("*** DATA/artifactid = "+ artifactid);
					  System.out.println("*** DATA/version = "+ version);
					  artifactVersions.add(version);
					}
					*/
				 
			}
		} catch (JsonGenerationException e) {

			e.printStackTrace();

		} catch (JsonMappingException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	private WebResource getService() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		if (user != null && !user.isEmpty() && password != null
				&& !password.isEmpty())
			client.addFilter(new HTTPBasicAuthFilter(user, password));
		return client.resource(getBaseURI());
	}

	private URI getBaseURI() {
		return UriBuilder.fromUri(url).build();
	}
}
