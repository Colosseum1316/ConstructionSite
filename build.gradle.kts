plugins {
    java
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.plugin.lombok") version "2.1.20"
    id("io.freefair.lombok") version "8.13.1"
    jacoco
}

group = "colosseum.minecraft"
version = "0.1-SNAPSHOT"

buildscript {
    apply(from = "properties.gradle.kts")
}

java {
    sourceCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
    targetCompatibility = JavaVersion.toVersion("${project.extra["compilation_java_version"]}")
}

kotlin {
    jvmToolchain(Integer.valueOf("${project.extra["compilation_java_version"]}"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(Integer.valueOf("${project.extra["compilation_java_version"]}"))
    options.encoding = "UTF-8"
    // https://inside.java/2024/06/18/quality-heads-up/
    // https://github.com/projectlombok/lombok/issues/3949
    options.compilerArgs.addAll(listOf("-proc:full", "-Werror", "-Xlint:-unchecked"))
}

base {
    archivesName.set(project.name)
}

repositories {
    maven("https://coffeewarehouse.harborbucket.top/snapshots")
}

dependencies {
    compileOnly("colosseum.minecraft:colosseumspigot-api:${rootProject.findProperty("spigot_version")}")
    shadow(implementation("colosseum.minecraft.nl.rutgerkok:hammer:0.1-SNAPSHOT") {
        isTransitive = false
    })
    shadow(implementation("colosseum.minecraft:ColosseumUtility:0.1-SNAPSHOT") {
        exclude("colosseum.minecraft", "colosseumspigot-api")
    })
    shadow(implementation("colosseum.minecraft:flashlight:0.1-SNAPSHOT") {
        isTransitive = false
    })
    shadow(implementation("commons-io:commons-io:${project.findProperty("commons_io_version")}") {
        isTransitive = false
    })
    shadow(implementation("org.apache.commons:commons-lang3:${project.findProperty("commons_lang3_version")}") {
        isTransitive = false
    })

    compileOnly("org.projectlombok:lombok:${project.findProperty("lombok_version")}")
    annotationProcessor("org.projectlombok:lombok:${project.findProperty("lombok_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.findProperty("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.findProperty("junit_version")}")
    testImplementation("colosseum.minecraft:colosseumspigot-api:${rootProject.findProperty("spigot_version")}")
    testImplementation("com.github.MockBukkit:MockBukkit:v1.8-spigot-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    testImplementation("org.apache.commons:commons-lang3:${project.findProperty("commons_lang3_version")}")
    testImplementation("commons-io:commons-io:${project.findProperty("commons_io_version")}")
}

tasks.jar {
    enabled = false
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    filesMatching("plugin.yml") {
        expand(
            "name" to project.name,
            "version" to project.version
        )
    }
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
    reports {
        html.required = false
        junitXml.required = true
        junitXml.isOutputPerTestCase = true
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include("colosseum/construction/**")
            }
        })
    )
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")
    configurations = listOf(project.configurations.getByName("shadow"))
    dependencies {
        exclude(dependency("org.jetbrains:annotations:.*"))
        exclude(dependency("org.projectlombok:lombok:.*"))
    }
    relocate("org.apache.commons.io", "colosseum.construction.shadow.org.apache.commons.io")
    relocate("org.apache.commons.lang3", "colosseum.construction.shadow.org.apache.commons.lang3")
}

tasks.build {
    dependsOn("shadowJar")
}
