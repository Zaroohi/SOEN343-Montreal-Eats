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
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        load();
    }

    /** Loads city.json. Runs at startup and again on Reset. Missing or empty sections give an empty city. */
    public synchronized void load() {
        try (InputStream in = getClass().getResourceAsStream("/city.json")) {
            if (in == null) throw new IllegalStateException("city.json not found on classpath");
            byte[] raw = in.readAllBytes();
            CityConfig city = new String(raw, StandardCharsets.UTF_8).isBlank()
                    ? new CityConfig(null, null, null)
                    : mapper.readValue(raw, CityConfig.class);

            List<CityConfig.BakeryConfig> bakeryList = orEmpty(city.bakeries());
            List<CityConfig.CourierConfig> courierList = orEmpty(city.couriers());
            List<CityConfig.CustomerConfig> customerList = orEmpty(city.customers());

            for (CityConfig.BakeryConfig b : bakeryList) {
                Bakery bakery = new Bakery(b.id(), b.name(), b.latitude(), b.longitude(),
                        BakeryStatus.valueOf(b.status()));
                for (CityConfig.MenuItemConfig m : orEmpty(b.menu())) {
                    bakery.addMenuItem(new MenuItem(m.id(), m.name(), m.price(),
                            ItemStatus.valueOf(m.status())));
                }
                bakeryRepository.save(bakery);
            }
            for (CityConfig.CourierConfig c : courierList) {
                courierRepository.save(new CourierProfile(c.id(), c.latitude(), c.longitude(),
                        CourierStatus.valueOf(c.status())));
            }
            for (CityConfig.CustomerConfig c : customerList) {
                customerRepository.save(new CustomerLocation(c.id(), c.name(),
                        c.latitude(), c.longitude()));
            }

            if (bakeryList.isEmpty() && courierList.isEmpty() && customerList.isEmpty()) {
                System.out.println("[CONFIG] city.json is empty: starting with an empty city");
            } else {
                System.out.println("[CONFIG] Loaded " + bakeryList.size() + " bakeries, "
                        + courierList.size() + " couriers, " + customerList.size()
                        + " customers from city.json");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load city.json", e);
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}