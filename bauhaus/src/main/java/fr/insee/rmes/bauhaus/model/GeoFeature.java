package fr.insee.rmes.bauhaus.model;

import java.util.List;

public record GeoFeature(String id,
        String labelLg1,
        String labelLg2,
        List<GeoFeature>unions,
        List<GeoFeature> difference,
        String code,
        String uri,
        String descriptionLg1,
        String descriptionLg2,
        String typeTerritory) {
}
