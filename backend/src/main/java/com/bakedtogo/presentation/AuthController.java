package com.bakedtogo.presentation;

import com.bakedtogo.business.domain.User;
import com.bakedtogo.business.domain.exception.UnauthenticatedException;
import com.bakedtogo.business.service.AuthenticationService;
import com.bakedtogo.business.service.RegistrationService;
import com.bakedtogo.business.service.SessionService;
import com.bakedtogo.persistence.UserRepository;
import com.bakedtogo.presentation.dto.LoginRequest;
import com.bakedtogo.presentation.dto.LoginResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final SessionService sessionService;

    public AuthController(UserRepository repository) {
        this.sessionService = new SessionService();
        this.authenticationService =
                new AuthenticationService(repository, sessionService);
        this.registrationService = new RegistrationService(repository);
    }

    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = authenticationService.login(
                request.id(), request.password());

        if (user == null) {
            throw new UnauthenticatedException("Invalid credentials");
        }

        String token = sessionService.createToken(user);
        return new LoginResponse(token);
    }

    @PostMapping("/api/register")
    public void register(@RequestBody RegistrationRequest request) {
        registrationService.registerCustomer(
                request.id(), request.name(), request.password());
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public record RegistrationRequest(String id, String name, String password) {
    }
}
