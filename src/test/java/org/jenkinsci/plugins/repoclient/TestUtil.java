package org.jenkinsci.plugins.repoclient;

import java.io.File;

public class TestUtil {

	private TestUtil() {
		// do not allow instances of this class
	}

	public static File getTestDataFolder(String version) {
		final String curDirProperty = System.getProperty("user.dir");
		final File curDir = new File(curDirProperty);
		final File testDataFolder = new File(curDir, "src" + File.separator + "test" + File.separator + "resources"
				+ File.separator + version);
        return testDataFolder;
	}	
}
