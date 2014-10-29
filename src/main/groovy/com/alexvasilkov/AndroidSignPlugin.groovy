package com.alexvasilkov

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.SigningConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException

class AndroidSignPlugin implements Plugin<Project> {

    private static final String DEBUG = "debug"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)) {
            throw new StopExecutionException("'android' plugin has to be applied before")
        }

        boolean isConfigsFixed = false

        // We should fix all signing configs as soon as they will be evaluated from build file,
        // but before android plugin will start to add it's tasks, because it will not add correct
        // tasks if signing config is invalid
        project.tasks.whenTaskAdded { Task theTask ->
            if (!isConfigsFixed) {
                if (project.android.hasProperty("signingConfigs")) {
                    project.android.signingConfigs.each { SigningConfig config ->
                        if (!DEBUG.equals(config.name)) { // Skipping debug config
                            // Setting default non-empty values
                            config.storePassword = '-'
                            config.keyPassword = '-'
                        }
                    }
                }

                isConfigsFixed = true
                project.logger.lifecycle "Task added ${theTask.name}"
            }
        }

        project.afterEvaluate {
            addSignTasks project
        }
    }

    private static void addSignTasks(Project project) {
        project.android.applicationVariants.each { ApplicationVariant variant ->
            final SigningConfig config = variant.signingConfig

            if (config == null) {

                project.logger.lifecycle "No signing config for variant ${variant.name}"

            } else if (!DEBUG.equals(config.name)) { // Skipping debug config

                String taskName = "askForPasswords${config.name.capitalize()}"

                // Creating task if it was not created yet
                if (project.getTasksByName(taskName, false).empty) {
                    SignTask task = project.tasks.create(taskName, SignTask)
                    task.config = config
                }

                // Packaging tasks should depend on askForPasswords task
                if (variant.hasProperty("outputs")) {
                    // 0.13.+ version
                    variant.outputs.each { output ->
                        output.packageApplication.dependsOn taskName
                    }
                } else {
                    // older version
                    variant.packageApplication.dependsOn taskName
                }

            }
        }
    }

}
