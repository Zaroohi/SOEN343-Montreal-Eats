package com.bakedtogo.infrastructure;

import com.bakedtogo.business.domain.Bakery;
import com.bakedtogo.business.domain.CourierProfile;
import com.bakedtogo.business.domain.CustomerLocation;
import com.bakedtogo.business.domain.MenuItem;
import com.bakedtogo.business.domain.enums.BakeryStatus;
import com.bakedtogo.business.domain.enums.CourierStatus;
import com.bakedtogo.business.domain.enums.ItemStatus;
import com.bakedtogo.infrastructure.config.CityConfig;
import com.bakedtogo.persistence.BakeryRepository;
import com.bakedtogo.persistence.CourierRepository;
import com.bakedtogo.persistence.CustomerLocationRepository;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CityLoader implements ApplicationRunner {

    private final BakeryRepository bakeryRepository;
    private final CourierRepository courierRepository;
    private final CustomerLocationRepository customerRepository;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public CityLoader(BakeryRepository bakeryRepository, CourierRepository courierRepository,
                      CustomerLocationRepository customerRepository) {
        this.bakeryRepository = bakeryRepository;
        this.courierRepository = courierRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadCity();
    }

    private void loadCity() {
        try (InputStream in = getClass().getResourceAsStream("/city.json")) {
            if (in == null) throw new IllegalStateException("city.json not found on classpath");
            CityConfig city = mapper.readValue(in, CityConfig.class);

            for (CityConfig.BakeryConfig b : city.bakeries()) {
                Bakery bakery = new Bakery(b.id(), b.name(), b.latitude(), b.longitude(),
                        BakeryStatus.valueOf(b.status()));
                for (CityConfig.MenuItemConfig m : b.menu()) {
                    bakery.addMenuItem(new MenuItem(m.id(), m.name(), m.price(),
                            ItemStatus.valueOf(m.status())));
                }
                bakeryRepository.save(bakery);
            }
            System.out.println("[CONFIG] Loaded " + city.bakeries().size() + " bakeries from city.json");

            for (CityConfig.CourierConfig c : city.couriers()) {
                courierRepository.save(new CourierProfile(c.id(), c.latitude(), c.longitude(),
                        CourierStatus.valueOf(c.status())));
            }
            System.out.println("[CONFIG] Loaded " + city.couriers().size() + " couriers from city.json");

            for (CityConfig.CustomerConfig c : city.customers()) {
                customerRepository.save(new CustomerLocation(c.id(), c.name(),
                        c.latitude(), c.longitude()));
            }
            System.out.println("[CONFIG] Loaded " + city.customers().size() + " customers from city.json");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load city.json", e);
        }
    }
}