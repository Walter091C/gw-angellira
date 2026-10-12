
plugins {
    id("java-library")
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
}

wsdl2java {
    wsdlDir.set(layout.projectDirectory.dir("src/main/resources/wsdl"))
    includes.set(listOf("**/WSImport.wsdl"))
    useJakarta.set(false)
    packageName.set("br.com.gwsistemas.angellira.generated")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}