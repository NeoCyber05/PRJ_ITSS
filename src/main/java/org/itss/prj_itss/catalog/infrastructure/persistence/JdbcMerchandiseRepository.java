package org.itss.prj_itss.catalog.infrastructure.persistence;

import org.itss.prj_itss.common.data.JdbcRepositorySupport;

import org.itss.prj_itss.catalog.domain.Merchandise;
import org.itss.prj_itss.catalog.application.port.MerchandiseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMerchandiseRepository extends JdbcRepositorySupport implements MerchandiseRepository {

    public JdbcMerchandiseRepository(org.itss.prj_itss.common.config.IConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Merchandise> findAll() {
        List<Merchandise> list = new ArrayList<>();
        String sql = "SELECT id, code, name, unit FROM merchandise ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMerchandise(rs));
        } catch (SQLException e) {
            System.err.println("MerchandiseRepository.findAll: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Merchandise findById(int id) {
        String sql = "SELECT id, code, name, unit FROM merchandise WHERE id = ?";
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
    public Merchandise findByCode(String code) {
        String sql = "SELECT id, code, name, unit FROM merchandise WHERE code = ?";
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

    private Merchandise mapMerchandise(ResultSet rs) throws SQLException {
        Merchandise m = new Merchandise();
        m.setId(rs.getInt("id"));
        m.setCode(rs.getString("code"));
        m.setName(rs.getString("name"));
        m.setUnit(rs.getString("unit"));
        return m;
    }
}

