package com.alexvasilkov

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidSignPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configure(project) {
            if (project.hasProperty('android') && project.android.hasProperty('signingConfigs')) {
                tasks.whenTaskAdded { theTask ->
                    boolean flavorFound = false

                    android.productFlavors.all { theFlavor ->
                        String flavorName = theFlavor.name
                        if (!flavorFound && theTask.name ==~ /package${flavorName}Release/) {
                            theTask.dependsOn addPasswordsTask(project, flavorName)
                            flavorFound = true
                        }
                    }

                    if (!flavorFound && theTask.name ==~ /packageRelease/) {
                        theTask.dependsOn addPasswordsTask(project, null)
                    }
                }
            }
        }
    }

    static String addPasswordsTask(Project project, String flavorName) {
        final String taskName = flavorName == null ? "Release" : flavorName
        final String fullTaskName = "askForPasswords${taskName}"
        final String configName = flavorName == null ? "release" : flavorName

        def final config = project.android.signingConfigs[configName];

        config.storePassword = '-'
        config.keyPassword = '-'

        project.task(fullTaskName) << {
            if (!project.android.signingConfigs.hasProperty(configName)) {
                println "Project does not contain \"${configName}\" config under \"android.signingConfigs.${configName}\""
                return
            }

            char[] storePass = ''
            char[] keyPass = ''

            if (System.console() == null) {
                println "Cannot read passwords: System.console() is not available"
            } else {
                System.console().println("\nKeystore file: ${config.storeFile.getPath()}");
                storePass = System.console().readPassword("\n${taskName} keystore password:\n")
                keyPass = System.console().readPassword("${taskName} password for key \"${config.keyAlias}\"" +
                        " (leave empty if same as keystore password):\n")
            }

            config.storePassword = new String(storePass)
            config.keyPassword = keyPass == null || keyPass.length == 0 ? new String(storePass) : new String(keyPass)
        }

        return fullTaskName
    }

}
