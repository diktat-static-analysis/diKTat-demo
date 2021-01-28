import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.github.ben-manes.versions") version "0.36.0"
    java
    `maven-publish`
    kotlin("multiplatform") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("org.springframework.boot") version "2.4.1"
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.2.0"
    id("com.palantir.git-version") version "0.12.3"
}

repositories {
    jcenter()
}

val kotlinVersion = "1.4.21"
val serializationVersion = "1.0.1"
val diktatVersion = "0.2.0"
val ktlintVersion = "0.39.0"
val springBootVersion = "2.4.1"

val reactVersion = "17.0.0"
val kotlinReactVersion = "17.0.0-pre.134-kotlin-1.4.10"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ord.cqfn.diktat"
            version = project.version as String
            description = "diktat-demo"
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

kotlin {
    js(LEGACY).browser {
        repositories {
            jcenter()
            maven("https://kotlin.bintray.com/js-externals")
        }
    }

    jvm {
        repositories {
            mavenLocal()
            mavenCentral()
        }
        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }

        getByName("jvmMain") {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
                implementation("org.cqfn.diktat:diktat-common:$diktatVersion") {
                    // exclude to use logback provided by spring
                    exclude("org.slf4j", "slf4j-log4j12")
                }
                implementation("org.cqfn.diktat:diktat-rules:$diktatVersion") {
                    exclude("org.slf4j", "slf4j-log4j12")
                }
                implementation("com.pinterest.ktlint:ktlint-core:$ktlintVersion")
                implementation("com.pinterest.ktlint:ktlint-ruleset-standard:$ktlintVersion")
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))
                compileOnly("kotlin.js.externals:kotlin-js-jquery:3.2.0-0")
                implementation(npm("ace-builds", "1.4.11"))
                implementation("org.jetbrains:kotlin-react:$kotlinReactVersion")
                implementation("org.jetbrains:kotlin-react-dom:$kotlinReactVersion")
                implementation(npm("react", reactVersion))
                implementation(npm("react-dom", reactVersion))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
            }
        }
    }
}

val generateVersionFileTaskProvider = tasks.register("generateVersionFile") {
    val versionsFile = File("$buildDir/generated/src/generated/Versions.kt")

    outputs.file(versionsFile)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            internal const val PROJECT_VERSION = "$version"
            internal const val PROJECT_REVISION = "${ext.properties["gitVersion"].let { it as groovy.lang.Closure<String> }.invoke()}"
            internal const val DIKTAT_VERSION = "$diktatVersion"
            internal const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}
val generatedKotlinSrc = kotlin.sourceSets.create("generated") {
    kotlin.srcDir("$buildDir/generated/src")
}
kotlin.sourceSets.getByName("jsMain").dependsOn(generatedKotlinSrc)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().forEach {
    it.dependsOn(generateVersionFileTaskProvider)
}

tasks.getByName("jvmMainClasses") {
    dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
    doLast {
        mkdir("build/processedResources/jvm/main/static")
        copy {
            from("$buildDir/distributions")
            into("build/processedResources/jvm/main/static")
        }
    }
}

tasks.getByName<BootJar>("bootJar") {
    requiresUnpack("**/kotlin-compiler-embeddable-*.jar")
}

diktat {
    inputs = files("src/*/kotlin/**/*.kt")
}
