package com.alexvasilkov
import groovy.swing.SwingBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidSignPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configure(project) {
            if (project.hasProperty('android') && project.android.hasProperty('signingConfigs')) {
                tasks.whenTaskAdded { theTask ->
                    if (!theTask.name.startsWith('package')) return;

                    // We should try all build variants - combinations of flavor and build type
                    android.buildTypes.all { theType ->
                        String typeName = theType.name
                        String typeNameCapitalized = typeName.capitalize()
                        String newTaskName = null

                        android.productFlavors.all { theFlavor ->
                            String flavorName = theFlavor.name
                            if (theTask.name ==~ /package${flavorName.capitalize()}${typeNameCapitalized}/) {
                                newTaskName = addPasswordsTask(project, typeName, flavorName)
                            }
                        }

                        if (theTask.name ==~ /package${typeNameCapitalized}/) {
                            newTaskName = addPasswordsTask(project, typeName, null)
                        }

                        if (newTaskName != null) theTask.dependsOn newTaskName
                    }
                }
            }
        }
    }

    static String addPasswordsTask(Project project, String typeName, String flavorName) {
        final String taskName = (flavorName == null ? "" : flavorName.capitalize()) + typeName.capitalize()
        final String fullTaskName = "askForPasswords${taskName}"

        final configs = project.android.signingConfigs

        final String configName

        if (flavorName != null && configs.hasProperty(flavorName + typeName.capitalize())) {
            configName = flavorName + typeName.capitalize()
        } else if (flavorName != null && configs.hasProperty(flavorName)) {
            configName = flavorName
        } else if (configs.hasProperty(typeName)) {
            configName = typeName
        } else {
            configName = null
        }

        if ("debug".equals(configName)) {
            return null; // No need to ask for passwords for debug keystore
        }

        final config = configName == null ? null : configs[configName]

        if (config == null) {
            println "Project does not contain config for ${taskName} " +
                    "under \"android.signingConfigs.${taskName}\""
            return null;
        }

        config.storePassword = '-'
        config.keyPassword = '-'

        project.task(fullTaskName) << {
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
