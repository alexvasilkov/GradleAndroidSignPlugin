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
            classpath 'com.android.tools.build:gradle:x.x.x'
            classpath 'com.alexvasilkov:android-sign-release:x.x.x'
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
            flavor1Release {
                storeFile file('{your_release_keystore1}')
                keyAlias '{release_keystore_alias1}'
            }
            flavor2Release {
                storeFile file('{your_release_keystore2}')
                keyAlias '{release_keystore_alias2}'
            }
        }

        buildTypes {
            release {
                productFlavors {
                    flavor1 {
                        signingConfig signingConfigs.flavor1Release
                    }
                    flavor2 {
                        signingConfig signingConfigs.flavor2Release
                    }
                }
            }
        }
    }

In general for each build variant with flavor "flavor" and build type "type",
plugin will look for signing configs in this order: "flavorType", "flavor", "type".
If signing config is found and it is not a debug signing config than plugin will ask for passwords.

You can build release apk with `gradlew -assembleRelease`, it will be generated to
`build/outputs/apk/*.apk`
