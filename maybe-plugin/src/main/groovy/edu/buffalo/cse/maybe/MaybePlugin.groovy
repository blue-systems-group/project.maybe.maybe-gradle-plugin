package edu.buffalo.cse.maybe

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by xcv58 on 10/3/15.
 */
class MaybePlugin implements Plugin<Project> {
    /**
     * apply
     * @param project
     */
    @Override
    void apply(Project project) {
        println('hello world!')
//        project.getExtensions().create("MaybeSetting", MaybePluginExtension.class);
//        project.getTasks().create("demo", MaybeTask.class);
//        project.extensions.create('retrolambda', RetrolambdaExtension)

//        project.configurations {
//            maybeConfig
//        }

//        project.plugins.withType(JavaPlugin) {
//            project.apply plugin: RetrolambdaPluginJava
//        }

//        project.plugins.withType(GroovyPlugin) {
//            project.apply plugin: RetrolambdaPluginGroovy
//        }

        project.plugins.withId('com.android.application') {
            println 'application'
//            project.apply plugin: RetrolambdaPluginAndroid
        }

        project.plugins.withId('com.android.library') {
            println 'library'
//            project.apply plugin: RetrolambdaPluginAndroid
        }
    }
}
