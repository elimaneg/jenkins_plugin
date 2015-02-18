package org.jenkinsci.plugins.repoclient;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.repoclient.Messages;

import java.io.IOException;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jenkinsci.plugins.repoclient.client.MavenRepositoryClient;
import org.jenkinsci.plugins.repoclient.client.Version;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

/**
 * 
 * @author mrumpf
 * 
 */
public class RepositoryClientParameterDefinition extends
		SimpleParameterDefinition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6612501312931986484L;

	private static final Logger logger = Logger
			.getLogger(RepositoryClientParameterDefinition.class);

	private final String groupid;
	private final String reponame;
	private final String artifactid;
	private final String pattern;

	@DataBoundConstructor
	public RepositoryClientParameterDefinition(String reponame, String groupid,String artifactid, String pattern, String description) {
		super(groupid + "Version : " + artifactid, description);
		this.reponame = reponame;
		this.groupid = groupid;
		this.artifactid = artifactid;
		this.pattern = pattern;
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
		if (defaultValue instanceof StringParameterValue) {
			StringParameterValue value = (StringParameterValue) defaultValue;
			return new RepositoryClientParameterDefinition(getReponame(), "",
					"", value.value, getDescription());
		} else {
			return this;
		}
	}

	@Exported
	public List<String> getChoices() {
		Repository r = DESCRIPTOR.getRepo(reponame);
		List<String> versions = null;
		if (r != null) {
			versions = MavenRepositoryClient.getVersions(r.getBaseurl(),
					groupid, artifactid, r.getUsername(), r.getPassword());
		}
		return versions;
	}

	@Exported
	public String getArtifactid() {
		return artifactid;
	}

	@Exported
	public String getReponame() {
		return reponame;
	}

	@Exported
	public String getGroupid() {
		return groupid;
	}

	@Exported
	public String getPattern() {
		return pattern;
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		return new RepositoryClientParameterValue(reponame, groupid,
				artifactid, jo.getString("value"), pattern,
				DESCRIPTOR.getRepo(reponame));
	}

	@Override
	public ParameterValue createValue(String version) {
		// this should never be called
		throw new RuntimeException("Not implemented");
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static class DescriptorImpl extends ParameterDescriptor {
		private final CopyOnWriteList<Repository> repos = new CopyOnWriteList<Repository>();

		public DescriptorImpl() {
			super(RepositoryClientParameterDefinition.class);
			load();
		}

		public Repository getRepo(String reponame) {
			logger.debug("getRepo(" + reponame + ")");
			Repository result = null;
			for (Repository r : repos) {
				if (reponame.equals(r.getReponame())) {
					if (result != null) {
						logger.warn("Repository " + reponame
								+ " found multiple times");
					}
					result = r;
				}
			}
			if (result == null) {
				logger.warn("Repository " + reponame + " not found");
			}
			return result;
		}

		public Repository[] getRepos() {
			return repos.toArray(new Repository[repos.size()]);
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) {
			if (formData.has("repo")) {
				try {
					repos.replaceBy(JSONArray.toList(
							formData.getJSONArray("repo"), Repository.class));
				} catch (JSONException ex) {
					repos.replaceBy((Repository) JSONObject.toBean(
							formData.getJSONObject("repo"), Repository.class));
				}
			} else {
				repos.clear();
			}

			save();
			return true;
		}

		public FormValidation doCheckGroupid(@QueryParameter String groupid,
				@QueryParameter String artifactid,
				@QueryParameter String reponame) throws IOException {
			/*
			FormValidation result = FormValidation.ok();
			if (groupid == null || groupid.isEmpty()) {
				result = FormValidation.error(Messages.EmptyGroupId());
			} */
			return FormValidation.ok();
		}

		public FormValidation doCheckArtifactid(
				@QueryParameter String artifactid,
				@QueryParameter String groupid, @QueryParameter String reponame)
				throws IOException {
			FormValidation result = FormValidation.ok();
			if (artifactid == null || artifactid.isEmpty()) {
				result = FormValidation.error(Messages.EmptyArtifactId());
			} 
			return result;
		}

		public FormValidation doCheckGroupid_orig(@QueryParameter String groupid,
				@QueryParameter String artifactid,
				@QueryParameter String reponame) throws IOException {
			
			FormValidation result = FormValidation.ok();
			if (groupid == null || groupid.isEmpty()) {
				result = FormValidation.error(Messages.EmptyGroupId());
			} else {
				result = checkPath(artifactid, groupid, reponame);
			}
			return result;
		}

		public FormValidation doCheckArtifactid_orig(
				@QueryParameter String artifactid,
				@QueryParameter String groupid, @QueryParameter String reponame)
				throws IOException {
			FormValidation result = FormValidation.ok();
			if (artifactid == null || artifactid.isEmpty()) {
				result = FormValidation.error(Messages.EmptyArtifactId());
			} else {
				result = checkPath(artifactid, groupid, reponame);
			}
			return result;
		}

		
		private FormValidation checkPath(String artifactid, String groupid,
				String reponame) {
			FormValidation result = FormValidation.ok();
			Repository r = getRepo(reponame);
			String url = MavenRepositoryClient.concatUrl(r.getBaseurl(),
					groupid, artifactid);
			if (!MavenRepositoryClient.testConnection(url, r.getUsername(),
					r.getPassword())) {
				result = FormValidation.error(Messages.EmptyArtifactId() + url);
			}
			return result;
		}

		public FormValidation doCheckPattern(@QueryParameter String pattern)
				throws IOException {
			FormValidation result = FormValidation.ok();
			try {
				pattern.matches("");
			} catch (PatternSyntaxException ex) {
				result = FormValidation.error(Messages.RegexPatternNotValid());
			}
			return result;
		}

		public FormValidation doCheckReponame(@QueryParameter String reponame)
				throws IOException {
			FormValidation result = FormValidation.ok();
			if (reponame == null || reponame.isEmpty()) {
				result = FormValidation.error(Messages.EmptyRepositoryName());
			}
			return result;
		}

		public FormValidation doCheckBaseurl(@QueryParameter String baseurl)
				throws IOException {
			FormValidation result = FormValidation.ok();
			if (baseurl == null || baseurl.isEmpty()) {
				result = FormValidation.error(Messages.EmptyBaseURL());
			}
			return result;
		}

		public FormValidation doCheckPassword(@QueryParameter String password,
				@QueryParameter String username) throws IOException {
			FormValidation result = FormValidation.ok();
			if (password != null && !password.isEmpty()
					&& (username == null || username.isEmpty())) {
				result = FormValidation.error(Messages.EmptyUsername());
			}
			return result;
		}

		public FormValidation doTestConnection(@QueryParameter String baseurl,
				@QueryParameter String username, @QueryParameter String password)
				throws IOException, ServletException {
			try {
				if (MavenRepositoryClient.testConnection(baseurl, username,
						password)) {
					return FormValidation.ok(Messages.Success());
				} else {
					return FormValidation.error(Messages.ConnectionFailed());
				}
			} catch (Exception e) {
				logger.error("Client error: " + e.getMessage(), e);
				return FormValidation.error(Messages.ClientError()
						+ e.getMessage());
			}
		}

		@Override
		public String getDisplayName() {
			return Messages.DisplayName();
		}

		@Override
		public String getHelpFile() {
			return "/help-parameter.html";
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RepositoryClientParameterDefinition@[");
		sb.append("name=");
		sb.append(getName());
		sb.append(", description=");
		sb.append(getDescription());
		sb.append(", groupid=");
		sb.append(groupid);
		sb.append(", reponame=");
		sb.append(reponame);
		sb.append(", artifactid=");
		sb.append(artifactid);
		sb.append(" ,pattern=");
		sb.append(pattern);
		sb.append(']');
		return sb.toString();
	}
}
