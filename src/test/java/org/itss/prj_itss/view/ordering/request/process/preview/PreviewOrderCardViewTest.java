package org.itss.prj_itss.view.ordering.request.process.preview;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewOrderCardViewTest {

    private static final Path VIEW_ROOT = Path.of(
        "src",
        "main",
        "java",
        "org",
        "itss",
        "prj_itss",
        "view",
        "ordering",
        "request",
        "process",
        "preview"
    );

    @Test
    void allocationPreviewDoesNotRenderReceivingDeadlineColumn() throws IOException {
        String cardFxml = Files.readString(
            VIEW_ROOT.resolve("preview-order-card-view.fxml"),
            StandardCharsets.UTF_8
        );
        String rowFxml = Files.readString(
            VIEW_ROOT.resolve("preview-table-row-view.fxml"),
            StandardCharsets.UTF_8
        );

        assertTrue(cardFxml.contains("DỰ KIẾN NHẬN"));
        assertTrue(rowFxml.contains("fx:id=\"estimatedCell\""));
        assertFalse(cardFxml.contains("HẠN NHẬN"));
        assertFalse(rowFxml.contains("fx:id=\"desiredCell\""));
    }
}
