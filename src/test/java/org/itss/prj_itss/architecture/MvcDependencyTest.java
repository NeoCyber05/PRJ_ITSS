package org.itss.prj_itss.architecture;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MvcDependencyTest {

    private static final String BASE_PACKAGE = "org.itss.prj_itss";
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources");
    private static final Pattern FX_CONTROLLER_PATTERN = Pattern.compile("fx:controller\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern FXML_RESOURCE_PATTERN = Pattern.compile("\"(/org/itss/prj_itss/[^\"]+\\.fxml)\"");

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
    void controllerDoesNotDependOnJavaFx() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".controller")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
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

    @Test
    void salesRequestViewDoesNotDependOnEditApplicationState() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".view.sales.request")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.startsWith(BASE_PACKAGE + ".model.request.application.sales.update")) {
                    violations.add(sourceFile.relativePath() + " (Sales Request View) imports edit Application DTO: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void salesRequestUpdateViewContractsDoNotExposeApplicationDtos() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().equals(BASE_PACKAGE + ".controller.sales.request.update")) {
                continue;
            }
            String fileName = sourceFile.path().getFileName().toString();
            if (!fileName.contains("View") && !fileName.contains("ViewPort")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.startsWith(BASE_PACKAGE + ".model.request.application.sales.update")) {
                    violations.add(sourceFile.relativePath() + " (Update View contract) exposes Application DTO: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void salesRequestApplicationUsesFormatterPortInsteadOfSharedFormatterUtility() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            if (!sourceFile.packageName().startsWith(BASE_PACKAGE + ".model.request.application.sales")
                && !sourceFile.packageName().startsWith(BASE_PACKAGE + ".model.request.application.listing")) {
                continue;
            }
            for (String importLine : sourceFile.imports()) {
                if (importLine.startsWith(BASE_PACKAGE + ".model.shared.formatting.OrderingFormatters")) {
                    violations.add(sourceFile.relativePath() + " imports formatter utility instead of RequestDisplayFormatter port: " + importLine);
                }
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void fxmlFilesDoNotLiveUnderMainResources() throws IOException {
        List<String> violations = fxmlFiles(RESOURCE_ROOT).stream()
            .map(path -> RESOURCE_ROOT.relativize(path).toString())
            .toList();

        assertTrue(
            violations.isEmpty(),
            () -> "FXML files must live beside their view controller under src/main/java:"
                + System.lineSeparator()
                + String.join(System.lineSeparator(), violations)
        );
    }

    @Test
    void fxmlControllerPathMirrorsControllerPackage() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path fxmlFile : fxmlFiles(SOURCE_ROOT)) {
            String relativePath = normalized(SOURCE_ROOT.relativize(fxmlFile));
            if (!relativePath.startsWith("org/itss/prj_itss/view/")) {
                violations.add(relativePath + " is outside the view package tree");
                continue;
            }

            var matcher = FX_CONTROLLER_PATTERN.matcher(Files.readString(fxmlFile));
            if (!matcher.find()) {
                continue;
            }

            String controllerClass = matcher.group(1);
            String controllerPackage = controllerClass.substring(0, controllerClass.lastIndexOf('.'));
            String expectedPath = controllerPackage.replace('.', '/');
            String actualPath = normalized(SOURCE_ROOT.relativize(fxmlFile.getParent()));

            if (!actualPath.equals(expectedPath)) {
                violations.add(relativePath + " declares " + controllerClass + " but lives under " + actualPath);
            }
        }
        assertNoViolations(violations);
    }

    @Test
    void fxmlResourceStringsPointToExistingViewFiles() throws IOException {
        List<String> violations = new ArrayList<>();
        for (SourceFile sourceFile : sourceFiles()) {
            var matcher = FXML_RESOURCE_PATTERN.matcher(Files.readString(sourceFile.path()));
            while (matcher.find()) {
                String resourcePath = matcher.group(1);
                if (!resourcePath.startsWith("/org/itss/prj_itss/view/")) {
                    violations.add(sourceFile.relativePath() + " uses non-view FXML resource path: " + resourcePath);
                    continue;
                }

                Path filePath = SOURCE_ROOT.resolve(resourcePath.substring(1).replace('/', File.separatorChar));
                if (!Files.exists(filePath)) {
                    violations.add(sourceFile.relativePath() + " points to missing FXML resource: " + resourcePath);
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

    private List<Path> fxmlFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(path -> path.toString().endsWith(".fxml"))
                .toList();
        }
    }

    private String normalized(Path path) {
        return path.toString().replace('\\', '/');
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
