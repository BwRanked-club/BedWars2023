dependencies {
    compileOnly(projects.bedwarsApi)
    implementation(projects.versionsupportCommon)
//    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("libs/spigot-1.8.8.jar"))
}

tasks.compileJava {
    options.release.set(17)
}

repositories {
    // Important Repos
    mavenCentral()
    mavenLocal()
    maven("https://repo.codemc.io/repository/nms/") // Spigot
}

description = "versionsupport_v1_8_r3"