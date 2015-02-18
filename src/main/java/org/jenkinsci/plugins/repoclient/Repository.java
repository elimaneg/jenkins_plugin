package org.jenkinsci.plugins.repoclient;

import hudson.util.Secret;

/**
 * This class represents a repository in the global configuration.
 *
 * @author mrumpf
 *
 */
public class Repository {
	private String reponame;
	private String username;
	private Secret password;
	private String baseurl;

	/**
	 * Default constructor.
	 */
	public Repository() {
	}

	/**
	 * Constructor.
	 * 
	 * @param reponame the repository name
	 * @param baseurl the base URL
	 * @param username the username or null if not necessary
	 * @param password the password or null if no username is given
	 */
	public Repository(String reponame, String baseurl, String username,
			String password) {
		this.reponame = reponame;
		this.baseurl = baseurl;
		this.username = username;
		this.password = Secret.fromString(password.trim());;
	}

	public String getReponame() {
		return reponame;
	}

	public String getUsername() {
		return username;
	}

	public String getBaseurl() {
		return baseurl;
	}

	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = Secret.fromString(password.trim());;
	}

	public String getPassword() {
		return Secret.toString(password);
	}

	public void setReponame(String reponame) {
		this.reponame = reponame;
	}

	public String toString() {
		return reponame;
	}
}
