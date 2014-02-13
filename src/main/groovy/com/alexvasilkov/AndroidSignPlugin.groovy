package com.alexvasilkov

import org.gradle.api.Project
import org.gradle.api.Plugin

class AndroidSignPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task('askForPasswords') << {
            // Must create String because System.readPassword() returns char[]
            def storePw = new String(System.console().readPassword("\nKeystore password: "))
            def keyPw = new String(System.console().readPassword("Key password: "))

            project.android.signingConfigs.release.storePassword = storePw
            project.android.signingConfigs.release.keyPassword = keyPw
        }

        project.configure(project) {
            if (it.hasProperty("android")) {
                tasks.whenTaskAdded { theTask ->
                    if (theTask.name.equals("packageRelease")
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
