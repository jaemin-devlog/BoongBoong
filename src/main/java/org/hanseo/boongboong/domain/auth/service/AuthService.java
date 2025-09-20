package org.hanseo.boongboong.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.auth.dto.LoginRequest;
import org.hanseo.boongboong.domain.auth.dto.LoginResponse;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletRequest httpReq) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpReq.getSession(true);
        httpReq.changeSessionId(); // 세션 고정 공격 방지
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
        // 대안: new HttpSessionSecurityContextRepository().saveContext(context, httpReq, null);

        return toLoginResponse(auth);
    }

    public LoginResponse getCurrentUser(Authentication authentication) {
        return toLoginResponse(authentication);
    }

    private LoginResponse toLoginResponse(Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        Optional<User> userOpt = userRepository.findByEmail(username);
        User user = userOpt.orElseGet(() ->
                userRepository.findByUsername(username).orElseThrow()
        );

        String role = isAdmin ? "ADMIN"
                : (user.getRole() != null ? user.getRole().name() : "USER");

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                role
        );
    }
}
