package org.itss.prj_itss.model.request.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;
import org.itss.prj_itss.model.request.domain.request.RequestMerchandise;
import org.itss.prj_itss.model.request.application.processing.ProcessingRequestPort;
import org.itss.prj_itss.model.request.domain.request.RequestStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcProcessingRequestRepository extends JdbcRepositorySupport implements ProcessingRequestPort {

    public JdbcProcessingRequestRepository(org.itss.prj_itss.model.shared.database.ConnectionProvider connectionProvider) {
        super(connectionProvider);
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
            System.err.println("JdbcProcessingRequestRepository.findItemsByRequestId: " + e.getMessage());
        }
        return list;
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
            System.err.println("JdbcProcessingRequestRepository.getEarliestDeliveryDate: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateStatus(int requestId, RequestStatus newStatus) {
        String sql = "UPDATE request SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus.storageValue());
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("JdbcProcessingRequestRepository.updateStatus: " + e.getMessage());
        }
        return false;
    }
}
