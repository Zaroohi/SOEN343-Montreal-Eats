package com.bakedtogo.infrastructure;

import com.bakedtogo.business.promo.PromoCode;
import com.bakedtogo.infrastructure.config.PromoConfig;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

public class PromoLoader {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public Map<String, PromoCode> load() {
        try (InputStream in = getClass().getResourceAsStream("/promos.json")) {
            if (in == null) {
                System.out.println("[PROMO] promos.json not found: no promo codes available");
                return Map.of();
            }
            PromoConfig config = mapper.readValue(in.readAllBytes(), PromoConfig.class);
            Map<String, PromoCode> rules = new LinkedHashMap<>();
            if (config != null && config.promos() != null) {
                for (PromoConfig.PromoEntry e : config.promos()) {
                    String code = e.code().trim().toUpperCase();
                    rules.put(code, new PromoCode(
                            code,
                            PromoCode.Type.valueOf(e.type().toUpperCase()),
                            e.value(), e.minOrderValue(),
                            e.expiresOn() == null ? null : LocalDate.parse(e.expiresOn())));
                }
            }
            System.out.println("[CONFIG] Loaded " + rules.size() + " promo codes from promos.json");
            return rules;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load promos.json", e);
        }
    }
}