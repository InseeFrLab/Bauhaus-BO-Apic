package fr.insee.rmes.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface UserResources {
    @GetMapping(value = "/stamp",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getStamp(/*@AuthenticationPrincipal*/ Object principal);
}
