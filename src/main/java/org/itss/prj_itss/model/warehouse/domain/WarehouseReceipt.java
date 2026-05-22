package org.itss.prj_itss.model.warehouse.domain;

import java.time.LocalDateTime;

public class WarehouseReceipt {

    private int id;
    private int sourceOrderId;
    private String sourceOrderCode;
    private String sourceRequestCode;
    private String siteCode;
    private String siteName;
    private String resultStatus;
    private boolean hasDiscrepancy;
    private String discrepancyNote;
    private String overallNote;
    private LocalDateTime confirmedAt;
    private Integer confirmedByAccountId;
    private String confirmedByUsername;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(int sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderCode() {
        return sourceOrderCode;
    }

    public void setSourceOrderCode(String sourceOrderCode) {
        this.sourceOrderCode = sourceOrderCode;
    }

    public String getSourceRequestCode() {
        return sourceRequestCode;
    }

    public void setSourceRequestCode(String sourceRequestCode) {
        this.sourceRequestCode = sourceRequestCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public boolean isHasDiscrepancy() {
        return hasDiscrepancy;
    }

    public void setHasDiscrepancy(boolean hasDiscrepancy) {
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public String getDiscrepancyNote() {
        return discrepancyNote;
    }

    public void setDiscrepancyNote(String discrepancyNote) {
        this.discrepancyNote = discrepancyNote;
    }

    public String getOverallNote() {
        return overallNote;
    }

    public void setOverallNote(String overallNote) {
        this.overallNote = overallNote;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Integer getConfirmedByAccountId() {
        return confirmedByAccountId;
    }

    public void setConfirmedByAccountId(Integer confirmedByAccountId) {
        this.confirmedByAccountId = confirmedByAccountId;
    }

    public String getConfirmedByUsername() {
        return confirmedByUsername;
    }

    public void setConfirmedByUsername(String confirmedByUsername) {
        this.confirmedByUsername = confirmedByUsername;
    }
}
