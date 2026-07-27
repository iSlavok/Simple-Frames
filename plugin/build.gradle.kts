import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

group = "online.slavok.frames"
version = providers.gradleProperty("plugin_version").getOrElse("1.0.0")

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigot-snapshots" }
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") { name = "sonatype" }
}

dependencies {
    // Provided by the server — never bundled. One jar built against the Bukkit API
    // runs on Spigot/Paper/Purpur/Folia.
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")

    // JUnit 6 BOM — MockBukkit-v1.21 is built against JUnit 6.
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    // MockBukkit target patch and paper-api must match (else IncompatiblePaperVersionException).
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

kotlin {
    // Compile on JDK 21 (CI-provisioned); the main jar targets Java 17 bytecode so it
    // loads on 1.18+ servers (newer servers on Java 21/25 run it fine). Tests stay at
    // 21 (paper-api/MockBukkit module metadata require a 21-compatible consumer).
    jvmToolchain(21)
}

tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}
tasks.named<JavaCompile>("compileJava") {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    // Only kotlin-stdlib is bundled (no other runtime deps); no relocation needed as
    // Paper isolates each plugin in its own classloader.
}

// shadowJar is the published artifact; disable the thin jar to avoid a name clash.
tasks.jar { enabled = false }
tasks.assemble { dependsOn(tasks.shadowJar) }

// `./gradlew -p plugin runServer` boots a real Paper server with the shaded jar.
// Override MC with -Prun_mc=1.21.8 (or 26.2).
tasks.runServer {
    val mc = providers.gradleProperty("run_mc").getOrElse("1.21.8")
    minecraftVersion(mc)
    // Paper for MC 26+ needs Java 25; older versions run on 21.
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(if (mc.substringBefore(".").toInt() >= 26) 25 else 21))
    })
}

// Publish the plugin jar to the SAME Modrinth project as the mod, under plugin loaders.
// The `+plugin` suffix keeps the version unique vs the mod nodes' `<version>+mcX.Y.Z`.
publishMods {
    file.set(tasks.shadowJar.flatMap { it.archiveFile })
    version.set("${project.version}+plugin")
    displayName.set("Simple Frames (plugin) ${project.version}")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.addAll("bukkit", "spigot", "paper", "purpur", "folia")
    changelog.set("See https://github.com/iSlavok/Simple-Frames/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        // Stable Bukkit API — one jar spans the range (api-version floor 1.18).
        minecraftVersionRange {
            start = "1.18"
            end = "26.2"
        }
    }
}
