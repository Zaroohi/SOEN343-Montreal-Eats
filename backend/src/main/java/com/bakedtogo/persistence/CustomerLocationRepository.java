package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.CustomerLocation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.bakedtogo.business.reset.Resettable;

@Repository
public class CustomerLocationRepository implements Resettable {
    private final Map<String, CustomerLocation> store = new ConcurrentHashMap<>();

    public void save(CustomerLocation c) { store.put(c.id(), c); }
    public Optional<CustomerLocation> findById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }
    public List<CustomerLocation> findAll() { return List.copyOf(store.values()); }
    @Override 
    public void reset() { store.clear(); }
}