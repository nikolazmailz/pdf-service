import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
}

group = "com.signservice"
version = "0.1.0"
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Liquibase + JDBC для миграций
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql") // JDBC-драйвер

    // R2DBC-драйвер PostgreSQL (ГЛАВНОЕ ИСПРАВЛЕНИЕ)
    implementation("org.postgresql:r2dbc-postgresql")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")



    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // BouncyCastle for CMS/CAdES parsing and GOST support
//    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
//    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    // cripto hash
    // провайдер (криптография)
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    // парсить/работать с CMS/PKCS#7 (подписи, сертификаты и т.п.)
    implementation("org.bouncycastle:bcpkix-jdk18on:1.83")

    // PDF processing
    implementation("org.apache.pdfbox:pdfbox:2.0.30")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += "-Xjsr305=strict"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.signservice.SignServiceApplication")
}

