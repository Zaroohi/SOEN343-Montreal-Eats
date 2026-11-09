package com.bakedtogo.persistence;

import com.bakedtogo.business.domain.CourierProfile;
import java.util.List;
import java.util.Optional;

public interface CourierRepository {
    Optional<CourierProfile> findById(String id);
    List<CourierProfile> findAll();
    void save(CourierProfile courier);
}