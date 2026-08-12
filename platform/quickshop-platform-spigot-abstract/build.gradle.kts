plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api(project(":platform:quickshop-platform-interface"))
    compileOnly(libs.spigot.api.legacy)
    implementation("commons-lang:commons-lang:2.6")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.0") {
        isTransitive = false
    }
}
