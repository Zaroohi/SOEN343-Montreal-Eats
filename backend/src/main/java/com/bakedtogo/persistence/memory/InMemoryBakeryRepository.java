package com.bakedtogo.persistence.memory;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.persistence.BakeryRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBakeryRepository implements BakeryRepository {
    private final Map<String, Bakery> store = new ConcurrentHashMap<>();

    public Optional<Bakery> findById(String id) { return Optional.ofNullable(store.get(id)); }
    public List<Bakery> findAll() { return List.copyOf(store.values()); }
    public void save(Bakery bakery) { store.put(bakery.getId(), bakery); }
}