package com.bakedtogo.business.service;
import com.bakedtogo.business.domain.Customer;
import com.bakedtogo.business.domain.User;
import com.bakedtogo.persistence.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {

    @Test
    public void testLoginWithCorrectPassword() {
        UserRepository repository = new UserRepository();
        SessionService session = new SessionService();

        Customer customer = new Customer("c1", "Alex", "pass123");
        repository.save(customer);

        AuthenticationService auth =
                new AuthenticationService(repository, session);

        User result = auth.login("c1", "pass123");

        assertNotNull(result);
        assertTrue(session.isAuthenticated());
    }

    @Test
    public void testLoginWithWrongPassword() {
        UserRepository repository = new UserRepository();
        SessionService session = new SessionService();

        Customer customer = new Customer("c1", "Alex", "pass123");
        repository.save(customer);

        AuthenticationService auth =
                new AuthenticationService(repository, session);

        User result = auth.login("c1", "wrong123");

        assertNull(result);
        assertFalse(session.isAuthenticated());
    }

    @Test
    public void testLoginWithUnknownId() {
        UserRepository repository = new UserRepository();
        SessionService session = new SessionService();

        AuthenticationService auth =
                new AuthenticationService(repository, session);

        User result = auth.login("unknown", "pass123");

        assertNull(result);
        assertFalse(session.isAuthenticated());
    }

    @Test
    public void testLogout() {
        UserRepository repository = new UserRepository();
        SessionService session = new SessionService();

        Customer customer = new Customer("c1", "Alex", "pass123");
        repository.save(customer);

        AuthenticationService auth =
                new AuthenticationService(repository, session);

        auth.login("c1", "pass123");
        auth.logout();

        assertFalse(session.isAuthenticated());
    }
}
