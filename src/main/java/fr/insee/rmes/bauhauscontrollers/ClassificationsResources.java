package fr.insee.rmes.bauhauscontrollers;

import fr.insee.rmes.model.Classification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classifications")
public interface ClassificationsResources {

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping(value="/classification/{id}")
    ResponseEntity<Classification> updateClassification(
            @PathVariable("id") String id,
            @RequestBody Classification body);
}
