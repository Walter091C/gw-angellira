
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
    // O runtime SOAP desta variante é o JAX-WS disponibilizado pelo Java 8.
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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
    // Java 8 já fornece JAX-WS, JAXB, SAAJ e as anotações javax necessárias.
    // O CXF configurado em wsdl2java é usado SOMENTE na geração de fontes.
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
