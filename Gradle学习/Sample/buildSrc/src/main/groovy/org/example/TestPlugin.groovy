package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

public class TestPlugin implements Plugin<Project> {
	public void apply(Project project) {
		def testExt = project.extensions.create('TestExt',TestExtension.class,project)

		project.tasks.create("sayhi",{
			doLast{
				println "${testExt.serverUrl} , ${testExt.age}"
			}
			})
	}
}

public class TestExtension {
    private final Property<String> serverUrl;

    String age

	
	public TestExtension(Project project) {
		serverUrl = project.getObjects().property(String.class);
	}
	
	public Property<String> getServerUrl() {
		return serverUrl;
	}
}