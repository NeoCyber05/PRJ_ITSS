package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.request.application.international.detail.ReceivedRequestDetailQueryPort;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcReceivedRequestDetailQuery extends JdbcRepositorySupport implements ReceivedRequestDetailQueryPort {

    public JdbcReceivedRequestDetailQuery(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public RequestSummary findRequestSummary(int requestId) {
        String sql = "SELECT r.id, r.created_at, r.status, r.note, " +
                     "MIN(rm.desired_delivery_date) AS earliest_delivery_date " +
                     "FROM request r " +
                     "LEFT JOIN request_merchandise rm ON rm.request_id = r.id " +
                     "WHERE r.id = ? " +
                     "GROUP BY r.id, r.created_at, r.status, r.note";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    java.sql.Date deliveryDate = rs.getDate("earliest_delivery_date");
                    return new RequestSummary(
                        rs.getInt("id"),
                        ts != null ? ts.toLocalDateTime() : null,
                        rs.getString("status"),
                        rs.getString("note"),
                        deliveryDate != null ? deliveryDate.toLocalDate() : null
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcReceivedRequestDetailQuery.findRequestSummary: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<RequestItemProjection> findRequestItems(int requestId) {
        List<RequestItemProjection> list = new ArrayList<>();
        String sql = "SELECT m.code, m.name, rm.quantity_ordered, m.unit, rm.desired_delivery_date " +
                     "FROM request_merchandise rm " +
                     "JOIN merchandise m ON m.id = rm.merchandise_id " +
                     "WHERE rm.request_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date desiredDate = rs.getDate("desired_delivery_date");
                    list.add(new RequestItemProjection(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getBigDecimal("quantity_ordered"),
                        rs.getString("unit"),
                        desiredDate != null ? desiredDate.toLocalDate() : null
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcReceivedRequestDetailQuery.findRequestItems: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<AllocatedOrderProjection> findAllocatedOrders(int requestId) {
        List<AllocatedOrderProjection> list = new ArrayList<>();
        String sql = "SELECT o.id, s.name AS site_name, " +
                     "(SELECT om.delivery_method FROM order_merchandise om WHERE om.order_id = o.id LIMIT 1) AS delivery_method, " +
                     "o.created_at, o.status " +
                     "FROM \"order\" o " +
                     "LEFT JOIN site s ON s.id = o.site_id " +
                     "WHERE o.request_id = ? " +
                     "ORDER BY o.id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    list.add(new AllocatedOrderProjection(
                        rs.getInt("id"),
                        null, // orderCode is derived from id
                        rs.getString("site_name"),
                        rs.getString("delivery_method"),
                        ts != null ? ts.toLocalDateTime() : null,
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcReceivedRequestDetailQuery.findAllocatedOrders: " + e.getMessage());
        }
        return list;
    }

    @Override
    public AllocatedOrderProjection findAllocatedOrderById(int orderId) {
        String sql = "SELECT o.id, s.name AS site_name, " +
                     "(SELECT om.delivery_method FROM order_merchandise om WHERE om.order_id = o.id LIMIT 1) AS delivery_method, " +
                     "o.created_at, o.status " +
                     "FROM \"order\" o " +
                     "LEFT JOIN site s ON s.id = o.site_id " +
                     "WHERE o.id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new AllocatedOrderProjection(
                        rs.getInt("id"),
                        null, // orderCode is derived from id
                        rs.getString("site_name"),
                        rs.getString("delivery_method"),
                        ts != null ? ts.toLocalDateTime() : null,
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("JdbcReceivedRequestDetailQuery.findAllocatedOrderById: " + e.getMessage());
        }
        return null;
    }
}
