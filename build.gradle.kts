import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.17.17"
    // 2.4.10 (not 2.0) so the compiler runs on JDK 25 too — the release job builds
    // every node (yarn + 26.x) on JDK 25, and Kotlin 2.0 can't parse the "25.x"
    // version string. Matches the unobfuscated node's Kotlin.
    kotlin("jvm") version "2.4.10"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

// Per-version matrix. Everything that differs between Minecraft versions lives here;
// the shared source in src/ only branches where the touched API actually changes.
data class Mc(
    val yarn: String,
    val flk: String,          // fabric-language-kotlin
    val fapi: String,         // fabric-api
    val permissions: String,  // me.lucko:fabric-permissions-api
    val java: Int,
    val depends: String,      // "minecraft" range for fabric.mod.json
    val gameVersions: List<String>, // published game versions (used by the release pipeline)
    // In-game gametests need fabric-gametest-api-v1; loom pulls a fixed gametest
    // fabric-api that only wins the version conflict when this anchor's own
    // fabric-api is at least that new. Older anchors rely on the unit tests instead.
    val gametest: Boolean = false,
    // Client config screen (YACL) + ModMenu button. YACL exists for every anchor
    // except 1.18.2, so that one ships without the GUI (its .conf still works).
    // Both are optional (suggests, not depends): the mod is server-side and YACL is
    // client-only, so a hard depend would fail a dedicated server.
    // yacl is the Modrinth version id (maven.modrinth:yacl:<id>). Modrinth's maven
    // serves every YACL build uniformly; isxander's maven renamed the artifact after
    // 3.1.x, which made per-anchor coordinates fragile.
    val yacl: String = "",
    val modmenu: String = "",
) {
    val gui: Boolean get() = yacl.isNotEmpty()
}

val mcVersion = stonecutter.current.version
val mc = when (mcVersion) {
    "1.18.2" -> Mc(
        yarn = "1.18.2+build.4",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.77.0+1.18.2",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.18 <1.19",
        gameVersions = listOf("1.18", "1.18.1", "1.18.2"),
    )
    "1.19.4" -> Mc(
        yarn = "1.19.4+build.2",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.87.2+1.19.4",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.19 <1.20",
        gameVersions = listOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"),
        yacl = "3.1.1+1.19.4-fabric",
        modmenu = "6.3.1",
    )
    "1.20.4" -> Mc(
        yarn = "1.20.4+build.3",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.97.3+1.20.4",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.20 <1.20.5",
        gameVersions = listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"),
        yacl = "3.6.6+1.20.4-fabric",
        modmenu = "9.2.0",
    )
    "1.20.6" -> Mc(
        yarn = "1.20.6+build.3",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.100.8+1.20.6",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.20.5 <1.21",
        gameVersions = listOf("1.20.5", "1.20.6"),
        gametest = true,
        yacl = "3.6.6+1.20.6-fabric",
        modmenu = "10.0.0",
    )
    "1.21.1" -> Mc(
        yarn = "1.21.1+build.3",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.116.14+1.21.1",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.21 <1.21.2",
        gameVersions = listOf("1.21", "1.21.1"),
        gametest = true,
        yacl = "3.8.2+1.21.1-fabric",
        modmenu = "11.0.4",
    )
    "1.21.8" -> Mc(
        yarn = "1.21.8+build.1",
        flk = "1.11.0+kotlin.2.0.0",
        fapi = "0.136.1+1.21.8",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.21.2 <1.21.9",
        gameVersions = listOf("1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8"),
        gametest = true,
        yacl = "3.8.2+1.21.6-fabric",
        modmenu = "15.0.2",
    )
    "1.21.10" -> Mc(
        yarn = "1.21.10+build.2",
        flk = "1.11.0+kotlin.2.0.0",
        fapi = "0.138.3+1.21.10",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.21.9 <1.22",
        gameVersions = listOf("1.21.9", "1.21.10", "1.21.11"),
        gametest = true,
        yacl = "3.8.2+1.21.10-fabric",
        modmenu = "16.0.1",
    )
    else -> error("Unconfigured Minecraft version: $mcVersion")
}

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String
base { archivesName.set(property("archives_base_name") as String) }

repositories {
    mavenCentral()
    // YACL (config screen) and ModMenu (the button) both via Modrinth's maven, which
    // serves every build uniformly and with dependency-free POMs.
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${mc.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${mc.fapi}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mc.flk}")
    modImplementation("me.lucko:fabric-permissions-api:${mc.permissions}") {
        // permissions-api pulls the fabric-api BOM (0.97.0+1.20.4), which bumps
        // fabric-api to a 1.20.4 build and breaks runClient on the older anchors.
        // Our own per-anchor fabric-api already provides the command API it needs.
        exclude(group = "net.fabricmc.fabric-api")
    }

    // Client config screen. Optional for users (suggests in fabric.mod.json); needed
    // here only to compile the screen. Skipped on 1.18.2, which has no YACL build.
    if (mc.gui) {
        modImplementation("maven.modrinth:yacl:${mc.yacl}")
        modImplementation("maven.modrinth:modmenu:${mc.modmenu}") {
            // Guard against ModMenu dragging in a mismatched fabric-api; keep ours.
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Headless in-game gametests (booted server) — see src/gametest. Enabled only on
// anchors whose fabric-api ships a compatible fabric-gametest-api-v1 (see the
// gametest flag in the matrix); other versions are covered by the unit tests.
if (mc.gametest) {
    fabricApi {
        configureTests {
            createSourceSet = true
            modId = "${property("archives_base_name")}-test"
            eula = true
        }
    }
    dependencies {
        "modGametestImplementation"("net.fabricmc.fabric-api:fabric-api:${mc.fapi}")
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "java_level" to mc.java,
        "minecraft_dep" to mc.depends,
        "gui" to mc.gui,
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(mc.java) }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(mc.java.toString()))
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(mc.java)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
}

// Publish this version node to Modrinth (run for all nodes via `./gradlew publishMods`).
publishMods {
    file.set(tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    changelog.set("See https://github.com/iSlavok/Simple-Frames/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        // Must be the exact, complete patch list — Modrinth shows only what is listed.
        minecraftVersions.addAll(mc.gameVersions)
        requires("fabric-api")
        requires("fabric-language-kotlin")
    }
}
