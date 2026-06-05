package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.dashboard.application.port.DashboardRequestPort;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcDashboardRequestRepository extends JdbcRepositorySupport implements DashboardRequestPort {

    public JdbcDashboardRequestRepository(org.itss.prj_itss.model.shared.database.ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Request> findAll() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT id, created_at, status, note FROM request ORDER BY id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRequest(rs));
        } catch (SQLException e) {
            System.err.println("JdbcDashboardRequestRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int countItemTypes(int requestId) {
        String sql = "SELECT COUNT(*) FROM request_merchandise WHERE request_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("JdbcDashboardRequestRepository.countItemTypes: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public LocalDate getEarliestDeliveryDate(int requestId) {
        String sql = "SELECT MIN(desired_delivery_date) FROM request_merchandise WHERE request_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate(1);
                    if (d != null) return d.toLocalDate();
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcDashboardRequestRepository.getEarliestDeliveryDate: " + e.getMessage());
        }
        return null;
    }

    private Request mapRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Timestamp ts = rs.getTimestamp("created_at");
        java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
        RequestStatus status = RequestStatus.fromStorageValue(rs.getString("status"));
        String note = rs.getString("note");
        return Request.reconstituteFromDb(id, createdAt, status, note);
    }
    
    // Helper method to fix the compilation error in my script above where I forgot a dot
    @Override
    protected Connection getConnection() throws SQLException {
        return super.getConnection();
    }
}
