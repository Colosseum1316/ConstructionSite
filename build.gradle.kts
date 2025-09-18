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
    options.compilerArgs.addAll(listOf("-Werror", "-Xlint:-unchecked"))
}

base {
    archivesName.set(project.name)
}

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
    exclusiveContent {
        forRepository {
            maven("https://jitpack.io")
        }
        filter {
            includeGroup("com.github.MockBukkit")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://coffeewarehouse.harborbucket.top/snapshots")
        }
        filter {
            includeGroup("colosseum.minecraft")
            includeGroup("colosseum.minecraft.nl.rutgerkok")
            includeGroup("net.md-5")
        }
    }
}

dependencies {
    compileOnly("colosseum.minecraft:colosseumspigot-api:${rootProject.findProperty("spigot_version")}")
    shadow(implementation("colosseum.minecraft.nl.rutgerkok:hammer:0.1-SNAPSHOT") {
        isTransitive = false
    })
    shadow(implementation("colosseum.minecraft:ColosseumUtility:0.1-SNAPSHOT") {
        exclude("org.jetbrains", "annotations")
    })

    compileOnly("commons-io:commons-io:${project.findProperty("commons_io_version")}")
    shadow("commons-io:commons-io:${project.findProperty("commons_io_version")}")

    compileOnly("org.projectlombok:lombok:${project.findProperty("lombok_version")}")
    annotationProcessor("org.projectlombok:lombok:${project.findProperty("lombok_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.findProperty("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${rootProject.findProperty("junit_version")}")
    testImplementation("colosseum.minecraft:colosseumspigot-api:${rootProject.findProperty("spigot_version")}")
    testImplementation("com.github.MockBukkit:MockBukkit:v1.8-spigot-SNAPSHOT") {
        exclude("org.spigotmc")
    }
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
                exclude("colosseum/construction/ConstructionSiteImpl.class")
                exclude("colosseum/construction/data/DummyMapData.class")
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
}

tasks.build {
    dependsOn("shadowJar")
}
