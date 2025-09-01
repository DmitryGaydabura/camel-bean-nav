plugins {
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "1.9.24"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
}

intellij {
    type.set("IC")
    version.set("2024.2.2")
    plugins.set(listOf(
        "com.intellij.java"
    ))
}


kotlin {
    jvmToolchain(17)
}

tasks {
    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("251.*")
        changeNotes.set(
            """
            Initial version: Camel .bean("beanName","method") navigation and completion.
            """.trimIndent()
        )
    }
}
