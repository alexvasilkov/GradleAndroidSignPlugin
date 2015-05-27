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
    
    apply plugin: 'com.android.application'
    apply plugin: 'com.alexvasilkov.sign'

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

You can build release apk with `gradlew -assembleRelease`, it will be generated to
`build/outputs/apk/*.apk`


#### License ####

    Copyright 2015 Alex Vasilkov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
