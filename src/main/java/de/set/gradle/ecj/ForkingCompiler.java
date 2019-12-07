
package de.set.gradle.ecj;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.tasks.compile.JavaCompileSpec;
import org.gradle.process.ExecResult;

public class ForkingCompiler {

	private final Project project;
	private final Configuration compilerConfiguration;

	public ForkingCompiler(Project project, Configuration compilerConfiguration) {
		super();
		this.project = project;
		this.compilerConfiguration = compilerConfiguration;
	}

	public int compile(JavaCompileSpec javaCompileSpec, final List<String> compilerArgs) {
		int result;
		ExecResult execResult =
			project.javaexec(
				exec -> {
					exec.setWorkingDir(javaCompileSpec.getWorkingDir());
					exec.setClasspath(compilerConfiguration);
					exec.setMain("org.eclipse.jdt.internal.compiler.batch.Main");
					exec.args(compilerArgs);
				}
			);
		result = execResult.getExitValue();
		return result;
	}

}

