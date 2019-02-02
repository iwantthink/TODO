package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.Action

public class TestPlugin implements Plugin<Project> {
	public void apply(Project project) {
		def testExt = project.extensions.create('TestExt',TestExtension.class,project)

		project.tasks.create("sayhi",{
			doLast{
				println "${testExt.serverUrl} , ${testExt.age}"
			}
			})
	// 创建依赖对象
		final Configuration config = project
				.getConfigurations()
				.create("dataFiles")
	            .setVisible(false)
	            .setDescription("The data artifacts to be processed for this plugin.");
	
	    println "config.class =  ${config.class}"

			// 给依赖对象添加 Action
		// config.defaultDependencies(new Action<DependencySet>() {
	 //            public void execute(DependencySet dependencies) {//net.ltgt.gradle:gradle-apt-plugin:0.20
	 //                dependencies.add(project.getDependencies().create("net.ltgt.gradle:gradle-apt-plugin:0.20"));
	 //            }
	 //        });

		config.defaultDependencies({
			DependencySet dependencies->
				println "add dependencies............"
				dependencies.add(project.getDependencies().create("net.ltgt.gradle:gradle-apt-plugin:0.20"));
		});
	
		project.getTasks().withType(DataProcessing.class).configureEach(new Action<DataProcessing>() {
	            public void execute(DataProcessing dataProcessing) {
	            	println "execute action in configureEach!!!!!"
	                dataProcessing.setDataFiles(config);
	            }
	        });

		project.tasks.create("testConfig",DataProcessing.class)



	}
}

	import org.gradle.api.DefaultTask;
	import org.gradle.api.file.ConfigurableFileCollection;
	import org.gradle.api.file.FileCollection;
	import org.gradle.api.tasks.InputFiles;
	import org.gradle.api.tasks.TaskAction;

	public class DataProcessing extends DefaultTask {
	    private final ConfigurableFileCollection dataFiles;
	
	    public DataProcessing() {
	        dataFiles = getProject().files();
	    }
	
	    @InputFiles
	    public FileCollection getDataFiles() {
	        return dataFiles;
	    }
	
	    public void setDataFiles(FileCollection dataFiles) {
	        this.dataFiles.setFrom(dataFiles);
	    }
	
	    @TaskAction
	    public void process() {
	        System.out.println(getDataFiles().getFiles());

	        println getProject().configurations.default.getAllArtifacts().size()

			getProject().configurations.default.getAllArtifacts().each() {
			        println "name of artifact : ${it.name} , ${it.getFile().name}"
			    }
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