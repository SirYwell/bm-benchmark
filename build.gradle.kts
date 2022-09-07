plugins {
    id("java")
    id("me.champeau.jmh") version "0.6.7"
}

group = "de.sirywell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}

jmh {
    jvmArgsAppend.add("--add-modules=jdk.incubator.vector")
    // profilers.add("gc")
    profilers.add("perfasm")
}

tasks.jmhRunBytecodeGenerator {
    jvmArgs.add("--add-modules=jdk.incubator.vector")
}
