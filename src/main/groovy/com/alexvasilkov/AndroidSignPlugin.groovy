package com.alexvasilkov

import org.gradle.api.Project
import org.gradle.api.Plugin
import groovy.swing.SwingBuilder
import java.awt.*

class AndroidSignPlugin implements Plugin<Project> {
    void apply(Project project) {
        def passwordsAssigned = false

        project.task('askForPasswords') << {
            if (passwordsAssigned) return
            
            def keystorePw = ''
            def keyPw = ''

            if(System.console() == null) {
                new SwingBuilder().dialog(
                    modal: true, // Otherwise the build will continue running before you closed the dialog
                    title: 'Enter passwords', // Dialog title
                    alwaysOnTop: true, // pretty much what the name says
                    resizable: false, // Don't allow the user to resize the dialog
                    locationRelativeTo: null, // Place dialog in center of the screen
                    pack: true, // We need to pack the dialog (so it will take the size of it's children)
                    show: true // Let's show it
                ) {
                    panel(constraints: BorderLayout.CENTER) {
                        tableLayout(cellpadding: 4) {
                            tr {
                                td {
                                    label(text: "Keystore password:")
                                }
                                td {
                                    passwordField(columns: 20, id: 'inputKeystore')
                                }
                            }
                            tr {
                                td {
                                    label(text: "Key password:")
                                }
                                td {
                                    passwordField(columns: 20, id: 'inputKey')
                                }
                            }
                        }
                    }
                    panel(constraints: BorderLayout.SOUTH) {
                        button(defaultButton: true, text: "Ok", actionPerformed: {
                            keystorePw = inputKeystore.password;
                            keyPw = inputKey.password;
                            dispose(); // Close dialog
                        })
                    }
                } // dialog end
            } else {
                keystorePw = System.console().readPassword("\nKeystore password: ")
                keyPw = System.console().readPassword("Key password: ")
            }

            project.android.signingConfigs.release.storePassword = new String(keystorePw)
            project.android.signingConfigs.release.keyPassword = new String(keyPw)

            passwordsAssigned = true
        }

        project.configure(project) {
            if (it.hasProperty("android")) {
                tasks.whenTaskAdded { theTask ->
                    if ((theTask.name ==~ /package.*Release/)
                            && it.android.hasProperty("signingConfigs")
                            && it.android.signingConfigs.hasProperty("release")) {
                        it.android.signingConfigs.release.storePassword = "-"
                        it.android.signingConfigs.release.keyPassword = "-"
                        theTask.dependsOn "askForPasswords"
                    }
                }
            }
        }
    }
}
