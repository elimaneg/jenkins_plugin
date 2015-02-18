package org.jenkinsci.plugins.repoclient.client;

import hudson.ProxyConfiguration;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jenkinsci.plugins.repoclient.RepositoryClientParameterDefinition;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * This class handles all communication with the Maven repository.
 * 
 * @author mrumpf
 * @author elimane.gueye@gmail.com
 * 
 */
public class MavenRepositoryClient {
	private static final String FILES_TO_IGNORE = "^maven-metadata.*$|^archetype-catalog.*$";
	private static final String NONE = "";
	private static final Pattern PATTERN = Pattern.compile(
			"href=[\n\r ]*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

	private static final Logger logger = Logger
			.getLogger(RepositoryClientParameterDefinition.class);

	private static java.util.logging.Logger log = java.util.logging.Logger
			.getLogger(MavenRepositoryClient.class.getSimpleName());

	/**
	 * Gets a list of version strings from a repository folder.
	 * 
	 * @param baseurl
	 *            the base url of the repository
	 * @param groupid
	 *            the group id which will be turned into a folder tree
	 * @param artifactid
	 *            the artifact id which is a folder
	 * @param username
	 *            the username for accessing the repository or null if the
	 *            repository is not protected
	 * @param password
	 *            the password for the user
	 * @return a list of version strings
	 */
	public static List<String> getVersions_orig(String baseurl, String groupid,
			String artifactid, String username, String password) {

		String url = concatUrl(baseurl, groupid, artifactid);

		logger.info("Getting versions from " + url);
		System.out.println("Getting versions from " + url);

		String responseBody = doHttpRequest(username, password, url);

		logger.info("Response " + responseBody);

		List<String> items = new ArrayList<String>();
		Content c = unmarshal(responseBody);
		if (c != null) {
			for (ContentItem ci : c.getContentItems()) {
				String ver = ci.getText();
				if (!ver.matches(FILES_TO_IGNORE)) {
					items.add(ver);
				}
			}
		} else {
			logger.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
			parseHtml(responseBody, items);
		}

		return sortVersions(items);
	}

	/**
	 * Gets a list of version strings from a repository folder.
	 * 
	 * @param baseurl
	 *            the base url of the repository
	 * @param groupid
	 *            the group id which will be turned into a folder tree
	 * @param artifactid
	 *            the artifact id which is a folder
	 * @param username
	 *            the username for accessing the repository or null if the
	 *            repository is not protected
	 * @param password
	 *            the password for the user
	 * @return a list of version strings
	 */

	public static List<String> getVersions(String baseurl,
			String groupid, String artifactid, String username, String password) {

		List<String> artifactVersions;
		try {
			System.out.println("########## DANS getVersions ###############");
			String key = artifactid;
			ClientConfig config = new DefaultClientConfig();
			//config.getProperties().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY,"true");
			//config.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY, true);

			Client client = Client.create(config);
			if (username != null && !username.isEmpty() && password != null
					&& !password.isEmpty())
				client.addFilter(new HTTPBasicAuthFilter(username, password));
			WebResource service = client.resource(UriBuilder.fromUri(baseurl)
					.build());

			log.info("Retrieving Nexus server status... :" + baseurl);
			System.out.println("Retrieving Nexus server status... :" + baseurl);
			String nexusStatus = service.path("service").path("local")
					.path("status").accept(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class).toString();

			if (!nexusStatus.contains("200 OK")) {
				log.severe("Unable to get Nexus status: " + nexusStatus);
				return null;
			}
			System.out.println("SEARCH");
			log.info("Performing search on " + key + " against Nexus server "
					+ baseurl);
			String searchResult = service.path("service").path("local")
					.path("lucene").path("search").queryParam("a", key)
					.accept(MediaType.APPLICATION_JSON).get(String.class)
					.toString();
			// log.info(searchResult);

			if ((searchResult != null) && (!searchResult.isEmpty())
					&& (searchResult.startsWith("{"))) {

				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(searchResult
						.getBytes());
				JsonNode idNode = rootNode.path("totalCount");
				log.info("Found " + idNode.asInt() + " results for key " + key
						+ " against Nexus server " + baseurl);
				JsonNode data = rootNode.path("data");
				artifactVersions = data.findValuesAsText("version");
				Iterator<String> it = artifactVersions.iterator();
				while (it.hasNext()) {
					System.out.println(">>>> List<String> = " + it.next());
				}
				// /com/lq/config/config-file/0.0.9-SNAPSHOT/config-file-0.0.9-20140117.162128-2.jar
				/*
				 * Iterator<JsonNode> elements = data.getElements();
				 * while(elements.hasNext()){ JsonNode currentDataNode
				 * =elements.next(); String groupid =
				 * currentDataNode.path("groupId").asText(); String artifactid =
				 * currentDataNode.path("artifactId").asText(); String version =
				 * currentDataNode.path("version").asText();
				 * 
				 * System.out.println("*** DATA/groupid = "+ groupid);
				 * System.out.println("*** DATA/artifactid = "+ artifactid);
				 * System.out.println("*** DATA/version = "+ version);
				 * artifactVersions.add(version); }
				 */
				//return sortVersions(artifactVersions);
				return artifactVersions;
			}
		} catch (JsonGenerationException e) {

			e.printStackTrace();

		} catch (JsonMappingException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return null;
	}

	private static List<String> sortVersions(List<String> items) {
		List<Version> versions = new ArrayList<Version>();
		for (String item : items) {
			try {
				versions.add(new Version(item));
			} catch (Exception ex) {
				logger.warn("Could not parse version: " + item, ex);
			}
		}
		Collections.sort(versions);
		Collections.reverse(versions);
		List<String> versionStrings = new ArrayList<String>();
		versionStrings.add(NONE);
		for (Version ver : versions) {
			versionStrings.add(ver.toString());
		}
		return versionStrings;
	}

	/**
	 * Returns a list of files from a version sub-folder.
	 * 
	 * @param baseurl
	 *            the base URL of the repository
	 * @param username
	 *            the username to access the repository or null if no
	 *            credentials are needed
	 * @param password
	 *            the password to access the repository
	 * @param pattern
	 *            the pattern to match the file list against. Only files which
	 *            match will be returned
	 * @return a list of files from the specified version folder
	 */
	public static List<String> getFiles(String baseurl, String username,
			String password, String pattern) {

		logger.debug("Getting files from " + baseurl);

		String responseBody = doHttpRequest(username, password, baseurl);

		List<String> files = new ArrayList<String>();
		Content c = unmarshal(responseBody);
		if (c != null) {
			for (ContentItem ci : c.getContentItems()) {
				String file = ci.getText();
				files.add(file);
			}
		} else {
			logger.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
			parseHtml(responseBody, files);
		}
		return files;
	}

	/**
	 * Creates a full URL out of several components.
	 * 
	 * @param baseurl
	 *            the base URL
	 * @param groupid
	 *            the group id, where dots will be substituted by slashes
	 * @param artifactid
	 *            the artifact id
	 * @return the concatenated URL with a slash at the end
	 */
	public static String concatUrl(String baseurl, String groupid,
			String artifactid) {
		return concatUrl(baseurl, groupid, artifactid, "");
	}

	/**
	 * Creates a full URL out of several components.
	 * 
	 * @param baseurl
	 *            the base URL
	 * @param groupid
	 *            the group id, where dots will be substituted by slashes
	 * @param artifactid
	 *            the artifact id
	 * @param version
	 *            the version number
	 * @return the concatenated URL with a slash at the end
	 */
	public static String concatUrl(String baseurl, String groupid,
			String artifactid, String version) {
		StringBuffer sb = new StringBuffer();
		sb.append(baseurl);
		if (!baseurl.endsWith("/")) {
			sb.append("/");
		}
		if (groupid.startsWith("/")) {
			sb.append(groupid.substring(1).replace('.', '/'));
		} else {
			sb.append(groupid.replace('.', '/'));
		}
		if (!groupid.endsWith("/")) {
			sb.append("/");
		}
		if (artifactid.startsWith("/")) {
			sb.append(artifactid.substring(1));
		} else {
			sb.append(artifactid);
		}
		if (!artifactid.endsWith("/")) {
			sb.append("/");
		}
		if (version.startsWith("/")) {
			sb.append(version.substring(1));
		} else {
			sb.append(version);
		}
		if (!version.isEmpty() && !version.endsWith("/")) {
			sb.append("/");
		}
		return sb.toString();
	}

	/**
	 * Tests access to the specified URL.
	 * 
	 * @param url
	 *            the URL to access
	 * @param username
	 *            the username to use. Might be null if no credentials are
	 *            necessary
	 * @param password
	 *            the password to use
	 * @return true when connection was successful, false otherwise
	 */
	public static boolean testConnection(String url, String username,
			String password) {

		boolean result = false;
		HttpClient client = createHttpClient(username, password);

		HttpMethod method = null;
		int statusCode = -1;
		try {
			method = createHTMLGetMethod(url);
			statusCode = client.executeMethod(method);
			log.fine("Connection test returns HTTP Code = " + statusCode);
			result = (statusCode == HttpStatus.SC_OK);
		} catch (HttpException he) {
			log.severe("Http error connecting to nexus at '" + url + "'" + "returned HTTP Code is : " + statusCode);
			he.printStackTrace();
			logger.error("Http error connecting to '" + url + "'", he);
		} catch (IOException ioe) {
			log.severe("Unable to connect to to nexus at '" + url + "'"+ "returned HTTP Code is : " + statusCode);
			ioe.printStackTrace();
			logger.error("Unable to connect to '" + url + "'", ioe);
		} catch (Exception e) {
			log.severe("Unknown exception while connecting to '" + url + "'");
			e.printStackTrace();
			logger.error("Unknown exception while connecting to '" + url + "'",e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		return result;
	}

	private boolean logError(String msg, final PrintStream logger, Exception e) {
       // use
		//logger.println("ERROR: possible causes: 1. in case of a SNAPSHOT deployment: does your remote repository allow SNAPSHOT deployments?, 2. in case of a release dpeloyment: is this version of the artifact already deployed then does your repository allow updating artifacts?");
        //return logError("DeploymentException: ", logger, e);
		
		log.log(Level.SEVERE, msg, e);
        logger.println(msg);
        e.printStackTrace(logger);
        return false;
    }
	
	private static String doHttpRequest(String username, String password,
			String url) {
		HttpClient client = createHttpClient(username, password);

		String responseBody = null;
		HttpMethod method = null;
		try {
			method = createGetMethod(url);

			client.executeMethod(method);
			responseBody = method.getResponseBodyAsString();
		} catch (HttpException he) {
			logger.error("Http error connecting to '" + url + "'", he);
		} catch (IOException ioe) {
			logger.error("Unable to connect to '" + url + "'", ioe);
		} catch (Exception e) {
			logger.error("Unknown exception while connecting to '" + url + "'",
					e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		return responseBody;
	}

	private static HttpMethod createHTMLGetMethod(String url) {
		HttpMethod method;
		method = new GetMethod(url);
		//method.setRequestHeader("Accept", "application/xml");
		method.setFollowRedirects(true);
		return method;
	}
	
	private static HttpMethod createGetMethod(String url) {
		HttpMethod method;
		method = new GetMethod(url);
		method.setRequestHeader("Accept", "application/xml");
		method.setFollowRedirects(true);
		return method;
	}

	private static HttpClient createHttpClient(String username, String password) {
		HttpClient client = new HttpClient();

		setProxy(client);

		// establish a connection within 10 seconds
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);

		if (username != null) {
			Credentials creds = new UsernamePasswordCredentials(username,
					password);
			if (creds != null) {
				client.getState().setCredentials(AuthScope.ANY, creds);
			}
		}
		return client;
	}

	private static void parseHtml(String responseBody, List<String> matches) {
		if (responseBody != null) {
			Matcher matcher = PATTERN.matcher(responseBody);
			while (matcher.find()) {
				String match = matcher.group(1);
				// remove trailing slash
				if (match.endsWith("/")) {
					match = match.substring(0, match.length() - 1);
				}
				// extract the version only
				if (match.toLowerCase().startsWith("http")) {
					int idx = match.lastIndexOf('/');
					match = match.substring(0, idx);
				}
				if (!"..".equals(match)
						&& !match.toLowerCase().startsWith("http")) {
					matches.add(match);
				}
			}
		}
	}

	private static void setProxy(HttpClient client) {
		ProxyConfiguration pc;
		try {
			pc = ProxyConfiguration.load();
			if (pc != null) {
				logger.info("Using proxy " + pc.name + ":" + pc.port);
				client.getHostConfiguration().setProxy(pc.name, pc.port);
			}
		} catch (IOException e) {
			logger.error("Unable to determine proxy", e);
		}
	}

	private static Content unmarshal(String xml) {
		Content content = null;
		if (xml != null) {
			try {
				StringReader sr = new StringReader(xml);
				JAXBContext jaxbContext = JAXBContext
						.newInstance(Content.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext
						.createUnmarshaller();
				content = (Content) jaxbUnmarshaller.unmarshal(sr);

			} catch (JAXBException e) {
				logger.error("Unmarshaling of XML data failed", e);
			}
		}
		return content;
	}
}
