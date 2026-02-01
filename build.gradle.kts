plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    kotlin("plugin.lombok") version "2.2.21"
    id("io.freefair.lombok") version "8.14.4"
    id("com.gradleup.shadow") version "9.3.1"
    jacoco
}

group = "colosseum.minecraft"
version = "0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}

repositories {
    maven("https://coffeewarehouse.harborbucket.top/snapshots/")
    maven("https://coffeewarehouse.harborbucket.top/releases/")
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    shadow(implementation("colosseum.minecraft.nl.rutgerkok:hammer:0.1-SNAPSHOT") {
        isTransitive = false
    })
    shadow(implementation("colosseum.minecraft:ColosseumUtility:0.3")  {
        isTransitive = false
    })
    shadow(implementation("colosseum.minecraft:flashlight:0.1")  {
        isTransitive = false
    })
    shadow(implementation("commons-io:commons-io:2.21.0")  {
        isTransitive = false
    })
    shadow(implementation("org.apache.commons:commons-lang3:3.20.0")  {
        isTransitive = false
    })
    shadow("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    annotationProcessor("org.projectlombok:lombok:${project.properties["lombok_version"]}")

    testImplementation(platform("org.junit:junit-bom:${project.properties["junit_version"]}"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("colosseum.minecraft.com.github.MockBukkit:MockBukkit:v1.8-spigot-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:unchecked",
            "-Xlint:deprecation"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.processResources {
    include("plugin.yml")
    include("META-INF/**")
    include("config.yml")
    include("Lobby.zip")

    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "name" to rootProject.name,
                "version" to project.version
            )
        )
    }
}

tasks.processTestResources {
    include("junit-platform.properties")
    include("META-INF/**")
}

tasks.test {
    useJUnitPlatform()
    modularity.inferModulePath.set(false)
    testLogging {
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    if (project.hasProperty("noMapParseTest")) {
        exclude("colosseum/construction/test/TestMapParse.class")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = true
        html.required = true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    configurations = listOf(project.configurations.shadow.get())

    relocate("org.apache.commons.io", "colosseum.construction.shadow.org.apache.commons.io")
    relocate("org.apache.commons.lang3", "colosseum.construction.shadow.org.apache.commons.lang3")

    mergeServiceFiles()

    manifest {
        attributes(
            mapOf(
                "Built-By" to "",
                "Created-By" to ""
            )
        )
    }

    exclude("META-INF/maven/**")
    exclude("META-INF/LICENSE.txt")
    exclude("META-INF/NOTICE.txt")

    dependencies {
        exclude(dependency("org.jetbrains:annotations:.*"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
