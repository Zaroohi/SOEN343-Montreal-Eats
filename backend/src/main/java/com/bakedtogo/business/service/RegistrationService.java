package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.Customer;
import com.bakedtogo.persistence.UserRepository;

public class RegistrationService {

    private UserRepository userRepository;

    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Customer registerCustomer(String id, String name, String password) {

        if (id == null || id.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }

        if (userRepository.existsById(id)) {
            throw new IllegalArgumentException("User already exists");
        }

        Customer customer = new Customer(id, name, password);
        userRepository.save(customer);

        return customer;
    }
}
