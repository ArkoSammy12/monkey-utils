import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("maven-publish")
	id("org.jetbrains.kotlin.jvm") version "2.1.20"
}

version = property("mod_version").toString()
group = property("maven_group").toString()

base {
	archivesName.set(property("archives_base_name").toString())
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.

    repositories {
        // Patbox's SGui
        maven("https://maven.nucleoid.xyz")

		maven {
			name = "enjaraiMavenReleases"
			url = uri("https://maven.enjarai.dev/releases")
		}

		maven {
			name = "modrinth"
			url = uri("https://api.modrinth.com/maven")
		}

    }



}

sourceSets {
	create("testmod") {
		compileClasspath += main.get().compileClasspath
		runtimeClasspath += main.get().runtimeClasspath
	}
}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("monkey-utils") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets["client"])
		}
	}

	runs {
		create("testmodClient") {
			client()
			name = "Testmod Client"
			source(sourceSets["testmod"])
		}

		create("testmodServer") {
			server()
			name = "Testmod Server"
			source(sourceSets["testmod"])
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

	include(implementation("io.github.arkosammy12:monkey-config:${property("monkey-config")}")!!)

	// Fabric Permissions API
	include(modImplementation("me.lucko:fabric-permissions-api:${property("fabric-permissions-api")}")!!)

	"testmodImplementation"(sourceSets.main.get().output)
	"testmodImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.2")
	"testmodRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to project.version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
	}
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Jar> {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}" }
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}