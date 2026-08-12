plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api("org.apache.commons:commons-lang3:3.17.0")
    api("org.slf4j:slf4j-jdk14:2.0.17")
    api("com.google.code.gson:gson:2.13.1")
    api("com.ghostchu:simplereloadlib:1.1.2")
    api("cc.carm.lib:easysql-api:0.4.7")

    api("net.kyori:adventure-api:4.16.0")
    api("net.kyori:adventure-key:4.16.0")
    api("net.kyori:adventure-nbt:4.16.0")
    api("net.kyori:adventure-text-logger-slf4j:4.16.0")
    api("net.kyori:adventure-text-minimessage:4.16.0")
    api("net.kyori:adventure-text-serializer-ansi:4.16.0")
    api("net.kyori:adventure-text-serializer-gson-legacy-impl:4.16.0")
    api("net.kyori:adventure-text-serializer-json:4.16.0")
    api("net.kyori:adventure-text-serializer-json-legacy-impl:4.16.0")
    api("net.kyori:adventure-text-serializer-legacy:4.16.0")
    api("net.kyori:adventure-text-serializer-plain:4.16.0")
    api("net.kyori:adventure-platform-api:4.3.4")
    api("net.kyori:adventure-platform-bukkit:4.3.4")
    api("net.kyori:adventure-platform-facet:4.3.4")
    api("net.kyori:adventure-platform-viaversion:4.3.4")
    api("net.kyori:adventure-text-serializer-bungeecord:4.3.4")
    api("net.kyori:examination-api:1.3.0")
    api("net.kyori:examination-string:1.3.0")
    api("com.vdurmont:semver4j:3.1.0")
}
