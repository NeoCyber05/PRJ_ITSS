package org.itss.prj_itss.model.request.domain.request;

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
    private RequestStatus status;
    private String note;
    private final java.util.List<RequestMerchandise> items = new java.util.ArrayList<>();

    /** Constructor mặc định để framework / JDBC mapper có thể khởi tạo. */
    public Request() {
    }

    /** Constructor nghiệp vụ để khởi tạo Yêu cầu mới. */
    public Request(String note) {
        this.note = note;
        this.status = RequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(int merchandiseId, java.math.BigDecimal quantity, java.time.LocalDate desiredDate) {
        this.items.add(new RequestMerchandise(this.id, merchandiseId, quantity, desiredDate));
    }

    public java.util.List<RequestMerchandise> getItems() {
        return java.util.Collections.unmodifiableList(items);
    }

    /**
     * Factory method tái tạo đối tượng từ dữ liệu DB — thay thế cho việc
     * gọi setter công khai. Dùng trong {@code JdbcRequestRepository.mapRequest()}.
     */
    public static Request reconstituteFromDb(int id, LocalDateTime createdAt, RequestStatus status, String note) {
        Request r = new Request();
        r.id = id;
        r.createdAt = createdAt;
        r.status = status;
        r.note = note;
        return r;
    }

    // ---------------------------------------------------------------
    // Business methods — đóng gói quy tắc chuyển trạng thái
    // ---------------------------------------------------------------

    /**
     * Chuyển yêu cầu sang trạng thái PROCESSING.
     *
     * @throws IllegalStateException nếu yêu cầu không đang ở trạng thái PENDING.
     */
    public void startProcessing() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException(
                "Chỉ có thể bắt đầu xử lý yêu cầu đang ở trạng thái PENDING. Trạng thái hiện tại: " + status
            );
        }
        this.status = RequestStatus.PROCESSING;
    }

    /**
     * Hoàn tất yêu cầu, chuyển sang trạng thái COMPLETED.
     *
     * @throws IllegalStateException nếu yêu cầu không đang ở trạng thái PROCESSING.
     */
    public void complete() {
        if (status != RequestStatus.PROCESSING) {
            throw new IllegalStateException(
                "Chỉ có thể hoàn thành yêu cầu đang ở trạng thái PROCESSING. Trạng thái hiện tại: " + status
            );
        }
        this.status = RequestStatus.COMPLETED;
    }

    // ---------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------

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

    /** Trả về {@link RequestStatus} enum, có thể {@code null} nếu DB có giá trị không khớp. */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Trả về chuỗi key lưu trong DB (vd: "pending", "processing", "completed").
     * Dùng để tương thích ngược với các formatter / view cũ đang nhận String.
     * Trả về {@code null} nếu status chưa được set.
     */
    public String getStatusKey() {
        return status == null ? null : status.storageValue();
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
                ", status=" + status +
                ", note='" + note + '\'' +
                '}';
    }
}
