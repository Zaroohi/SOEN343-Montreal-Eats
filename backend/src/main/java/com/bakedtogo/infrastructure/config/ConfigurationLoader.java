package com.bakedtogo.infrastructure.config;

import com.bakedtogo.business.domain.*;
import com.bakedtogo.persistence.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;



import java.io.IOException;
import java.io.InputStream;

public class ConfigurationLoader {

    public void loadUsers(UserRepository userRepository) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("accounts.json")) {

            if (input == null) {
                throw new IOException("accounts.json not found");
            }

            JsonNode root = mapper.readTree(input);

            for (JsonNode account : root.get("accounts")) {

                String id = account.get("id").asText();
                String name = account.get("name").asText();
                String password = account.get("password").asText();
                String role = account.get("role").asText();

                User user;

                switch (role) {
                    case "CUSTOMER":
                        user = new Customer(id, name, password);
                        break;

                    case "BAKERY_STAFF":
                        String bakeryId = account.get("bakeryId").asText();
                        user = new BakeryStaff(id, name, password, bakeryId);
                        break;

                    case "COURIER":
                        user = new Courier(id, name, password);
                        break;

                    case "OPERATOR":
                        user = new PlatformOperator(id, name, password);
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid role: " + role);
                }

                userRepository.save(user);
            }
        }
    }
}
