package org.itss.prj_itss.auth.presentation.workspace;

import java.util.List;

public record RoleWorkspaceContent(
    String homeLabel,
    String sidebarSubtitle,
    String title,
    String summary,
    String accessNote,
    String statusLabel,
    List<String> capabilityHighlights
) {
}
