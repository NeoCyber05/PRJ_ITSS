package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.sales.SalesRequestCommandPort;

import java.sql.*;
import java.util.List;

public class JdbcSalesRequestCommandRepository extends JdbcRepositorySupport implements SalesRequestCommandPort {

    public JdbcSalesRequestCommandRepository(org.itss.prj_itss.model.shared.database.ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public int createRequest(Request request) throws Exception {
        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            
            int requestId = -1;
            String insertRequestSql = "INSERT INTO request (status, note, created_at) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, request.getStatus().storageValue());
                ps.setString(2, request.getNote());
                ps.setTimestamp(3, Timestamp.valueOf(request.getCreatedAt()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) requestId = rs.getInt(1);
                }
            }

            if (requestId == -1) throw new SQLException("Failed to create request, no ID obtained.");

            String insertItemSql = "INSERT INTO request_merchandise (request_id, merchandise_id, quantity_ordered, desired_delivery_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                for (RequestMerchandise item : request.getItems()) {
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
            return requestId;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
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
                System.err.println("JdbcSalesRequestCommandRepository.deleteById: " + e.getMessage());
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
                return false;
            } finally {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ex) { /* ignore */ }
            }
        } catch (SQLException e) {
            System.err.println("JdbcSalesRequestCommandRepository.deleteById (connection): " + e.getMessage());
            return false;
        }
    }
}
