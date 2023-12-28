package fr.insee.rmes.bauhauscontrollers.geography;

import fr.insee.rmes.model.GeoFeature;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/geo")
public interface GeographyResources {
    @GetMapping(value = "/territories", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeoFeature> getGeoFeatures() ;

    @GetMapping(value = "/territory/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeoFeature> getGeoFeature(@PathVariable("id") String id);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(value = "/territory", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> createGeography(@RequestBody GeoFeature body);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping(value = "/territory/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> updateGeography(@PathVariable("id") String id, @RequestBody GeoFeature body);
}
