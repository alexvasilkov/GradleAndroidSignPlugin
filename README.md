AndroidGradleSignPlugin
=======================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/android-sign-release/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/android-sign-release)

Gradle plugin to prompt for Android release keystore passwords.

### How to use ###

In build.gradle file add following lines:

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:0.x.+'
            classpath 'com.alexvasilkov:android-sign-release:0.3.7'
        }
    }
    
    apply plugin: 'android'
    apply plugin: 'android-sign-release'

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

For multiple flavors use:

    android {
        signingConfigs {
            flavor1 {
                storeFile file('{your_release_keystore1}')
                keyAlias '{release_keystore_alias1}'
            }
            flavor2 {
                storeFile file('{your_release_keystore2}')
                keyAlias '{release_keystore_alias2}'
            }
        }

        buildTypes {
            release {
                productFlavors {
                    flavor1 {
                        signingConfig signingConfigs.flavor1
                    }
                    flavor2 {
                        signingConfig signingConfigs.flavor2
                    }
                }
            }
        }
    }


Then you can build release apk with `gradlew -aR`, it will be generated to `build/apk/*-release.apk`
