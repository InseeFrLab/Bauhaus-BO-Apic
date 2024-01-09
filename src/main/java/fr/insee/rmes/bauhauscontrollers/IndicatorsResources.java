package fr.insee.rmes.bauhauscontrollers;

import fr.insee.rmes.model.Indicator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/operations")
public interface IndicatorsResources {
    @GetMapping(value = "/indicators", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getIndicators();

    @GetMapping(value = "/indicators/withSims", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getIndicatorsWIthSims();

    @GetMapping(value = "/indicators/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getIndicatorsForSearch();

    @GetMapping(value = "/indicator/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<Object> getIndicatorByID(@PathVariable("id") String id, @RequestHeader(name="accept", required = false) String accept);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
    @PutMapping(value = "/indicator/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> setIndicatorById(@PathVariable("id") String id, @RequestBody Indicator body);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
    @PutMapping(value = "/indicator/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> setIndicatorValidation(@PathVariable("id") String id);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
    @PostMapping(value = "/indicator", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> setIndicator(@RequestBody Indicator body);
}
