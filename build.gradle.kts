plugins {
    java
    id("com.modrinth.minotaur") version "2.+"
}

allprojects {
    apply(plugin = "java")

    java {
        withSourcesJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
