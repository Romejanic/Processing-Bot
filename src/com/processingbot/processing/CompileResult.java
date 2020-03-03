package com.processingbot.processing;

import java.nio.file.Path;

public class CompileResult {

	public final boolean success;
	public final int exitCode;
	public final String stderr;
	public final Path outputPath;
	
	public CompileResult(Path outputPath) {
		this.success = true;
		this.exitCode = 0;
		this.stderr = null;
		this.outputPath = outputPath;
	}

	public CompileResult(int exitCode, String stderr) {
		this.success = false;
		this.exitCode = exitCode;
		this.stderr = stderr;
		this.outputPath = null;
	}
	
}