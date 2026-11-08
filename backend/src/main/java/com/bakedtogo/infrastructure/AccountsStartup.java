package com.bakedtogo.infrastructure;

import com.bakedtogo.infrastructure.config.ConfigurationLoader;
import com.bakedtogo.persistence.UserRepository;
import java.io.IOException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountsStartup implements ApplicationRunner {

    private final UserRepository userRepository;

    public AccountsStartup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        new ConfigurationLoader().loadUsers(userRepository);
        System.out.println("[CONFIG] Loaded accounts from accounts.json");
    }
}