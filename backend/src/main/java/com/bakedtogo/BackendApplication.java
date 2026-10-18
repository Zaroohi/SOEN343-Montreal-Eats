package com.bakedtogo;

import com.bakedtogo.infrastructure.config.ConfigurationLoader;
import com.bakedtogo.persistence.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public UserRepository userRepository() {
		return new UserRepository();
	}

	@Bean
	public CommandLineRunner loadAccounts(UserRepository userRepository) {
		return args -> {
			ConfigurationLoader loader = new ConfigurationLoader();
			loader.loadUsers(userRepository);
			System.out.println("REG accounts loaded successfully");
		};
	}
}

