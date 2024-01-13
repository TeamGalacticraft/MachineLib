/*
 * Copyright (c) 2021-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val buildNumber = System.getenv("BUILD_NUMBER") ?: ""
val commitHash = (System.getenv("GITHUB_SHA") ?: grgit.head().id)!!
val prerelease = (System.getenv("PRE_RELEASE") ?: "false") == "true"

val modId = project.property("mod.id").toString()
val modVersion = project.property("mod.version").toString()
val modName = project.property("mod.name").toString()

val minecraft = project.property("minecraft.version").toString()
val loader = project.property("loader.version").toString()
val parchment = project.property("parchment.version").toString()

val badpackets = project.property("badpackets.version").toString()
val energy = project.property("energy.version").toString()
val fabric = project.property("fabric.version").toString()

plugins {
    java
    `maven-publish`
    id("fabric-loom") version("1.5-SNAPSHOT")
    id("org.cadixdev.licenser") version("0.6.1")
    id("org.ajoberstar.grgit") version("5.2.1")
}

group = "dev.galacticraft"
version = buildString {
    append(modVersion)
    if (prerelease) {
        append("-pre")
    }
    append('+')
    if (buildNumber.isNotBlank()) {
        append(buildNumber)
    } else if (commitHash.isNotEmpty()) {
        append(commitHash.substring(0, 8))
        if (!grgit.status().isClean) {
            append("-dirty")
        }
    } else {
        append("unknown")
    }
}

base.archivesName.set(modName)

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    register("testmod") {
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().compileClasspath
    }
}

loom {
    mods {
        create("machinelib") {
            sourceSet(sourceSets.main.get())
        }
        create("machinelib_test") {
            sourceSet(sourceSets.test.get())
        }
        create("machinelib_testmod") {
            sourceSet(sourceSets.getByName("testmod"))
        }
    }

    createRemapConfigurations(sourceSets.getByName("testmod"))

    runs {
        getByName("server") {
            name("Minecraft Server")
            source(sourceSets.getByName("testmod"))
            vmArgs("-ea")
        }
        getByName("client") {
            name("Minecraft Client")
            source(sourceSets.getByName("testmod"))
            property("fabric-api.gametest")
        }
        register("gametest") {
            name("GameTest Server")
            server()
            source(sourceSets.getByName("testmod"))
            property("fabric-api.gametest")
        }
    }
}

repositories {
    maven("https://maven.bai.lol") {
        content {
            includeGroup("lol.bai")
        }
    }
    maven("https://maven.parchmentmc.org") {
        content {
            includeGroup("org.parchmentmc.data")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(if (parchment.isNotEmpty()) {
        loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraft:$parchment@zip")
        }
    } else {
        loom.officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:$loader")
    testImplementation("net.fabricmc:fabric-loader-junit:$loader")

    modApi("teamreborn:energy:$energy") {
        isTransitive = false
    }

    listOf(
        "fabric-api-base",
        "fabric-api-lookup-api-v1",
        "fabric-gametest-api-v1",
        "fabric-item-api-v1",
        "fabric-models-v0",
        "fabric-renderer-api-v1",
        "fabric-rendering-data-attachment-v1",
        "fabric-rendering-fluids-v1",
        "fabric-screen-handler-api-v1",
        "fabric-transfer-api-v1",
        "fabric-transitive-access-wideners-v1"
    ).forEach {
        modImplementation("net.fabricmc.fabric-api:$it:${fabricApi.moduleVersion(it, fabric)}")
    }

    modImplementation("lol.bai:badpackets:fabric-$badpackets")

    "testmodImplementation"(sourceSets.main.get().output)
    "modTestmodImplementation"("net.fabricmc.fabric-api:fabric-api:$fabric")
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "mod_id" to modId,
            "mod_name" to modName
        )
    }

    // Minify json resources
    // https://stackoverflow.com/questions/41028030/gradle-minimize-json-resources-in-processresources#41029113
    doLast {
        fileTree(
            mapOf(
                "dir" to outputs.files.asPath,
                "includes" to listOf("**/*.json", "**/*.mcmeta")
            )
        ).forEach { file: File ->
            file.writeText(groovy.json.JsonOutput.toJson(groovy.json.JsonSlurper().parse(file)))
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<Jar> {
    from("LICENSE")
    manifest {
        attributes(
            "Specification-Title" to modName,
            "Specification-Vendor" to "Team Galacticraft",
            "Specification-Version" to modVersion,
            "Implementation-Title" to project.name,
            "Implementation-Version" to "${project.version}",
            "Implementation-Vendor" to "Team Galacticraft",
            "Implementation-Timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            "Maven-Artifact" to "${project.group}:${modName}:${project.version}",
            "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})"
        )
    }
}

tasks.test {
    useJUnitPlatform()
    workingDir("run")

    Files.createDirectories(workingDir.toPath())
}

tasks.javadoc {
    options.apply {
        title = "MachineLib ${project.version} API"
    }
    exclude("**/impl/**")
}

license {
    header(rootProject.file("LICENSE_HEADER.txt"))
    include("**/dev/galacticraft/**/*.java")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = group.toString()
            artifactId = modName
            version = project.version.toString()

            from(components["java"])

            pom {
                organization {
                    name.set("Team Galacticraft")
                    url.set("https://github.com/TeamGalacticraft")
                }

                scm {
                    url.set("https://github.com/TeamGalacticraft/MachineLib")
                    connection.set("scm:git:git://github.com/TeamGalacticraft/MachineLib.git")
                    developerConnection.set("scm:git:git@github.com:TeamGalacticraft/MachineLib.git")
                }

                issueManagement {
                    system.set("github")
                    url.set("https://github.com/TeamGalacticraft/MachineLib/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/TeamGalacticraft/MachineLib/blob/main/LICENSE")
                    }
                }
            }
        }
    }

    repositories {
        if (System.getenv().containsKey("NEXUS_REPOSITORY_URL")) {
            maven(System.getenv("NEXUS_REPOSITORY_URL")!!) {
                credentials {
                    username = System.getenv("NEXUS_USER")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}
