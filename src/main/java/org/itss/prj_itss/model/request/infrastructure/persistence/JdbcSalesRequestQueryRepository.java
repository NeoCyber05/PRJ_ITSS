package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;
import org.itss.prj_itss.model.request.domain.request.Request;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.sales.SalesRequestQueryPort;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSalesRequestQueryRepository extends JdbcRepositorySupport implements SalesRequestQueryPort {

    public JdbcSalesRequestQueryRepository(org.itss.prj_itss.model.shared.database.ConnectionProvider connectionProvider) {
        super(connectionProvider);
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
            System.err.println("JdbcSalesRequestQueryRepository.findById: " + e.getMessage());
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
            System.err.println("JdbcSalesRequestQueryRepository.findItemsByRequestId: " + e.getMessage());
        }
        return list;
    }

    private Request mapRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Timestamp ts = rs.getTimestamp("created_at");
        java.time.LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
        RequestStatus status = RequestStatus.fromStorageValue(rs.getString("status"));
        String note = rs.getString("note");
        return Request.reconstituteFromDb(id, createdAt, status, note);
    }
}
