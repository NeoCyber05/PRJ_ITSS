package org.itss.prj_itss.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanArchitectureDependencyTest {

    private static final String BASE_PACKAGE = "org.itss.prj_itss";
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Set<String> LEGACY_TOP_LEVEL_PACKAGES = Set.of(
        "dto",
        "entity",
        "model",
        "repository",
        "service",
        "ordering",
        "sales"
    );

    @Test
    void domainPackagesDoNotDependOnOuterLayers() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().contains(".domain")) {
                continue;
            }
            sourceFile.imports().stream()
                .filter(this::isForbiddenDomainImport)
                .map(importLine -> sourceFile.relativePath() + " imports " + importLine)
                .forEach(violations::add);
        }

        assertNoViolations(violations);
    }

    @Test
    void applicationPackagesDoNotDependOnFrameworksOrAdapters() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().contains(".application")) {
                continue;
            }
            sourceFile.imports().stream()
                .filter(this::isForbiddenApplicationImport)
                .map(importLine -> sourceFile.relativePath() + " imports " + importLine)
                .forEach(violations::add);
        }

        assertNoViolations(violations);
    }

    @Test
    void presentationPackagesDoNotDependOnPersistenceAdapters() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().contains(".presentation")) {
                continue;
            }
            sourceFile.imports().stream()
                .filter(importLine -> importLine.contains(".infrastructure."))
                .map(importLine -> sourceFile.relativePath() + " imports " + importLine)
                .forEach(violations::add);
        }

        assertNoViolations(violations);
    }

    @Test
    void legacyLayerPackagesAreNotUsed() throws IOException {
        List<String> violations = sourceFiles().stream()
            .filter(sourceFile -> LEGACY_TOP_LEVEL_PACKAGES.contains(sourceFile.topLevelPackage()))
            .map(SourceFile::relativePath)
            .toList();

        assertNoViolations(violations);
    }

    @Test
    void requestPresentationDoesNotDependOnDomain() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".request.presentation")) {
                continue;
            }
            sourceFile.imports().stream()
                .filter(this::isForbiddenDomainImportForRequestPresentation)
                .map(importLine -> sourceFile.relativePath() + " imports " + importLine)
                .forEach(violations::add);
        }

        assertNoViolations(violations);
    }

    @Test
    void requestInfrastructureDoesNotDependOnApplicationServices() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".request.infrastructure")) {
                continue;
            }
            sourceFile.imports().stream()
                .filter(this::isForbiddenInfrastructureImport)
                .map(importLine -> sourceFile.relativePath() + " imports " + importLine)
                .forEach(violations::add);
        }

        assertNoViolations(violations);
    }

    private boolean isForbiddenDomainImportForRequestPresentation(String importLine) {
        return importLine.contains(".domain.");
    }

    private boolean isForbiddenInfrastructureImport(String importLine) {
        return importLine.startsWith(BASE_PACKAGE + ".request.application.")
            && !importLine.startsWith(BASE_PACKAGE + ".request.application.port.");
    }

    private boolean isForbiddenDomainImport(String importLine) {
        return importLine.startsWith("javafx.")
            || importLine.startsWith("java.sql.")
            || importLine.startsWith(BASE_PACKAGE + ".common.config")
            || importLine.startsWith(BASE_PACKAGE + ".db")
            || importLine.contains(".application")
            || importLine.contains(".infrastructure")
            || importLine.contains(".presentation");
    }

    private boolean isForbiddenApplicationImport(String importLine) {
        return importLine.startsWith("javafx.")
            || importLine.startsWith("java.sql.")
            || importLine.startsWith(BASE_PACKAGE + ".common.config")
            || importLine.startsWith(BASE_PACKAGE + ".db")
            || importLine.contains(".infrastructure.")
            || importLine.contains(".presentation.");
    }

    private List<SourceFile> sourceFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            return paths
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> !path.getFileName().toString().equals("module-info.java"))
                .map(SourceFile::from)
                .toList();
        }
    }

    private void assertNoViolations(List<String> violations) {
        assertTrue(
            violations.isEmpty(),
            () -> "Clean Architecture dependency violations:" + System.lineSeparator()
                + String.join(System.lineSeparator(), violations)
        );
    }

    private record SourceFile(Path path, String packageName, List<String> imports) {

        private static SourceFile from(Path path) {
            try {
                List<String> lines = Files.readAllLines(path);
                String packageName = lines.stream()
                    .filter(line -> line.startsWith("package "))
                    .findFirst()
                    .map(line -> line.substring("package ".length(), line.length() - 1))
                    .orElse("");
                List<String> imports = lines.stream()
                    .filter(line -> line.startsWith("import "))
                    .map(line -> line.substring("import ".length(), line.length() - 1))
                    .toList();
                return new SourceFile(path, packageName, imports);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot read " + path, exception);
            }
        }

        private String topLevelPackage() {
            if (!packageName.startsWith(BASE_PACKAGE + ".")) {
                return "";
            }
            String remainder = packageName.substring((BASE_PACKAGE + ".").length());
            int dotIndex = remainder.indexOf('.');
            return dotIndex < 0 ? remainder : remainder.substring(0, dotIndex);
        }

        private String relativePath() {
            return SOURCE_ROOT.relativize(path).toString();
        }
    }
}
