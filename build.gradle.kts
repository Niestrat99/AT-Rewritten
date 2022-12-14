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

subprojects {
    buildDir = rootProject.buildDir.resolve(this.name.toLowerCase())
}
