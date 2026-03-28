package com.hospital.hms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architecture rules")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.hospital.hms");
    }

    @Test
    @DisplayName("Controllers should not access repositories directly")
    void controllersShouldNotAccessRepositories() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .because("Controllers should use services, not repositories directly")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Entities should not depend on DTOs")
    void entitiesShouldNotDependOnDtos() {
        noClasses()
                .that().resideInAPackage("..entity..")
                .should().dependOnClassesThat().resideInAPackage("..dto..")
                .because("Entities must not know about DTOs")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Repository interfaces should reside in repository packages")
    void repositoriesShouldBeInCorrectPackage() {
        classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..repository..")
                .because("Repositories should be in the repository package")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Service classes should reside in service packages")
    void servicesShouldBeInCorrectPackage() {
        classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().areNotInterfaces()
                .should().resideInAnyPackage("..service..", "..jwt..")
                .because("Service implementations should be in service packages")
                .check(importedClasses);
    }
}
