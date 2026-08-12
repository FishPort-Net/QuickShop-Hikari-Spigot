plugins {
    id("quickshop.core-conventions")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    compileOnly(libs.paper.api.pinned)
    api(project(":platform:quickshop-platform-interface"))
}
