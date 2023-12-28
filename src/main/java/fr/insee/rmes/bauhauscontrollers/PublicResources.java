package fr.insee.rmes.bauhauscontrollers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface PublicResources {
    @GetMapping(value = "/init", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getProperties() ;

    @GetMapping(value = "/stamps", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getStamps();

    @GetMapping(value = "/disseminationStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getDisseminationStatus();

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getRoles();
}

