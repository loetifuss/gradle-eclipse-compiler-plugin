
package de.set.gradle.ecj;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.tools.JavaCompiler;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class ServiceLoaderCompiler {

	private static final Logger LOGGER = Logging.getLogger(ServiceLoaderCompiler.class);
	private final ClassLoader ecjClassLoader;

	public ServiceLoaderCompiler(Configuration compilerConfiguration) {
		ecjClassLoader = new EcjCompilerClassLoader(compilerConfiguration);
	}

	public ServiceLoaderCompiler(ClassLoader cl) {
		ecjClassLoader = cl;
	}

	public int compile(List<String> compilerArguments) {
		Instant start = Instant.now();
		int result;
		Iterator<JavaCompiler> compilerIterator = ServiceLoader.load(JavaCompiler.class, ecjClassLoader).iterator();
		if (compilerIterator.hasNext()) {
			JavaCompiler compiler = compilerIterator.next();
			List<String> args = new ArrayList<String>(compilerArguments);

			// prevent System.exit
			args.add("-noExit");
			result = compiler.run(System.in, System.out, System.err, args.toArray(new String[0]));
		} else {
			LOGGER.error("no compiler implementation found on classpath");
			throw new CompilationFailedException();
		}
		Instant end = Instant.now();
		LOGGER.info("ecj compilation took: " + Duration.between(start, end));
		return result;
	}

}

