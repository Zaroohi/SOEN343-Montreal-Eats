package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.User;
import com.bakedtogo.business.domain.exception.UnauthenticatedException;
import com.bakedtogo.business.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public SessionInterceptor(AuthController authController) {
        this.sessionService = authController.getSessionService();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthenticatedException("Login required");
        }

        token = token.substring(7);

        User user = sessionService.getUserByToken(token);

        if (user == null) {
            throw new UnauthenticatedException("Invalid token");
        }

        request.setAttribute("userId", user.getId());
        request.setAttribute("role", user.getRole());
        request.setAttribute("user", user);

        return true;
    }
}
