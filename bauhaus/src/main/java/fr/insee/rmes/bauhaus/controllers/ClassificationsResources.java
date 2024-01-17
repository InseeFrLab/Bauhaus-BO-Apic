package fr.insee.rmes.bauhaus.controllers;

import fr.insee.rmes.bauhaus.model.Classification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classifications")
public interface ClassificationsResources {

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.bauhaus.auth.Roles).ADMIN)")
    @PutMapping(value="/classification/{id}")
    ResponseEntity<Classification> updateClassification(
            @PathVariable("id") String id,
            @RequestBody Classification body);
}
