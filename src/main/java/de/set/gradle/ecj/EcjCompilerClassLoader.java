package de.set.gradle.ecj;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import org.gradle.api.artifacts.Configuration;

public class EcjCompilerClassLoader extends URLClassLoader {

	public EcjCompilerClassLoader(Configuration compilerConfiguration) {
		super(getSourceURLs(compilerConfiguration.getFiles()));
	}

	public EcjCompilerClassLoader(Set<File> files) {
		super(getSourceURLs(files));
	}

	private static URL[] getSourceURLs(Set<File> files) {
		URL[] urls =
			files
				.stream()
				.map(
					file -> {
						try {
							return file.toURI().toURL();
						} catch (MalformedURLException e) {
							throw new IllegalArgumentException("Invalid source file location", e);
						}
					}
				)
				.toArray(URL[]::new);
		return urls;
	}

}

