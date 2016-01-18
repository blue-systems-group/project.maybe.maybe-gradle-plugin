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
        project.configurations {
            maybeConfig
        }
//        project.getExtensions().create("MaybeSetting", MaybePluginExtension.class);
        project.extensions.create('maybe', MaybeExtension)
        assert project.maybe instanceof MaybeExtension

//        project.plugins.withType(JavaPlugin) {
//            project.apply plugin: RetrolambdaPluginJava
//        }

//        project.plugins.withType(GroovyPlugin) {
//            project.apply plugin: RetrolambdaPluginGroovy
//        }

        project.plugins.withId('com.android.application') {
            println 'application'
            project.apply plugin: MaybePluginAndroid
        }

        project.plugins.withId('com.android.library') {
            println 'library'
            project.apply plugin: MaybePluginAndroid
        }

        project.dependencies {
            compile 'edu.buffalo.cse.maybe:android-library:0.0.2'
            testCompile 'edu.buffalo.cse.maybe:android-library:0.0.2'
        }

        project.afterEvaluate {
            def config = project.configurations.maybeConfig

            if (config.dependencies.isEmpty()) {
                project.dependencies {
                    maybeConfig 'com.google.code.gson:gson:2.3.1'
                    maybeConfig 'com.squareup.retrofit:converter-gson:2.0.0-beta1'
                    maybeConfig 'com.squareup.retrofit:retrofit:2.0.0-beta1'
                    maybeConfig 'edu.buffalo.cse.maybe:maybe-statement-compiler:0.0.4'
                }
            }

//            def compileConfig = project.configurations.compile
//            println compileConfig
        }
    }
}
