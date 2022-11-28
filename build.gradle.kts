plugins {
    java
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
