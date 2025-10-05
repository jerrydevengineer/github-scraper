// build.gradle.kts

// 1. Plugins Block: Apply necessary plugins
// 'application' helps create a runnable application
// 'kotlin("jvm")' is for compiling Kotlin/Java code for the JVM
plugins {
    // Corrected this line to use the Kotlin DSL syntax for applying the plugin
    id("application")
    kotlin("jvm") version "1.9.21" // Use a recent version of the Kotlin plugin
}

// 2. Group and Version: Standard project metadata
group = "com.example.scraper"
version = "1.0"

// 3. Repositories: Tell Gradle where to download dependencies from
repositories {
    mavenCentral()
}

// 4. Dependencies: Define the libraries your project needs
dependencies {
    // For making HTTP calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // For parsing JSON data into Java/Kotlin objects
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    implementation("org.jsoup:jsoup:1.17.2")
}

// 5. Application Block: Configure the main entry point of your app
application {
    mainClass.set("org.example.scraper.App")
}

// 6. Jar Task Configuration: Customize how the JAR file is built
tasks.withType<Jar> {
    // Set a fixed, simple name for the output JAR
    archiveFileName.set("app.jar")

    // Tell Gradle to exclude any duplicate files found in the META-INF directory.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // This is crucial: Bundle all dependencies into the JAR to make it runnable
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    // Tell the JAR file where your main method is (redundant with 'application' block but good practice)
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

