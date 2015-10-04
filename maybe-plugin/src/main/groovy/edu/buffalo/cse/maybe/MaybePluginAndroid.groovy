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

//import static me.tatarka.RetrolambdaPlugin.checkIfExecutableExists
//configurations {
//    maybe
//}
//
//dependencies {
//    //    compile(name: 'maybe', ext: 'aar')
//    maybe 'com.google.code.gson:gson:2.3.1'
//    maybe 'com.squareup.retrofit:converter-gson:2.0.0-beta1'
//    maybe 'com.squareup.retrofit:retrofit:2.0.0-beta1'
//    maybe fileTree(dir: '/Users/xcv58/Dropbox/Projects-Android/MaybeLibrary/libs-maybe', include: '*.jar')
//}

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
                configureCompileJavaTask(project, variant.name, variant.javaCompile)
            }
            android.testVariants.all { TestVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile)
            }
        } else {
            println 'notLibrary'
            def android = project.extensions.getByType(AppExtension)
            android.applicationVariants.all { BaseVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile)
            }
            android.testVariants.all { TestVariant variant ->
                configureCompileJavaTask(project, variant.name, variant.javaCompile)
            }
        }
    }

    private
    static configureCompileJavaTask(Project project, String variant, JavaCompile javaCompileTask) {
        def oldDestDir = javaCompileTask.destinationDir
        def newDestDir = project.file("$project.buildDir/maybe/$variant/classes")
        println newDestDir
        println 'project: ' + project
        println 'variant: ' + variant
        println 'javaCompileTask: ' + javaCompileTask
        println 'new dir: ' + newDestDir
        def newTaskName = javaCompileTask.name + 'Maybe'
        def options = javaCompileTask.options
        JavaExec maybeTask = project.task(
                newTaskName,
                type: JavaExec
        ) as JavaExec

//        javaCompileTask.source.forEach{ file -> println file}
        maybeTask.inputs.file(javaCompileTask.source)
        maybeTask.outputs.dir(newDestDir)
        maybeTask.main = 'edu.buffalo.cse.blue.maybe.Main'
        maybeTask.dependsOn javaCompileTask.dependsOn.collect()
        maybeTask.doFirst {
            def arguments = args
            arguments.addAll(['-g', '-c', '-D', newDestDir])
            arguments.addAll(['-upload'])
            arguments.addAll(['-noserial', '-postcompiler', 'javac'])
            // TODO: let user set
            arguments.addAll(['-package', 'test'])
//            arguments.addAll(['-package', manifestPackage()])

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
            println project.files(project.configurations.maybeConfig)
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

        javaCompileTask.dependsOn { newTaskName }
        javaCompileTask.doFirst {
            javaCompileTask.source = project.fileTree(dir: newDestDir.toString())
        }

//        MaybeTask maybeTask = project.task(
//                "compileMaybe${variant.capitalize()}",
//                type: MaybeTask
//        ) as MaybeTask

//        RetrolambdaTask retrolambdaTask = project.task(
//                "compileRetrolambda${variant.capitalize()}",
//                type: RetrolambdaTask
//        ) as RetrolambdaTask
//
//        retrolambdaTask.inputDir = newDestDir
//        retrolambdaTask.outputDir = oldDestDir
//        retrolambdaTask.classpath = project.files()
//
//        retrolambdaTask.doFirst {
//            def classpathFiles = javaCompileTask.classpath + project.files("$project.buildDir/retrolambda/$variant")
//            retrolambdaTask.classpath += classpathFiles
//
//            // bootClasspath isn't set until the last possible moment because it's expensive to look
//            // up the android sdk path.
//            def bootClasspath = javaCompileTask.options.bootClasspath
//            if (bootClasspath) {
//                retrolambdaTask.classpath += project.files(bootClasspath.tokenize(File.pathSeparator))
//            } else {
//                // If this is null it means the javaCompile task didn't need to run, don't bother running retrolambda either.
//                throw new StopExecutionException()
//            }
//        }
//
//        project.gradle.taskGraph.afterTask { Task task, TaskState state ->
//            if (task == retrolambdaTask) {
//                // We need to set this back to subsequent android tasks work correctly.
//                javaCompileTask.destinationDir = oldDestDir
//            }
//        }
//
//        javaCompileTask.destinationDir = newDestDir
//        javaCompileTask.sourceCompatibility = "1.8"
//        javaCompileTask.targetCompatibility = "1.8"
//        javaCompileTask.finalizedBy(retrolambdaTask)
//
//        javaCompileTask.doFirst {
//            def retrolambda = project.extensions.getByType(MaybeExtension)
//            def rt = "$retrolambda.jdk/jre/lib/rt.jar"
//
//            javaCompileTask.classpath += project.files(rt)
//
//            retrolambdaTask.javaVersion = retrolambda.javaVersion
//            retrolambdaTask.jvmArgs = retrolambda.jvmArgs
//
//            ensureCompileOnJava8(retrolambda, javaCompileTask)
//        }
//
//        def extractAnnotations = project.tasks.findByName("extract${variant.capitalize()}Annotations")
//        if (extractAnnotations) {
//            extractAnnotations.deleteAllActions()
//            project.logger.warn("$extractAnnotations.name is incompatible with java 8 sources and has been disabled.")
//        }
//
//        JavaCompile compileUnitTest = (JavaCompile) project.tasks.find { task ->
//            task.name.startsWith("compile${variant.capitalize()}UnitTestJava")
//        }
//        if (compileUnitTest) {
//            configureUnitTestTask(project, variant, compileUnitTest)
//        }
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

    private static ensureCompileOnJava8(MaybeExtension retrolambda, JavaCompile javaCompile) {
//        if (!retrolambda.onJava8) {
//            // Set JDK 8 for the compiler task
//            def javac = "${retrolambda.tryGetJdk()}/bin/javac"
//            if (!checkIfExecutableExists(javac)) throw new ProjectConfigurationException("Cannot find executable: $javac", null)
//            javaCompile.options.fork = true
//            javaCompile.options.forkOptions.executable = javac
//        }
    }

    private static ensureRunOnJava8(MaybeExtension retrolambda, Test test) {
//        if (!retrolambda.onJava8) {
//            def java = "${retrolambda.tryGetJdk()}/bin/java"
//            if (!checkIfExecutableExists(java)) throw new ProjectConfigurationException("Cannot find executable: $java", null)
//            test.executable java
//        }
    }
}
