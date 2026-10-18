package com.bakedtogo.business.service;
import com.bakedtogo.business.domain.Customer;
import com.bakedtogo.persistence.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationServiceTest {

    @Test
    public void testRegisterCustomer() {
        UserRepository repository = new UserRepository();
        RegistrationService registration =
                new RegistrationService(repository);

        Customer customer =
                registration.registerCustomer("c1", "Alex", "pass123");

        assertNotNull(customer);
        assertEquals("c1", customer.getId());
        assertNotNull(repository.findById("c1"));
    }

    @Test
    public void testRegisterDuplicateId() {
        UserRepository repository = new UserRepository();
        RegistrationService registration =
                new RegistrationService(repository);

        registration.registerCustomer("c1", "Alex", "pass123");

        assertThrows(IllegalArgumentException.class, () -> {
            registration.registerCustomer("c1", "Sam", "pass456");
        });
    }

    @Test
    public void testRegisterEmptyId() {
        UserRepository repository = new UserRepository();
        RegistrationService registration =
                new RegistrationService(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            registration.registerCustomer("", "Alex", "pass123");
        });
    }

    @Test
    public void testRegisterEmptyPassword() {
        UserRepository repository = new UserRepository();
        RegistrationService registration =
                new RegistrationService(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            registration.registerCustomer("c1", "Alex", "");
        });
    }

    @Test
    public void testRegisterEmptyName() {
        UserRepository repository = new UserRepository();
        RegistrationService registration =
                new RegistrationService(repository);

        assertThrows(IllegalArgumentException.class, () -> {
            registration.registerCustomer("c1", "", "pass123");
        });
    }
}
