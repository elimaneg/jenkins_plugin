package org.jenkinsci.plugins.repoclient.client;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * This represents a content-item of a Maven repository response with mime-type
 * application/xml for the Accept HTTP header.
 *
 * <pre>
 * <content>
 *   <data>
 *     <content-item>
 *       <resourceURI>https://www.mycompany.com/nexus/content/groups/repo/com/mycompany/abc/maven-metadata.xml.md5</resourceURI>
 *       <relativePath>/groups/repo/com/mycompany/abc/maven-metadata.xml.md5</relativePath>
 *       <text>maven-metadata.xml.md5</text> <leaf>true</leaf>
 *       <lastModified>2012-04-22 20:08:56.0 CEST</lastModified>
 *       <sizeOnDisk>33</sizeOnDisk>
 *     </content-item>
 *     <!-- ... -->
 *   </data>
 * </content>
 * </pre>
 * 
 * @author mrumpf
 */
@XmlRootElement
public class Content {
	private List<ContentItem> contentitems;

	public List<ContentItem> getContentItems() {
		return contentitems;
	}

	@XmlElementWrapper(name = "data")
	@XmlElement(name = "content-item")
	public void setContentItems(List<ContentItem> contentitems) {
		this.contentitems = contentitems;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Content[");
		sb.append(contentitems);
		sb.append("]");
		return sb.toString();
	}
}
