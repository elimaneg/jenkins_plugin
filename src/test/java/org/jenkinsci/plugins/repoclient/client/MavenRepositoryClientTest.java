package org.jenkinsci.plugins.repoclient.client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author mrumpf
 *
 */
public class MavenRepositoryClientTest {

	@Test
	public void testConcatUrl() {
		String url = MavenRepositoryClient.concatUrl(
				"http://www.mycompany.com", "com.mycompany", "product");
		assertTrue("",
				"http://www.mycompany.com/com/mycompany/product/".equals(url));
	}

	public void testConcatUrlWithVersion() {
		String url = MavenRepositoryClient.concatUrl(
				"http://www.mycompany.com", "com.mycompany", "product", "1.2.3.4");
		assertTrue("",
				"http://www.mycompany.com/com/mycompany/product/1.2.3.4/".equals(url));
	}
}
