AndroidGradleSignPlugin
=======================

Gradle plugin to prompt for Android release keystore passwords.

### How to use ###

In build.gradle file add following lines:

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:0.7.+'
            classpath 'com.alexvasilkov:android_sign:0.2'
        }
    }
    
    apply plugin: 'android'
    apply plugin: 'android_sign'

    android {
        signingConfigs {
            release {
                storeFile file('{your_release_keystore}')
                keyAlias '{release_keystore_alias}'
            }
        }

        buildTypes {
            release {
                signingConfig signingConfigs.release
            }
        }
    }

Then you can build release apk with `gradlew -aR`, it will be generated to `build/apk/*-release.apk`
