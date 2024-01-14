package fr.insee.rmes.bauhaus.controllers.geography;

import fr.insee.rmes.bauhaus.model.GeoFeature;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/geo")
public interface GeographyResources {
    @GetMapping(value = "/territories", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeoFeature> getGeoFeatures() ;

    @GetMapping(value = "/territory/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeoFeature> getGeoFeature(@PathVariable("id") String id);

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.bauhaus.auth.Roles).ADMIN)")
    @PostMapping(value = "/territory", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> createGeography(@RequestBody GeoFeature body);

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.bauhaus.auth.Roles).ADMIN)")
    @PutMapping(value = "/territory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> updateGeography(@PathVariable("id") String id, @RequestBody GeoFeature body);
}
