package org.itss.prj_itss.repository;

import org.itss.prj_itss.auth.session.UserSession;
import org.itss.prj_itss.common.config.ConnectionProvider;
import org.itss.prj_itss.entity.Account;
import org.itss.prj_itss.entity.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcAccountRepository extends JdbcRepositorySupport implements AccountRepository {

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

    public JdbcAccountRepository(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<UserSession> findByCredentials(String username, String password) {
        try (PreparedStatement statement = getConnection().prepareStatement(AUTHENTICATE_SQL)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Account account = new Account(
                    resultSet.getInt("id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("full_name"),
                    resultSet.getString("status"),
                    resultSet.getInt("role_id"),
                    getNullableInteger(resultSet, "site_id")
                );
                Role role = new Role(resultSet.getInt("role_id"), resultSet.getString("role_name"));
                return Optional.of(new UserSession(account, role));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to authenticate account", exception);
        }
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }
}
