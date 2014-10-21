package com.alexvasilkov

import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.swing.SwingBuilder
import java.awt.Component

class AndroidSignPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configure(project) {
            if (project.hasProperty('android') && project.android.hasProperty('signingConfigs')) {
                tasks.whenTaskAdded { theTask ->
                    boolean flavorFound = false

                    android.productFlavors.all { theFlavor ->
                        String flavorName = theFlavor.name
                        if (!flavorFound && theTask.name ==~ /package${flavorName.capitalize()}Release/) {
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

        final String configName
        final config

        if (flavorName != null && project.android.signingConfigs.hasProperty(flavorName)) {
            configName = flavorName
            config = project.android.signingConfigs[configName]
        } else {
            configName = "release"
            if (project.android.signingConfigs.hasProperty(configName)) {
                config = project.android.signingConfigs[configName]
            } else {
                config = null
            }
        }

        if (config != null) {
            config.storePassword = '-'
            config.keyPassword = '-'
        }

        project.task(fullTaskName) << {
            if (config == null) {
                println "Project does not contain \"${configName}\" config under \"android.signingConfigs.${configName}\""
                return
            }

            char[] storePass = ''
            char[] keyPass = ''

            if (System.console() == null) {
                // Based on https://www.timroes.de/2014/01/19/using-password-prompts-with-gradle-build-files/
                new SwingBuilder().edt {
                    dialog(
                        modal: true, // Otherwise the build will continue running before you closed the dialog
                        title: "${taskName} keystore passwords", // Dialog title
                        alwaysOnTop: true, // pretty much what the name says
                        resizable: false, // Don't allow the user to resize the dialog
                        locationRelativeTo: null, // Place dialog in center of the screen
                        pack: true, // We need to pack the dialog (so it will take the size of it's children)
                        show: true // Let's show it
                        ) {
                        vbox { // Put everything below each other
                            label(text: "Keystore password:")
                            passwordField(id: 'storePassInput')
                            label(text: "(${config.storeFile.getPath()})")
                            label(text: " ")
                            label(text: "Key password for '${config.keyAlias}':")
                            passwordField(id: 'keyPassInput')
                            label(text: "(leave empty for same as keystore)")
                            label(text: " ")
                            button(defaultButton: true, text: 'OK', actionPerformed: {
                                storePass = storePassInput.password; // Set pass variable to value of input field
                                keyPass = keyPassInput.password;
                                dispose(); // Close dialog
                            })
                        } // vbox end
                    } // dialog end
                } // edt end
            } else {
                System.console().println("\nKeystore file: ${config.storeFile.getPath()}");
                storePass = System.console().readPassword("\n${taskName} keystore password:\n")
                keyPass = System.console().readPassword("${taskName} password for key \"${config.keyAlias}\"" +
                        " (leave empty for same as keystore):\n")
            }

            config.storePassword = new String(storePass)
            config.keyPassword = keyPass == null || keyPass.length == 0 ? new String(storePass) : new String(keyPass)
        }

        return fullTaskName
    }

}
