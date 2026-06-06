package org.itss.prj_itss.controller.ordering.request.process.state;

import java.util.List;

public record ProcessingPreviewOrder(
    String siteName,
    String siteCode,
    List<ProcessingPreviewLine> lines
) {

    public record ProcessingPreviewLine(
        String merchandiseCode,
        String merchandiseName,
        int quantity,
        String transport,
        String desiredDate,
        String estimatedDate
    ) {
    }
}

