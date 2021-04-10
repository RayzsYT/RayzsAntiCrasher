package de.rayzs.rayzsanticrasher.runtime;

import java.io.IOException;

public class RuntimeExec {

	public RuntimeExec(String text) {
		Process process;
		Runtime runtime = Runtime.getRuntime();
		try {
			process = runtime.exec(text); 
			Thread.sleep(50); 
			process.destroy();
		} catch (IOException | InterruptedException error) { }
	}
}