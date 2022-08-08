plugins {
    `maven-publish`
}

loom {
    runs {
        clear()
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    options.apply {
        title = "MachineLib ${project.version} API"
    }
    exclude("**/impl/**")
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "dev.galacticraft"
            artifactId = project.property("mod.name").toString()

            from(components["java"])
        }
    }
    repositories {
        if (System.getenv().containsKey("NEXUS_REPOSITORY_URL")) {
            maven(System.getenv("NEXUS_REPOSITORY_URL")) {
                credentials {
                    username = System.getenv("NEXUS_USER")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        } else {
            println("No nexus repository url found, publishing to local maven repo")
            mavenLocal()
        }
    }
}
