package org.itss.prj_itss.repository;

import org.itss.prj_itss.entity.Site;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteRepository extends RepositorySupport implements ISiteRepository, IInventoryRepository {

    public SiteRepository(org.itss.prj_itss.common.config.IConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Site> findAll() {
        List<Site> list = new ArrayList<>();
        String sql = "SELECT id, site_code, name, description, ship_delivery_days, air_delivery_days FROM site ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapSite(rs));
        } catch (SQLException e) {
            System.err.println("SiteRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Site> findAvailableForMerchandiseIds(List<Integer> merchandiseIds) {
        List<Site> list = new ArrayList<>();
        if (merchandiseIds == null || merchandiseIds.isEmpty()) {
            return list;
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(merchandiseIds.size(), "?"));
        String sql = """
            SELECT DISTINCT s.id, s.site_code, s.name, s.description, s.ship_delivery_days, s.air_delivery_days
            FROM site s
            INNER JOIN site_inventory si ON si.site_id = s.id
            WHERE si.merchandise_id IN (%s)
              AND si.stock_quantity > 0
            ORDER BY s.id
            """.formatted(placeholders);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < merchandiseIds.size(); i++) {
                ps.setInt(i + 1, merchandiseIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSite(rs));
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.findAvailableForMerchandiseIds: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Site findById(int id) {
        String sql = "SELECT id, site_code, name, description, ship_delivery_days, air_delivery_days FROM site WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSite(rs);
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Site findBySiteCode(String siteCode) {
        String sql = "SELECT id, site_code, name, description, ship_delivery_days, air_delivery_days FROM site WHERE site_code = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, siteCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSite(rs);
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.findBySiteCode: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM site";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("SiteRepository.countAll: " + e.getMessage());
        }
        return 0;
    }

    // IInventoryRepository

    @Override
    public Map<Integer, Integer> getInventoryBySiteId(int siteId) {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT merchandise_id, stock_quantity FROM site_inventory WHERE site_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, siteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("merchandise_id"), rs.getInt("stock_quantity"));
                }
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.getInventoryBySiteId: " + e.getMessage());
        }
        return map;
    }

    @Override
    public int getStockQuantity(int siteId, int merchandiseId) {
        String sql = "SELECT stock_quantity FROM site_inventory WHERE site_id = ? AND merchandise_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, siteId);
            ps.setInt(2, merchandiseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("stock_quantity");
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.getStockQuantity: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getTotalStock(int merchandiseId) {
        String sql = "SELECT COALESCE(SUM(stock_quantity), 0) FROM site_inventory WHERE merchandise_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, merchandiseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.getTotalStock: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countMerchandiseAtSite(int siteId) {
        String sql = "SELECT COUNT(*) FROM site_inventory WHERE site_id = ? AND stock_quantity > 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, siteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SiteRepository.countMerchandiseAtSite: " + e.getMessage());
        }
        return 0;
    }

    private Site mapSite(ResultSet rs) throws SQLException {
        Site s = new Site();
        s.setId(rs.getInt("id"));
        s.setSiteCode(rs.getString("site_code"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setShipDeliveryDays(rs.getObject("ship_delivery_days") != null ? rs.getInt("ship_delivery_days") : null);
        s.setAirDeliveryDays(rs.getObject("air_delivery_days") != null ? rs.getInt("air_delivery_days") : null);
        return s;
    }
}

