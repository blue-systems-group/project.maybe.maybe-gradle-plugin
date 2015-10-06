/**
 Copyright 2014 Evan Tatarka

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.buffalo.cse.maybe

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import org.apache.tools.ant.taskdefs.Java
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileTree
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

/**
 * Created with IntelliJ IDEA.
 * User: evan
 * Date: 8/4/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaybePluginAndroid implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin)

        if (isLibrary) {
            println 'isLibrary'
            def android = project.extensions.getByType(LibraryExtension)
            android.libraryVariants.all { BaseVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile, isLibrary)
            }
            android.testVariants.all { TestVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile, isLibrary)
            }
        } else {
            println 'notLibrary'
            def android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { BaseVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile, isLibrary)
            }
            android.testVariants.all { TestVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile, isLibrary)
            }
        }

        // TODO: remove this, because it just suppress the WARNING:
        // Conflict with dependency 'com.android.support:support-annotations'.
        // Resolved versions for app (23.0.0) and test app (22.2.0) differ.
        project.configurations.all {
            resolutionStrategy.force 'com.android.support:support-annotations:23.0.0'
        }
    }

    private
    static String getPackageName(Project project, boolean isLibrary) {
        def maybeExtension = project.extensions.getByType(MaybeExtension)
        if (maybeExtension.packageName != null) {
            return maybeExtension.packageName
        }

        if (!isLibrary) {
            if (project.android.defaultConfig.applicationId != null) {
                return project.android.defaultConfig.applicationId
            }
        }
        def manifestFile = project.file(project.projectDir.absolutePath + '/src/main/AndroidManifest.xml')
//            def ns = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", "android")
        def xml = new XmlParser().parse(manifestFile)
        return xml.attributes().package
    }

    private
    static String getVersion(Project project, boolean isLibrary) {
        def maybeExtension = project.extensions.getByType(MaybeExtension)
        if (maybeExtension.version != null) {
            return maybeExtension.version
        }
        if (project.android.defaultConfig.versionName != null) {
            return project.android.defaultConfig.versionName
        }
        return 'unknown version'
    }

    private
    static JavaExec createTask(Project project, String name, JavaCompile javaCompileTask, File dest, boolean upload, boolean isLibrary) {
        JavaExec maybeTask = project.task(
                name,
                type: JavaExec
        ) as JavaExec

        def options = javaCompileTask.options

        maybeTask.inputs.file(javaCompileTask.source)
        maybeTask.outputs.dir(dest)
        maybeTask.main = 'edu.buffalo.cse.blue.maybe.Main'
        maybeTask.dependsOn javaCompileTask.dependsOn.collect()

        maybeTask.doFirst {
            def packageName = getPackageName(project, isLibrary)
            def version = getVersion(project, isLibrary)
            println packageName
            println version
            def arguments = args
            arguments.addAll(['-g', '-c', '-D', dest])
            if (upload) {
                arguments.addAll(['-upload'])
            }
            arguments.addAll(['-noserial', '-postcompiler', 'javac'])
            // TODO: let user set
            arguments.addAll(['-package', "$packageName:$version"])

            if (options.bootClasspath != null) {
                arguments.addAll(['-bootclasspath', options.bootClasspath])
            }
            // TODO: polyglot doesn't support -Xlint
            // arguments.addAll(options.compilerArgs)

            // if (options.encoding != null) {
            //     // TODO: polyglot doesn't support -encoding
            //     arguments.addAll(['-encoding', options.encoding])
            // }

            javaCompileTask.source.each {
                file -> arguments.add(file)
            }

            javaCompileTask.classpath.each {
                path -> arguments.addAll(['-classpath', path])
            }

            args arguments
            classpath project.files(project.configurations.maybeConfig)

//            classpath configurations.maybe
//            classpath project.files( fileTree('/Users/xcv58/Dropbox/Projects-Android/MaybeLibrary/libs-maybe', include: '*.jar'))
//            classpath 'com.google.code.gson:gson:2.3.1'
//            classpath project.fileTree('/Users/xcv58/Dropbox/Projects-Android/MaybeLibrary/libs-maybe', include: '*.jar')

//            classpath Configuration.collect {
//                'com.google.code.gson:gson:2.3.1'
//                'com.squareup.retrofit:converter-gson:2.0.0-beta1'
//                'com.squareup.retrofit:retrofit:2.0.0-beta1'
////                FileTree.collect(
////                        dir: '/Users/xcv58/Dropbox/Projects-Android/MaybeLibrary/libs-maybe', include: '*.jar'
////                )
////                fileTree(dir: '/Users/xcv58/Dropbox/Projects-Android/MaybeLibrary/libs-maybe', include: '*.jar')
//            }
        }
        return maybeTask
    }

    private
    static configureCompileJavaTask(Project project, String variant, JavaCompile javaCompileTask, boolean isLibrary) {
        def oldDestDir = javaCompileTask.destinationDir
        def newDestDir = project.file("$project.buildDir/maybe/$variant/classes")
        println newDestDir
        println 'project: ' + project
        println 'variant: ' + variant
        println 'javaCompileTask: ' + javaCompileTask
        println 'new dir: ' + newDestDir
        def uploadTaskName = "maybeUpload${variant.capitalize()}"
        def newTaskName = javaCompileTask.name + 'Maybe'

        def maybeTask = createTask(project, newTaskName, javaCompileTask, newDestDir, false, isLibrary)
        def uploadTask = createTask(project, uploadTaskName, javaCompileTask, newDestDir, true, isLibrary)
//        javaCompileTask.source.forEach{ file -> println file}

        javaCompileTask.dependsOn { newTaskName }
        javaCompileTask.doFirst {
            javaCompileTask.source = project.fileTree(dir: newDestDir.toString())
        }
    }

    private
    static configureUnitTestTask(Project project, String variant, JavaCompile javaCompileTask) {
//        javaCompileTask.mustRunAfter("compileRetrolambda${variant.capitalize()}")
//
//        javaCompileTask.doFirst {
//            def retrolambda = project.extensions.getByType(MaybeExtension)
//            def rt = "$retrolambda.jdk/jre/lib/rt.jar"
//
//            // We need to add the rt to the classpath to support lambdas in the tests themselves
//            javaCompileTask.classpath += project.files(rt)
//
//            ensureCompileOnJava8(retrolambda, javaCompileTask)
//        }
//
//        Test runTask = (Test) project.tasks.findByName("test${variant.capitalize()}")
//        if (runTask) {
//            runTask.doFirst {
//                def retrolambda = project.extensions.getByType(MaybeExtension)
//                ensureRunOnJava8(retrolambda, runTask)
//            }
//        }
    }
}
