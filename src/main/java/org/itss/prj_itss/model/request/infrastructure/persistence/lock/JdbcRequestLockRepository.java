package org.itss.prj_itss.model.request.infrastructure.persistence.lock;

import org.itss.prj_itss.model.request.application.lock.RequestLockException;
import org.itss.prj_itss.model.request.application.lock.RequestLockGateway;
import org.itss.prj_itss.model.request.domain.lock.LockOwner;
import org.itss.prj_itss.model.request.domain.lock.LockResult;
import org.itss.prj_itss.model.request.domain.lock.RequestLock;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.shared.database.JdbcRepositorySupport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public final class JdbcRequestLockRepository extends JdbcRepositorySupport implements RequestLockGateway {

    public JdbcRequestLockRepository(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public LockResult acquireOrRenew(int requestId, LockOwner owner, int ttlSeconds) throws RequestLockException {
        String upsert = """
            INSERT INTO request_edit_lock(request_id, owner_username, owner_role, owner_display, locked_at, expires_at)
            VALUES (?, ?, ?, ?, now(), now() + (?||' seconds')::interval)
            ON CONFLICT (request_id) DO UPDATE
               SET owner_username = EXCLUDED.owner_username,
                   owner_role     = EXCLUDED.owner_role,
                   owner_display  = EXCLUDED.owner_display,
                   locked_at      = now(),
                   expires_at     = EXCLUDED.expires_at
             WHERE request_edit_lock.owner_username = EXCLUDED.owner_username
                OR request_edit_lock.expires_at <= now()
            RETURNING owner_username, owner_role, owner_display, locked_at, expires_at
            """;

        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                RequestLock acquired = null;
                try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                    ps.setInt(1, requestId);
                    ps.setString(2, owner.username());
                    ps.setString(3, owner.role());
                    ps.setString(4, owner.display());
                    ps.setString(5, String.valueOf(ttlSeconds));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            acquired = new RequestLock(
                                requestId,
                                rs.getString("owner_username"),
                                rs.getString("owner_role"),
                                rs.getString("owner_display"),
                                rs.getTimestamp("locked_at").toLocalDateTime(),
                                rs.getTimestamp("expires_at").toLocalDateTime()
                            );
                        }
                    }
                }

                if (acquired != null) {
                    conn.commit();
                    return LockResult.acquired(acquired);
                }

                // Blocked — read current holder
                String select = """
                    SELECT owner_username, owner_role, owner_display, locked_at, expires_at
                    FROM request_edit_lock WHERE request_id = ?
                    """;
                RequestLock holder = null;
                try (PreparedStatement ps = conn.prepareStatement(select)) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            holder = new RequestLock(
                                requestId,
                                rs.getString("owner_username"),
                                rs.getString("owner_role"),
                                rs.getString("owner_display"),
                                rs.getTimestamp("locked_at").toLocalDateTime(),
                                rs.getTimestamp("expires_at").toLocalDateTime()
                            );
                        }
                    }
                }
                conn.commit();
                return LockResult.blocked(holder);

            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw new RequestLockException("acquireOrRenew failed for requestId=" + requestId, e);
            } finally {
                try { conn.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            throw new RequestLockException("Cannot get connection for acquireOrRenew", e);
        }
    }

    @Override
    public void release(int requestId, String ownerUsername) throws RequestLockException {
        String sql = "DELETE FROM request_edit_lock WHERE request_id = ? AND owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, ownerUsername);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RequestLockException("release failed for requestId=" + requestId, e);
        }
    }

    @Override
    public void releaseAllForOwner(String ownerUsername) throws RequestLockException {
        String sql = "DELETE FROM request_edit_lock WHERE owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerUsername);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RequestLockException("releaseAllForOwner failed for owner=" + ownerUsername, e);
        }
    }

    @Override
    public Map<Integer, RequestLock> findActiveForRequests(Collection<Integer> requestIds) throws RequestLockException {
        if (requestIds.isEmpty()) return Map.of();
        // Build  WHERE request_id = ANY(?) using Array
        String sql = """
            SELECT request_id, owner_username, owner_role, owner_display, locked_at, expires_at
            FROM request_edit_lock
            WHERE request_id = ANY(?) AND expires_at > now()
            """;
        Map<Integer, RequestLock> result = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Integer[] ids = requestIds.toArray(Integer[]::new);
            ps.setArray(1, conn.createArrayOf("integer", ids));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int reqId = rs.getInt("request_id");
                    result.put(reqId, new RequestLock(
                        reqId,
                        rs.getString("owner_username"),
                        rs.getString("owner_role"),
                        rs.getString("owner_display"),
                        rs.getTimestamp("locked_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RequestLockException("findActiveForRequests failed", e);
        }
        return result;
    }
}
