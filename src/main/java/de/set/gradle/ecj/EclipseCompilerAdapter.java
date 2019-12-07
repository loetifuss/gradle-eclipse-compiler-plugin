
package de.set.gradle.ecj;

import java.io.File;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.api.internal.tasks.compile.JavaCompilerArgumentsBuilder;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.process.ArgWriter;
import org.gradle.language.base.internal.compile.Compiler;

/**
 * {@link Compiler} that calls the Eclipse Compiler for Java for code compilation.
 */
public class EclipseCompilerAdapter implements Compiler<JavaCompileSpec> {

	private static final Logger LOGGER = Logging.getLogger(EclipseCompilerAdapter.class);
	private Configuration compilerConfiguration;
	private Project project;
	private String ecjArtifact;

	EclipseCompilerAdapter(Configuration compilerConfiguration, Project project) {
		this.compilerConfiguration = compilerConfiguration;
		this.project = project;
		EclipseCompilerExtension extension = project.getExtensions().getByType(EclipseCompilerExtension.class);
		this.ecjArtifact =
			extension.getToolGroupId()
			+ ":"
			+ extension.getToolArtifactId()
			+ ":"
			+ extension.getToolVersion();
	}

	@Override
	public WorkResult execute(JavaCompileSpec javaCompileSpec) {
		LOGGER.info("Compiling sources using eclipse compiler for java [" + this.ecjArtifact + "]");

		final List<String> remainingArguments =
			new JavaCompilerArgumentsBuilder(javaCompileSpec)
				.includeSourceFiles(true)
				.build();

		List<String> compilerArguments = shortenArgs(javaCompileSpec.getTempDir(), remainingArguments);

		int result;
		if (isFork(javaCompileSpec)) {
			result = new ForkingCompiler(project, compilerConfiguration).compile(javaCompileSpec, compilerArguments);
		} else {
			result = new ServiceLoaderCompiler(compilerConfiguration).compile(compilerArguments);
		}

		if (result != 0) {
			throw new CompilationFailedException(result);
		}

		return () -> true;
	}

	private boolean isFork(JavaCompileSpec javaCompileSpec) {
		return
			javaCompileSpec.getCompileOptions().getForkOptions() != null
			&& javaCompileSpec.getCompileOptions().getForkOptions().getExecutable() != null;
	}

	private List<String> shortenArgs(File tempDir, List<String> args) {

		// for command file format, see http://docs.oracle.com/javase/6/docs/technotes/tools/windows/javac.html#commandlineargfile
		// use platform character and line encoding
		return
			ArgWriter
				.argsFileGenerator(new File(tempDir, "java-compiler-args.txt"), ArgWriter.unixStyleFactory())
				.transform(args);
	}
}

