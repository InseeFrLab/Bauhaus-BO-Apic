package fr.insee.rmes.bauhauscontrollers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public interface UserResources {
    @GetMapping(value = "/stamp",
            produces = MediaType.APPLICATION_JSON_VALUE)
    //TODO active AuthenticationPrincipal
    ResponseEntity<Object> getStamp(/*@AuthenticationPrincipal*/ Object principal);
}
