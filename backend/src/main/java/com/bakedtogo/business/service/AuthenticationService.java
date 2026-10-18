package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.User;
import com.bakedtogo.persistence.UserRepository;

public class AuthenticationService {

    private UserRepository userRepository;
    private SessionService sessionService;

    public AuthenticationService(UserRepository userRepository,
                                 SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public User login(String id, String password) {

        if (id == null || password == null) {
            return null;
        }

        User user = userRepository.findById(id);

        if (user == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        sessionService.startSession(user);

        return user;
    }

    public void logout() {
        sessionService.logout();
    }
}
