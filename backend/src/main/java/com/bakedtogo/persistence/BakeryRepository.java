package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.Bakery;
import java.util.List;
import java.util.Optional;

public interface BakeryRepository {
    Optional<Bakery> findById(String id);
    List<Bakery> findAll();
    void save(Bakery bakery);
}