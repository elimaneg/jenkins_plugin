package org.jenkinsci.plugins.repoclient.client;

import javax.xml.bind.annotation.XmlElement;

/**
 * @see Content
 *
 * @author mrumpf
 */
public class ContentItem {
	private String resourceURI;
	private String relativePath;
	private String text;
	private String leaf;
	private String lastModified;
	private String sizeOnDisk;

	public String getResourceURI() {
		return resourceURI;
	}

	@XmlElement
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	public String getRelativePath() {
		return relativePath;
	}

	@XmlElement
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getText() {
		return text;
	}

	@XmlElement
	public void setText(String text) {
		this.text = text;
	}

	public String getLeaf() {
		return leaf;
	}

	@XmlElement
	public void setLeaf(String leaf) {
		this.leaf = leaf;
	}

	public String getLastModified() {
		return lastModified;
	}

	@XmlElement
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getSizeOnDisk() {
		return sizeOnDisk;
	}

	@XmlElement
	public void setSizeOnDisk(String sizeOnDisk) {
		this.sizeOnDisk = sizeOnDisk;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ContentItem[");
		sb.append("resourceURI=");
		sb.append(resourceURI);
		sb.append(", relativePath=");
		sb.append(relativePath);
		sb.append(", text=");
		sb.append(text);
		sb.append(", leaf=");
		sb.append(leaf);
		sb.append(", lastModified=");
		sb.append(lastModified);
		sb.append(", sizeOnDisk=");
		sb.append(sizeOnDisk);
		sb.append("]");
		return sb.toString();
	}
}
