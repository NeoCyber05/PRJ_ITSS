package org.itss.prj_itss.service;

import org.itss.prj_itss.auth.login.LoginResult;
import org.itss.prj_itss.auth.AuthenticatedUser;
import org.itss.prj_itss.entity.Account;
import org.itss.prj_itss.repository.IAccountRepository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

public class AuthenticationService {

    private final IAccountRepository accountRepository;

    public AuthenticationService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public LoginResult authenticate(String username, String password) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password;

        if (normalizedUsername.isBlank() || normalizedPassword.isBlank()) {
            return LoginResult.failure("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }

        Optional<AuthenticatedUser> user = accountRepository.findByCredentials(normalizedUsername, normalizedPassword);
        if (user.isEmpty()) {
            return LoginResult.failure("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        if (!isAccountActive(user.get().account())) {
            return LoginResult.failure("Tài khoản đã bị khóa hoặc chưa sẵn sàng sử dụng.");
        }

        return LoginResult.success(user.get());
    }

    private boolean isAccountActive(Account account) {
        String normalizedStatus = normalizeText(account.getStatus());
        if (normalizedStatus.isBlank()) {
            return true;
        }

        return !(normalizedStatus.contains("inactive")
            || normalizedStatus.contains("disabled")
            || normalizedStatus.contains("locked")
            || normalizedStatus.contains("suspended")
            || normalizedStatus.contains("deleted")
            || normalizedStatus.contains("vo hieu hoa")
            || normalizedStatus.contains("khoa")
            || normalizedStatus.contains("ngung"));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
