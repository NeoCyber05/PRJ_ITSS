package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Request;
import org.itss.prj_itss.entity.RequestMerchandise;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RequestRepository extends RepositorySupport implements IRequestRepository {

    public RequestRepository(org.itss.prj_itss.common.config.IConnectionProvider connectionProvider) {
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
            System.err.println("RequestRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Request findById(int id) {
        String sql = "SELECT id, created_at, status, note FROM request WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRequest(rs);
            }
        } catch (SQLException e) {
            System.err.println("RequestRepository.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<RequestMerchandise> findItemsByRequestId(int requestId) {
        List<RequestMerchandise> list = new ArrayList<>();
        String sql = "SELECT request_id, merchandise_id, quantity_ordered, desired_delivery_date " +
                     "FROM request_merchandise WHERE request_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RequestMerchandise rm = new RequestMerchandise();
                    rm.setRequestId(rs.getInt("request_id"));
                    rm.setMerchandiseId(rs.getInt("merchandise_id"));
                    rm.setQuantityOrdered(rs.getBigDecimal("quantity_ordered"));
                    Date d = rs.getDate("desired_delivery_date");
                    if (d != null) rm.setDesiredDeliveryDate(d.toLocalDate());
                    list.add(rm);
                }
            }
        } catch (SQLException e) {
            System.err.println("RequestRepository.findItemsByRequestId: " + e.getMessage());
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
            System.err.println("RequestRepository.countItemTypes: " + e.getMessage());
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
            System.err.println("RequestRepository.getEarliestDeliveryDate: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateStatus(int requestId, String newStatus) {
        String sql = "UPDATE request SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("RequestRepository.updateStatus: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void updateRequestItems(int requestId, List<RequestMerchandise> items, String note) throws Exception {
        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            
            String updateNoteSql = "UPDATE request SET note = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateNoteSql)) {
                ps.setString(1, note);
                ps.setInt(2, requestId);
                ps.executeUpdate();
            }

            String deleteSql = "DELETE FROM request_merchandise WHERE request_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, requestId);
                ps.executeUpdate();
            }
            
            String insertSql = "INSERT INTO request_merchandise (request_id, merchandise_id, quantity_ordered, desired_delivery_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (RequestMerchandise item : items) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, item.getMerchandiseId());
                    ps.setBigDecimal(3, item.getQuantityOrdered());
                    if (item.getDesiredDeliveryDate() != null) {
                        ps.setDate(4, Date.valueOf(item.getDesiredDeliveryDate()));
                    } else {
                        ps.setNull(4, Types.DATE);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw new Exception("Error updating request items: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    @Override
    public boolean deleteById(int requestId) {
        try {
            Connection conn = getConnection();
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                // Delete child rows first (FK constraint)
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM request_merchandise WHERE request_id = ?")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }

                // Delete the request itself
                int affected;
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM request WHERE id = ?")) {
                    ps.setInt(1, requestId);
                    affected = ps.executeUpdate();
                }

                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                System.err.println("RequestRepository.deleteById: " + e.getMessage());
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
                return false;
            } finally {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ex) { /* ignore */ }
            }
        } catch (SQLException e) {
            System.err.println("RequestRepository.deleteById (connection): " + e.getMessage());
            return false;
        }
    }

    private Request mapRequest(ResultSet rs) throws SQLException {
        Request r = new Request();
        r.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        r.setStatus(rs.getString("status"));
        r.setNote(rs.getString("note"));
        return r;
    }
}

