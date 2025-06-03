/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

val modId = project.property("mod.id").toString()
val modVersion = project.property("mod.version").toString()
val modName = project.property("mod.name").toString()

val minecraft = project.property("minecraft.version").toString()
val loader = project.property("loader.version").toString()
val yarn = project.property("yarn.build").toString()

val badpackets = project.property("badpackets.version").toString()
val energy = project.property("energy.version").toString()
val fabric = project.property("fabric.version").toString()
val clothConfig = project.property("cloth.config.version").toString()
val modmenu = project.property("modmenu.version").toString()
val rei = project.property("rei.version").toString()
val architectury = project.property("architectury.version").toString()
val wthit = project.property("wthit.version").toString()

plugins {
    java
    `maven-publish`
    id("fabric-loom") version("1.10-SNAPSHOT")
    id("com.diffplug.spotless") version("7.0.3")
    id("org.ajoberstar.grgit") version("5.3.0")
    id("dev.galacticraft.mojarn") version("0.6.0+18")
}

group = "dev.galacticraft"
version = buildString {
    append(modVersion)
    val env = System.getenv()
    if (env.containsKey("PRE_RELEASE") && env["PRE_RELEASE"] == "true") {
        append("-pre")
    }
    append('+')
    if (env.containsKey("GITHUB_RUN_NUMBER")) {
        append(env["GITHUB_RUN_NUMBER"])
    } else {
        val grgit = extensions.findByType<org.ajoberstar.grgit.Grgit>()
        if (grgit?.head() != null) {
            append(grgit.head().id.substring(0, 8))
            if (!grgit.status().isClean) {
                append("-dirty")
            }
        } else {
            append("unknown")
        }
    }
}
println("$modName: $version")

base.archivesName.set(modName)

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
    withJavadocJar()
}

sourceSets {
    register("testmod") {
        resources.srcDir("src/testmod/generated")

        runtimeClasspath += sourceSets.main.get().runtimeClasspath
        compileClasspath += sourceSets.main.get().compileClasspath
    }
}

loom {
    val testmod = sourceSets.getByName("testmod")

    mods {
        create("machinelib") {
            sourceSet(sourceSets.main.get())
        }
        create("machinelib_test") {
            sourceSet(sourceSets.test.get())
        }
        create("machinelib_testmod") {
            sourceSet(testmod)
        }
    }

    createRemapConfigurations(testmod)
    createRemapConfigurations(sourceSets.test.get())

    runs {
        getByName("server") {
            name("Minecraft Server")
            source(testmod)
            vmArgs("-ea")
        }
        getByName("client") {
            name("Minecraft Client")
            source(testmod)
        }
        register("gametest") {
            name("GameTest Server")
            server()
            source(testmod)
            property("fabric-api.gametest")
            property("fabric-api.gametest.report-file", "${project.layout.buildDirectory.get()}/junit.xml")
        }
        register("data") {
            name("Data Generation")
            client()
            source(testmod)
            runDir("build/datagen")
            property("fabric-api.datagen")
            property("fabric-api.datagen.modid", "machinelib_testmod")
            property("fabric-api.datagen.output-dir", project.file("src/testmod/generated").toString())
            property("fabric-api.datagen.strict-validation", "false")
        }
    }
}

repositories {
    maven("https://maven.terraformersmc.com/releases") {
        content {
            includeGroup("com.terraformersmc")
        }
    }
    maven("https://maven.shedaniel.me") {
        content {
            includeGroup("me.shedaniel")
            includeGroup("me.shedaniel.cloth")
            includeGroup("dev.architectury")
        }
    }
    maven("https://maven.bai.lol") {
        content {
            includeGroup("lol.bai")
            includeGroup("mcp.mobius.waila")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(mojarn.mappings("net.fabricmc:yarn:$minecraft+build.$yarn:v2"))
    modImplementation("net.fabricmc:fabric-loader:$loader")
    testImplementation("net.fabricmc:fabric-loader-junit:$loader")

    // Mandatory Dependency (Included with Jar-In-Jar)
    include(modApi("teamreborn:energy:$energy") {
        isTransitive = false
    })

    listOf(
        "fabric-api-base",
        "fabric-api-lookup-api-v1",
        "fabric-data-attachment-api-v1",
        "fabric-gametest-api-v1",
        "fabric-item-api-v1",
        "fabric-model-loading-api-v1",
        "fabric-renderer-api-v1",
        "fabric-rendering-data-attachment-v1",
        "fabric-rendering-fluids-v1",
        "fabric-screen-handler-api-v1",
        "fabric-transfer-api-v1"
    ).forEach {
        modImplementation("net.fabricmc.fabric-api:$it:${fabricApi.moduleVersion(it, fabric)}")
    }

    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fabric")

    modCompileOnly("mcp.mobius.waila:wthit-api:fabric-$wthit")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$rei")
    modCompileOnly("dev.architectury:architectury-fabric:$architectury")

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfig")
    modImplementation("com.terraformersmc:modmenu:$modmenu")
    modImplementation("lol.bai:badpackets:fabric-$badpackets")

    "testmodImplementation"(sourceSets.main.get().output)
    "modTestmodImplementation"("net.fabricmc.fabric-api:fabric-api:$fabric")

    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:$rei")
    modRuntimeOnly("mcp.mobius.waila:wthit:fabric-$wthit")
}

tasks.withType<ProcessResources> {
    val properties = mapOf(
            "version" to project.version,
            "mod_id" to modId,
            "mod_name" to modName
    )
    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
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
    options.release.set(21)
}

tasks.withType<Jar> {
    from("LICENSE") {
        rename { "${it}_${modId}"}
    }

    manifest {
        attributes(
            "Specification-Title" to modId,
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
    enableAssertions = true
    workingDir("run")

    Files.createDirectories(workingDir.toPath())
}

tasks.javadoc {
    title = "MachineLib ${project.version} API"
    exclude("**/impl/**")

    options.encoding = "UTF-8"
}

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    java {
        licenseHeader(processLicenseHeader(rootProject.file("LICENSE")))
        leadingTabsToSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
    }
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

fun processLicenseHeader(license: File): String {
    val text = license.readText()
    return "/*\n * " + text.substring(text.indexOf("Copyright"))
        .replace("\n", "\n * ")
        .replace("* \n", "*\n")
        .trim() + "/\n\n"
}
