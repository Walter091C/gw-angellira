
plugins {
    id("java-library")
    id("maven-publish")
    id("com.github.bjornvester.wsdl2java") version "2.0.2"
}

group = "br.com.gwsistemas"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

wsdl2java {
    cxfVersion.set("3.5.11")
    wsdlDir.set(layout.projectDirectory.dir("src/main/resources/wsdl"))
    includes.set(listOf("**/WSImport.wsdl"))
    useJakarta.set(false)
    packageName.set("br.com.gwsistemas.angellira.generated")
    options.set(listOf("-wsdlLocation", "wsdl/WSImport.wsdl"))
}

dependencies {
    api("jakarta.xml.ws:jakarta.xml.ws-api:2.3.3")
    api("jakarta.xml.bind:jakarta.xml.bind-api:2.3.3")
    api("jakarta.jws:jakarta.jws-api:2.1.0")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:3.5.11")
    implementation("org.apache.cxf:cxf-rt-transports-http:3.5.11")
    implementation("jakarta.annotation:jakarta.annotation-api:1.3.5")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.9")
    runtimeOnly("com.sun.xml.messaging.saaj:saaj-impl:1.5.3")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
