package org.jenkinsci.plugins.repoclient;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jenkinsci.plugins.repoclient.client.MavenRepositoryClient;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 
 * @author mrumpf
 * 
 */
public class RepositoryClientParameterValue extends StringParameterValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6392602053348942216L;

	private static final Logger logger = Logger
			.getLogger(RepositoryClientParameterValue.class);

	private static final String PREFIX = "APP_";

	private final String groupid;
	private final String artifactid;
	private final String pattern;
	private final Repository repo;
	//private final String packaging;
	//private final String classifier;

	@DataBoundConstructor
	public RepositoryClientParameterValue(String reponame, String groupid,
			String artifactid, String version, String pattern, Repository repo) {
		super(reponame, version);
		this.groupid = groupid;
		this.artifactid = artifactid;
		this.pattern = pattern;
		this.repo = repo;
	}

	private String removeTrailingComma(String urllist) {
		String result = urllist;
		if (urllist.length() >= 1 && urllist.endsWith(",")) {
			result = removeTrailingComma(urllist.substring(0,
					urllist.length() - 1));
		}
		return result;
	}

	@Override
	public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) {
		String url = MavenRepositoryClient.concatUrl(repo.getBaseurl(),
				groupid, artifactid, value);
		List<String> files = MavenRepositoryClient.getFiles(url,
				repo.getUsername(), repo.getPassword(), pattern);
		StringBuffer sb = new StringBuffer();
		for (String file : files) {
			if (file.matches(pattern)) {
				logger.debug("File '" + file + "' matches pattern '" + pattern
						+ "'");
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(url + file);
			}
		}
		final String urllist = sb.toString();

		return new BuildWrapper() {
			/**
			 * This method just makes the build fail for various reasons.
			 */
			@Override
			public Environment setUp(AbstractBuild build, Launcher launcher,
					BuildListener listener) throws IOException,
					InterruptedException {
				return new Environment() {
					@Override
					public void buildEnvVars(Map<String, String> env) {
						
						if ((artifactid != null) && (!artifactid.isEmpty())) env.put(PREFIX + "ARTIFACT_ID", artifactid);
						if ((groupid != null) && (!groupid.isEmpty())) env.put(PREFIX + "ARTIFACT_VERSION", value);
						if ((groupid != null) && (!groupid.isEmpty())) env.put(PREFIX + "ARTIFACT_PACKAGE", pattern);
						if ((groupid != null) && (!groupid.isEmpty())) env.put(PREFIX + "ARTIFACT_CLASSIFIER", artifactid);
						env.put("ARTIFACT_urls_" + groupid + "." + artifactid, removeTrailingComma(urllist));
						env.put("ARTIFACT_user_" + groupid + "." + artifactid, repo.getUsername());
						env.put("ARTIFACT_password_" + groupid + "." + artifactid, repo.getPassword());
						
						/*
						env.put(PREFIX + "_reponame_" + groupid + "." + artifactid, getName());
						env.put(PREFIX + "_artifactid_" + groupid + "."	+ artifactid, artifactid);
						env.put(PREFIX + "_groupid_" + groupid + "." + artifactid, groupid);
						env.put(PREFIX + "_pattern_" + groupid + "." + artifactid, pattern);
						env.put(PREFIX + "_version_" + groupid + "." + artifactid, value);
						env.put(PREFIX + "_urls_" + groupid + "." + artifactid, removeTrailingComma(urllist));
						env.put(PREFIX + "_user_" + groupid + "." + artifactid, repo.getUsername());
						env.put(PREFIX + "_password_" + groupid + "." + artifactid, repo.getPassword());
						*/
						logger.debug("Setting environment: " + env);
					}
				};
			}
		};
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RepositoryClientParameterValue@[");
		sb.append(", reponame=");
		sb.append(getName());
		sb.append(", groupid=");
		sb.append(groupid);
		sb.append(", artifactid=");
		sb.append(artifactid);
		sb.append(" ,pattern=");
		sb.append(pattern);
		sb.append(", version=");
		sb.append(value);
		sb.append(", repo=");
		sb.append(repo);
		sb.append(" (baseurl=");
		sb.append(repo.getBaseurl());
		sb.append(", username=");
		sb.append(repo.getUsername());
		sb.append(", password=");
		sb.append(repo.getPassword());
		sb.append(')');
		sb.append(']');
		return sb.toString();
	}
}
