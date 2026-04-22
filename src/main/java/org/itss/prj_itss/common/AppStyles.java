package org.itss.prj_itss.common;

import javafx.scene.control.Label;

public final class AppStyles {

    private AppStyles() { }

    // ── Color constants ──────────────────────────────────────────────────────
    public static final String BG_PAGE     = "#F5F9F6";
    public static final String BG_CARD     = "white";
    public static final String BORDER_CARD = "#E0EBE4";
    public static final String PRIMARY     = "#253D2C";
    public static final String ACCENT      = "#2E6F40";
    public static final String TEXT_DARK   = "#1a2e22";
    public static final String TEXT_MID    = "#3A4A40";
    public static final String TEXT_MUTED  = "#6B7C72";
    public static final String TEXT_LIGHT  = "#8FA899";

    // ── Card style ───────────────────────────────────────────────────────────
    public static String cardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12;" +
               "-fx-border-radius: 12; -fx-border-color: #E0EBE4; -fx-border-width: 1;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";
    }

    // ── Button styles ────────────────────────────────────────────────────────
    public static String btnPrimary() {
        return "-fx-background-color: #2E6F40; -fx-text-fill: white;" +
               "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 9 20;";
    }

    public static String btnSecondary() {
        return "-fx-background-color: white; -fx-text-fill: #2E6F40;" +
               "-fx-background-radius: 7; -fx-border-color: #2E6F40; -fx-border-radius: 7;" +
               "-fx-border-width: 1.5; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 9 20;";
    }

    public static String btnDark() {
        return "-fx-background-color: #253D2C; -fx-text-fill: white;" +
               "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 7 16;";
    }

    public static String btnDarkHover() {
        return "-fx-background-color: #1a2e20; -fx-text-fill: white;" +
               "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;" +
               "-fx-font-weight: bold; -fx-padding: 7 16;";
    }

    public static String btnChip(String bg, String fg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
               "-fx-background-radius: 14; -fx-cursor: hand; -fx-font-size: 12px;" +
               "-fx-font-weight: bold; -fx-padding: 5 14;";
    }

    public static String btnOutline() {
        return "-fx-background-color: white;" +
               "-fx-border-color: #D0DAD5; -fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-text-fill: #3A4A40; -fx-cursor: hand; -fx-font-size: 12px;" +
               "-fx-font-weight: bold; -fx-padding: 5 10;";
    }

    public static String btnOutlineHover() {
        return "-fx-background-color: #F0F4F2; -fx-border-color: #D0DAD5;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-text-fill: #1a2e22; -fx-cursor: hand; -fx-font-size: 12px;" +
               "-fx-font-weight: bold; -fx-padding: 5 10;";
    }

    // ── Table header label ───────────────────────────────────────────────────
    public static Label colHeader(String text, double width) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8FA899;");
        l.setMinWidth(width);
        l.setPrefWidth(width);
        return l;
    }

    // ── Page header style ────────────────────────────────────────────────────
    public static String pageHeaderStyle() {
        return "-fx-background-color: #EDFAF2;" +
               "-fx-padding: 28 32 24 32;" +
               "-fx-border-color: transparent transparent #D4EDE0 transparent;" +
               "-fx-border-width: 0 0 1 0;";
    }

    // ── Filter bar style ─────────────────────────────────────────────────────
    public static String filterBarStyle() {
        return "-fx-background-color: white;" +
               "-fx-border-color: transparent transparent #EEF3EF transparent;" +
               "-fx-border-width: 0 0 1 0;";
    }

    // ── Row styles for hover effect ──────────────────────────────────────────
    public static String rowNormal() {
        return "-fx-background-color: white;" +
               "-fx-border-color: transparent transparent #F0F4F2 transparent;" +
               "-fx-border-width: 0 0 1 0;";
    }

    public static String rowHover() {
        return "-fx-background-color: #F7FDF9;" +
               "-fx-border-color: transparent transparent #F0F4F2 transparent;" +
               "-fx-border-width: 0 0 1 0;";
    }

    // ── Search box style ─────────────────────────────────────────────────────
    public static String searchBoxStyle() {
        return "-fx-background-color: white;" +
               "-fx-border-color: #D0DAD5;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-padding: 0 14 0 14;";
    }

    public static String searchFieldStyle() {
        return "-fx-background-color: transparent; -fx-border-color: transparent;" +
               "-fx-padding: 0; -fx-font-size: 13px; -fx-prompt-text-fill: #A0B0A6;";
    }

    public static String comboBoxStyle() {
        return "-fx-background-color: white; -fx-border-color: #D0DAD5;" +
               "-fx-border-radius: 6; -fx-background-radius: 6;" +
               "-fx-font-size: 13px; -fx-padding: 6 10;";
    }
}
