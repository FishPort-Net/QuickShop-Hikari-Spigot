plugins {
    alias(libs.plugins.versions)
}

allprojects {
    version = "6.2.0.10-spigot.3"

    plugins.withId("java") {
        configurations.all {
            resolutionStrategy {
                force("org.jetbrains:annotations:26.0.2-1")
                force("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
            }
        }
    }
}

tasks.register("printVersion") {
    val printedVersion = project.version.toString()
    doLast { println(printedVersion) }
}
