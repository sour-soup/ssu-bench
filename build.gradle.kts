plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.5.0"
}

group = "org.soup"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks {
    compileJava {
        inputs.files(processResources)
        dependsOn("openApiGenerate")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // SpringDoc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java.srcDirs(layout.buildDirectory.dir("generated/src/main/java"))
    }
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/swagger/ssu-bench.yaml")
    outputDir.set("${layout.buildDirectory.get().asFile}/generated")
    apiPackage.set("ssu.bench.endpoint")
    modelPackage.set("ssu.bench.model")
    configOptions.set(
        mapOf(
            "skipDefaultInterface" to "true",
            "dateLibrary" to "java8-localdatetime",
            "serializableModel" to "true",
            "interfaceOnly" to "true",
            "hideGenerationTimestamp" to "true",
            "useBeanValidation" to "true",
            "generateSupportingFiles" to "false",
            "swaggerDocketConfig" to "false",
            "useTags" to "true",
            "openApiNullable" to "false",
            "useNullForUnknownEnumValue" to "true",
            "enumPropertyNaming" to "legacy",
            "containerDefaultToNull" to "true",
            "useSpringBoot3" to "true",
            "additionalModelTypeAnnotations" to (
                "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true);" +
                    "@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)"
                )
        )
    )

    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "",
            "skipFormModel" to "false" // Need for correct model generation https://github.com/OpenAPITools/openapi-generator/issues/10396
        )
    )

    typeMappings.set(
        mapOf(
            "Double" to "java.math.BigDecimal",
            "Long" to "java.math.BigInteger"
        )
    )
}