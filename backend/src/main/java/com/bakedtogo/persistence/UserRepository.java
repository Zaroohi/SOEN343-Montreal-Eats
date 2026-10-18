package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private Map<String, User> users = new HashMap<>();

    public void save(User user) {
        users.put(user.getId(), user);
    }

    public User findById(String id) {
        return users.get(id);
    }

    public boolean existsById(String id) {
        return users.containsKey(id);
    }
}
