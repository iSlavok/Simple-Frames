import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Build script for the unobfuscated era (Minecraft 26+). No Yarn mappings exist;
// the game ships with Mojang names, so this uses the non-remapping Loom variant.
plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17.17" // non-remapping variant
    kotlin("jvm") version "2.4.10"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

data class Unobf(
    val fapi: String,
    val flk: String,
    val runtimeJava: Int,       // Java the game requires at runtime (fabric.mod.json + mixin level)
    val depends: String,
    val gameVersions: List<String>,
    // Client config screen deps (YACL + ModMenu). Present for both 26.x anchors.
    val yacl: String,
    val modmenu: String,
)

// Our own bytecode targets 21; Java 21 classes run fine on the game's Java 25
// runtime. The mixin compatibility level must match the game's class version, so
// it uses runtimeJava.
val compileJava = 21

val mcVersion = stonecutter.current.version
val u = when (mcVersion) {
    "26.1.2" -> Unobf(
        fapi = "0.155.2+26.1.2",
        flk = "1.13.13+kotlin.2.4.10",
        runtimeJava = 25,
        depends = ">=26.1 <26.2",
        gameVersions = listOf("26.1", "26.1.1", "26.1.2"),
        yacl = "3.9.6+26.1-fabric",
        modmenu = "18.0.0",
    )
    "26.2" -> Unobf(
        fapi = "0.155.2+26.2",
        flk = "1.13.13+kotlin.2.4.10",
        runtimeJava = 25,
        depends = ">=26.2 <27",
        gameVersions = listOf("26.2"),
        yacl = "3.9.6+26.2-fabric",
        modmenu = "20.0.1",
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
    // No mappings() — unobfuscated.
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${u.fapi}")
    // fabric-language-kotlin is a runtime-only language adapter: our code never
    // references its classes, but the mod depends on it in fabric.mod.json, so the
    // dev client/server needs it on the runtime classpath (runtimeOnly keeps it off
    // the compile classpath). Players supply it themselves at install time.
    runtimeOnly("net.fabricmc:fabric-language-kotlin:${u.flk}")
    // fabric-permissions-api is not published for 26+; commands fall back to the
    // vanilla operator level on this version (see the >=1.22 branch in Permission).

    // Client config screen deps. Optional for users (suggests in fabric.mod.json);
    // needed here only to compile the screen. Non-remapping loom has no
    // modImplementation, and 26.x mods are already Mojmap-named, so add them as plain
    // implementation libraries (mirrors how fabric-api is wired on this node).
    implementation("maven.modrinth:yacl:${u.yacl}")
    implementation("maven.modrinth:modmenu:${u.modmenu}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "java_level" to u.runtimeJava,
        "minecraft_dep" to u.depends,
        "gui" to true,
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(compileJava) }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(compileJava.toString()))
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(compileJava)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
}

// Non-remapping build: publish the plain `jar` (there is no remapJar here).
publishMods {
    file.set(tasks.named<AbstractArchiveTask>("jar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    changelog.set("See https://github.com/iSlavok/Simple-Frames/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(u.gameVersions)
        requires("fabric-api")
        requires("fabric-language-kotlin")
    }
}
