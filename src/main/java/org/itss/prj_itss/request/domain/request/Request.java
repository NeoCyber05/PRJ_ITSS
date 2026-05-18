package org.itss.prj_itss.request.domain.request;

import java.time.LocalDateTime;

/**
 * Entity class tương ứng với bảng "request" trong database.
 *
 * Bảng: public.request
 * - id (PK, auto-increment)
 * - created_at (timestamp, DEFAULT CURRENT_TIMESTAMP)
 * - status (varchar(50))
 */
public class Request {

    private int id;
    private LocalDateTime createdAt;
    private String status;
    private String note;

    public Request() {
    }

    public Request(int id, LocalDateTime createdAt, String status, String note) {
        this.id = id;
        this.createdAt = createdAt;
        this.status = status;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
