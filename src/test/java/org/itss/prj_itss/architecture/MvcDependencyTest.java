package org.itss.prj_itss.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MvcDependencyTest {

    private static final String BASE_PACKAGE = "org.itss.prj_itss";
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @Test
    void modelDoesNotDependOnViewOrControllerOrJavaFx() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".model")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.startsWith(BASE_PACKAGE + ".view")) {
                    violations.add(sourceFile.relativePath() + " (Model) imports View: " + importLine);
                }
                if (importLine.startsWith(BASE_PACKAGE + ".controller")) {
                    violations.add(sourceFile.relativePath() + " (Model) imports Controller: " + importLine);
                }
                if (importLine.startsWith("javafx.")) {
                    violations.add(sourceFile.relativePath() + " (Model) imports JavaFX: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void controllerDoesNotDependOnViewOrJavaFx() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".controller")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.startsWith(BASE_PACKAGE + ".view")) {
                    violations.add(sourceFile.relativePath() + " (Controller) imports View: " + importLine);
                }
                if (importLine.startsWith("javafx.")) {
                    violations.add(sourceFile.relativePath() + " (Controller) imports JavaFX: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void viewDoesNotDependOnPersistence() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".view")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.contains(".persistence.") || importLine.contains("Jdbc")) {
                    violations.add(sourceFile.relativePath() + " (View) imports Persistence/JDBC: " + importLine);
                }
                if (importLine.startsWith(BASE_PACKAGE + ".model.shared.database")) {
                    violations.add(sourceFile.relativePath() + " (View) imports Database Connection: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
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
            () -> "MVC Architecture dependency violations:" + System.lineSeparator()
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

        private String relativePath() {
            return SOURCE_ROOT.relativize(path).toString();
        }
    }
}
