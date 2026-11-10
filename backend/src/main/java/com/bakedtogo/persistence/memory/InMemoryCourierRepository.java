package com.bakedtogo.persistence.memory;

import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.persistence.CourierRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCourierRepository implements CourierRepository {
    private final Map<String, CourierProfile> store = new ConcurrentHashMap<>();

    public Optional<CourierProfile> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public List<CourierProfile> findAll() { return List.copyOf(store.values()); }
    public void save(CourierProfile courier) { store.put(courier.getId(), courier); }
}