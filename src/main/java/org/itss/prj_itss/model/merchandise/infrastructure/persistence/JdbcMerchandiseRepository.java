package org.itss.prj_itss.model.merchandise.infrastructure.persistence;

import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;

import org.itss.prj_itss.model.merchandise.domain.Merchandise;
import org.itss.prj_itss.model.merchandise.application.port.MerchandiseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcMerchandiseRepository extends JdbcRepositorySupport implements MerchandiseRepository {

    public JdbcMerchandiseRepository(org.itss.prj_itss.model.shared.database.ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Merchandise> findAll() {
        List<Merchandise> list = new ArrayList<>();
        String sql = "SELECT id, code, name, unit, is_active FROM merchandise ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMerchandise(rs));
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Merchandise> findActive() {
        List<Merchandise> list = new ArrayList<>();
        String sql = "SELECT id, code, name, unit, is_active FROM merchandise WHERE is_active = true ORDER BY code";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMerchandise(rs));
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findActive: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Merchandise findById(int id) {
        String sql = "SELECT id, code, name, unit, is_active FROM merchandise WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMerchandise(rs);
            }
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Map<Integer, Merchandise> findByIds(Collection<Integer> ids) {
        Map<Integer, Merchandise> merchandiseById = new LinkedHashMap<>();
        List<Integer> normalizedIds = normalizeIds(ids);
        if (normalizedIds.isEmpty()) {
            return merchandiseById;
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(normalizedIds.size(), "?"));
        String sql = "SELECT id, code, name, unit, is_active FROM merchandise WHERE id IN (" + placeholders + ")";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < normalizedIds.size(); i++) {
                ps.setInt(i + 1, normalizedIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Merchandise merchandise = mapMerchandise(rs);
                    merchandiseById.put(merchandise.getId(), merchandise);
                }
            }
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findByIds: " + e.getMessage());
        }
        return merchandiseById;
    }

    @Override
    public Merchandise findByCode(String code) {
        String sql = "SELECT id, code, name, unit, is_active FROM merchandise WHERE code = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMerchandise(rs);
            }
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findByCode: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM merchandise";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.countAll: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int create(Merchandise merchandise) {
        String sql = "INSERT INTO merchandise (code, name, unit, is_active) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, merchandise.getCode());
            ps.setString(2, merchandise.getName());
            ps.setString(3, merchandise.getUnit());
            ps.setBoolean(4, merchandise.isActive());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.create: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(Merchandise merchandise) {
        String sql = "UPDATE merchandise SET code = ?, name = ?, unit = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, merchandise.getCode());
            ps.setString(2, merchandise.getName());
            ps.setString(3, merchandise.getUnit());
            ps.setInt(4, merchandise.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.update: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean setActive(int merchandiseId, boolean active) {
        String sql = "UPDATE merchandise SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, merchandiseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.setActive: " + e.getMessage());
        }
        return false;
    }

    private Merchandise mapMerchandise(ResultSet rs) throws SQLException {
        Merchandise m = new Merchandise();
        m.setId(rs.getInt("id"));
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setUnit(rs.getString("unit"));
        m.setActive(rs.getBoolean("is_active"));
        return m;
    }

    private List<Integer> normalizeIds(Collection<Integer> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
            .filter(id -> id != null)
            .distinct()
            .toList();
    }
}
