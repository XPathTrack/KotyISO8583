plugins {
    id("java")
    id("application")
}

group = "org.refactor"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("org.koin:koin-test:3.2.1")
}

application {
    mainClass.set("org.refactor.ui.MainUI")
}

tasks.withType<JavaCompile> {
    options.release.set(11)
}

tasks.test {
    useJUnitPlatform()
}
