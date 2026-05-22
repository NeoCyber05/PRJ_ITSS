package org.itss.prj_itss.model.order.infrastructure.persistence;

import org.itss.prj_itss.common.data.JdbcRepositorySupport;

import org.itss.prj_itss.model.order.domain.Order;
import org.itss.prj_itss.model.order.domain.OrderMerchandise;
import org.itss.prj_itss.model.order.application.port.OrderRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderRepository extends JdbcRepositorySupport implements OrderRepository {

    public JdbcOrderRepository(org.itss.prj_itss.common.config.IConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT id, request_id, site_id, created_at, status FROM \"order\" ORDER BY id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            System.err.println("OrderRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Order> findByStatus(String status) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT id, request_id, site_id, created_at, status FROM \"order\" WHERE status = ? ORDER BY id DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("OrderRepository.findByStatus: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Order findById(int id) {
        String sql = "SELECT id, request_id, site_id, created_at, status FROM \"order\" WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapOrder(rs);
            }
        } catch (SQLException e) {
            System.err.println("OrderRepository.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<OrderMerchandise> findItemsByOrderId(int orderId) {
        List<OrderMerchandise> list = new ArrayList<>();
        String sql = "SELECT order_id, merchandise_id, quantity, delivery_method " +
                     "FROM order_merchandise WHERE order_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderMerchandise om = new OrderMerchandise();
                    om.setOrderId(rs.getInt("order_id"));
                    om.setMerchandiseId(rs.getInt("merchandise_id"));
                    om.setQuantity(rs.getBigDecimal("quantity"));
                    om.setDeliveryMethod(rs.getString("delivery_method"));
                    list.add(om);
                }
            }
        } catch (SQLException e) {
            System.err.println("OrderRepository.findItemsByOrderId: " + e.getMessage());
        }
        return list;
    }

    @Override
    public int create(Order order) {
        String sql = "INSERT INTO \"order\" (request_id, site_id, status) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, order.getRequestId());
            ps.setInt(2, order.getSiteId());
            ps.setString(3, order.getStatus());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("OrderRepository.create: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean addItem(OrderMerchandise item) {
        String sql = "INSERT INTO order_merchandise (order_id, merchandise_id, quantity, delivery_method) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getMerchandiseId());
            ps.setBigDecimal(3, item.getQuantity());
            ps.setString(4, item.getDeliveryMethod());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("OrderRepository.addItem: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE \"order\" SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("OrderRepository.updateStatus: " + e.getMessage());
        }
        return false;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setRequestId(rs.getInt("request_id"));
        o.setSiteId(rs.getInt("site_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        o.setStatus(rs.getString("status"));
        return o;
    }
}

