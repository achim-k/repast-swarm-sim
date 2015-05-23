package swarm_sim.experiment;

import java.io.File;
import java.io.FilenameFilter;

import repast.simphony.runtime.RepastBatchMain;

public class BatchRunner {

    public static void main(String[] args) {
	System.out.println("Test");

	String batchMainArgs[] = new String[] { "-params", "batchFile", args[0] };

	File batchFileFolder = new File("batch_files");

	FilenameFilter xmlFilter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		String lowercaseName = name.toLowerCase();
		if (lowercaseName.endsWith(".xml")) {
		    return true;
		} else {
		    return false;
		}
	    }
	};

	for (File file : batchFileFolder.listFiles(xmlFilter)) {
	    batchMainArgs[1] = file.getAbsolutePath();
	    RepastBatchMain.main(batchMainArgs);
	}
    }
}
