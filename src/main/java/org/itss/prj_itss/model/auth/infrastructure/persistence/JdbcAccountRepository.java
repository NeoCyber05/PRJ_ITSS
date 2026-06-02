package org.itss.prj_itss.model.auth.infrastructure.persistence;

import org.itss.prj_itss.model.auth.application.management.AccountDraft;
import org.itss.prj_itss.model.auth.application.port.AccountManagementRepository;
import org.itss.prj_itss.model.auth.application.port.AccountRepository;
import org.itss.prj_itss.model.auth.domain.Account;
import org.itss.prj_itss.model.auth.domain.AuthenticatedUser;
import org.itss.prj_itss.model.auth.domain.Role;
import org.itss.prj_itss.model.auth.domain.RoleType;
import org.itss.prj_itss.model.shared.database.ConnectionProvider;
import org.itss.prj_itss.model.site.application.SiteAccountDraft;
import org.itss.prj_itss.model.site.application.port.SiteAccountProvisioningPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository, AccountManagementRepository, SiteAccountProvisioningPort {

    private static final String AUTHENTICATE_SQL = """
        SELECT a.id,
               a.username,
               a.password,
               a.full_name,
               a.status,
               a.role_id,
               a.site_id,
               r.name AS role_name
        FROM public.account a
        INNER JOIN public.role r ON r.id = a.role_id
        WHERE LOWER(a.username) = LOWER(?)
          AND a.password = ?
        LIMIT 1
        """;

    private static final String FIND_ALL_USERS_SQL = """
        SELECT a.id, a.username, a.password, a.full_name, a.status, a.role_id, a.site_id, r.name AS role_name
        FROM public.account a
        INNER JOIN public.role r ON r.id = a.role_id
        ORDER BY a.id DESC
        """;

    private static final String FIND_ROLES_SQL = """
        SELECT id, name FROM public.role WHERE id != 4 ORDER BY id
        """;

    private static final String FIND_USER_BY_ID_SQL = """
        SELECT a.id, a.username, a.password, a.full_name, a.status, a.role_id, a.site_id, r.name AS role_name
        FROM public.account a INNER JOIN public.role r ON r.id = a.role_id WHERE a.id = ?
        """;

    private static final String FIND_USER_BY_USERNAME_SQL = """
        SELECT a.id, a.username, a.password, a.full_name, a.status, a.role_id, a.site_id, r.name AS role_name
        FROM public.account a INNER JOIN public.role r ON r.id = a.role_id WHERE LOWER(a.username) = LOWER(?)
        """;

    private static final String CREATE_ACCOUNT_SQL = """
        INSERT INTO public.account (username, password, full_name, status, role_id, site_id)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

    private static final String UPDATE_ACCOUNT_SQL = """
        UPDATE public.account
        SET username = ?, password = ?, full_name = ?, role_id = ?, site_id = ?
        WHERE id = ?
        """;

    private static final String UPDATE_STATUS_SQL = """
        UPDATE public.account SET status = ? WHERE id = ?
        """;

    private static final String USERNAME_EXISTS_SQL = """
        SELECT EXISTS(SELECT 1 FROM public.account WHERE LOWER(username) = LOWER(?))
        """;

    private static final String CREATE_SITE_ACCOUNT_SQL = """
        INSERT INTO public.account (username, password, full_name, status, role_id, site_id)
        VALUES (?, ?, ?, 'active', ?, ?)
        RETURNING id
        """;

    private final ConnectionProvider connectionProvider;

    public JdbcAccountRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Optional<AuthenticatedUser> findByCredentials(String username, String password) {
        try (PreparedStatement statement = getConnection().prepareStatement(AUTHENTICATE_SQL)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to authenticate account", exception);
        }
    }

    @Override
    public List<AuthenticatedUser> findAllUsers() {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_ALL_USERS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            List<AuthenticatedUser> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load all users", exception);
        }
    }

    @Override
    public List<Role> findRoles() {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_ROLES_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            List<Role> roles = new ArrayList<>();
            while (resultSet.next()) {
                roles.add(new Role(resultSet.getInt("id"), resultSet.getString("name")));
            }
            return roles;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load roles", exception);
        }
    }

    @Override
    public Optional<AuthenticatedUser> findUserById(int accountId) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_USER_BY_ID_SQL)) {
            statement.setInt(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to find user by id", exception);
        }
    }

    @Override
    public Optional<AuthenticatedUser> findUserByUsername(String username) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_USER_BY_USERNAME_SQL)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to find user by username", exception);
        }
    }

    @Override
    public int createAccount(AccountDraft draft) {
        try (PreparedStatement statement = getConnection().prepareStatement(CREATE_ACCOUNT_SQL)) {
            statement.setString(1, draft.username());
            statement.setString(2, draft.password());
            statement.setString(3, draft.fullName());
            statement.setString(4, "active");
            statement.setInt(5, draft.roleId());
            if (draft.siteId() == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setInt(6, draft.siteId());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                throw new IllegalStateException("CREATE_ACCOUNT_SQL returned no id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create account", exception);
        }
    }

    @Override
    public void updateAccount(int accountId, AccountDraft draft) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_ACCOUNT_SQL)) {
            statement.setString(1, draft.username());
            statement.setString(2, draft.password());
            statement.setString(3, draft.fullName());
            statement.setInt(4, draft.roleId());
            if (draft.siteId() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, draft.siteId());
            }
            statement.setInt(6, accountId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update account", exception);
        }
    }

    @Override
    public void updateStatus(int accountId, String status) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_STATUS_SQL)) {
            statement.setString(1, status);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update account status", exception);
        }
    }

    @Override
    public boolean usernameExists(String username) {
        try (PreparedStatement ps = getConnection().prepareStatement(USERNAME_EXISTS_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int createSiteAccount(SiteAccountDraft draft, int siteId) {
        try (PreparedStatement ps = getConnection().prepareStatement(CREATE_SITE_ACCOUNT_SQL)) {
            ps.setString(1, draft.username());
            ps.setString(2, draft.password());
            ps.setString(3, draft.fullName());
            ps.setInt(4, RoleType.SITE.id());
            ps.setInt(5, siteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create site account", e);
        }
        return 0;
    }

    private AuthenticatedUser mapUser(ResultSet rs) throws SQLException {
        Account account = new Account(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("status"),
            rs.getInt("role_id"),
            getNullableInteger(rs, "site_id")
        );
        Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"));
        return new AuthenticatedUser(account, role);
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    private Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }
}
