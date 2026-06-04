package org.itss.prj_itss.view.ordering.request.process.state;

import java.util.List;

public record ProcessingPreviewOrderView(
    String siteName,
    String siteCode,
    List<ProcessingPreviewLineView> lines
) {

    public record ProcessingPreviewLineView(
        String merchandiseCode,
        String merchandiseName,
        int quantity,
        String transport,
        String desiredDate,
        String estimatedDate
    ) {
    }
}
