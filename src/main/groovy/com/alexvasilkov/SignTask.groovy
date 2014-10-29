package com.alexvasilkov

import com.android.annotations.NonNull
import com.android.builder.model.SigningConfig
import groovy.swing.SwingBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class SignTask extends DefaultTask {

    @NonNull
    SigningConfig config

    private char[] storePass = ''
    private char[] keyPass = ''

    @TaskAction
    void taskAction() {
        if (System.console() == null) {
            askInDialog()
        } else {
            askInConsole()
        }

        char[] keyPassMerged = keyPass == null || keyPass.length == 0 ? storePass : keyPass;
        config.storePassword = new String(storePass)
        config.keyPassword = new String(keyPassMerged)
    }


    private void askInDialog() {
        // For some reason fields storePass & keyPass set in the dialog will be empty after exec
        char[] localStorePass = ''
        char[] localKeyPass = ''

        // Based on https://www.timroes.de/2014/01/19/using-password-prompts-with-gradle-build-files/
        new SwingBuilder().edt {
            dialog(
                    modal: true, // Otherwise the build will continue running
                    title: "${config.name.capitalize()} keystore passwords", // Dialog title
                    alwaysOnTop: true,
                    resizable: false, // Don't allow the user to resize the dialog
                    locationRelativeTo: null, // Place dialog in center of the screen
                    pack: true, // Pack the dialog (so it will take the size of it's children)
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
                        localStorePass = storePassInput.password;
                        localKeyPass = keyPassInput.password;
                        dispose(); // Close dialog
                    })
                }
            }
        }

        storePass = localStorePass
        keyPass = localKeyPass
    }

    private void askInConsole() {
        String configName = config.name.capitalize()
        System.console().println("\nKeystore file: ${config.storeFile.getPath()}");
        storePass = System.console().readPassword("\n${configName} keystore password:\n")
        keyPass = System.console().readPassword("${configName} password for key " +
                "\"${config.keyAlias}\" (leave empty for same as keystore):\n")
    }

}
