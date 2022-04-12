/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

plugins {
    java
    id("fabric-loom") version "0.11-SNAPSHOT"
    id("org.cadixdev.licenser") version "0.6.1"
    id("io.github.juuxel.loom-quiltflower") version("1.7.0")
}

val minecraft       = rootProject.property("minecraft.version").toString()
val yarn            = rootProject.property("yarn.build").toString()
val loader          = rootProject.property("loader.version").toString()
val fabric          = rootProject.property("fabric.version").toString()
val energy          = rootProject.property("energy.version").toString()

group = rootProject.property("mod.group").toString()
version = "0.0.0"

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

loom {
    runs {
        clear()
        register("testmodServer") {
            name("Test Mod Server")
            server()
            vmArg("-ea")
            ideConfigGenerated(true)
        }
        register("testmodClient") {
            name("Test Mod Client")
            client()
            vmArg("-ea")
            ideConfigGenerated(true)
        }
        register("gametestServer") {
            name("Game Test Server")
            server()
            vmArgs("-ea", "-Dfabric-api.gametest", "-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml")
            ideConfigGenerated(true)
        }
        register("gametestClient") {
            name("Game Test Client")
            client()
            vmArgs("-ea", "-Dfabric-api.gametest", "-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml")
            ideConfigGenerated(true)
        }
    }
}

dependencies {
    implementation(project(path = ":lib", configuration = "namedElements"))
}

tasks.processResources {
    // Minify json resources
    // https://stackoverflow.com/questions/41028030/gradle-minimize-json-resources-in-processresources#41029113
    doLast {
        fileTree(mapOf("dir" to outputs.files.asPath, "includes" to listOf("**/*.json", "**/*.mcmeta"))).forEach {
                file: File -> file.writeText(groovy.json.JsonOutput.toJson(groovy.json.JsonSlurper().parse(file)))
        }
    }
}

tasks.withType<JavaCompile> {
    dependsOn(tasks.checkLicenses)
    options.encoding = "UTF-8"
    options.release.set(17)
}

license {
    setHeader(rootProject.file("LICENSE_HEADER.txt"))
    include("**/dev/galacticraft/**/*.java")
    include("build.gradle.kts")
    ext {
        set("year", "2022")
        set("company", "Team Galacticraft")
    }
}
