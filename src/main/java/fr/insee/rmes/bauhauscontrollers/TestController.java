package fr.insee.rmes.bauhauscontrollers;

import fr.insee.rmes.backend.MyRepository;
import fr.insee.rmes.utils.PassePlatUtility;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController("fr.insee.rmes.bauhauscontrollers.TestControllers")
public record TestController(PassePlatUtility passePlat, MyRepository myRepository) {

    @GetMapping("/test/{pathToTest}")
    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
    public ResponseEntity<String> test(@PathVariable("pathToTest") String path, @RequestHeader HttpHeaders headers){
        return passePlat.allRequest(HttpMethod.GET, path, headers, Optional.empty());
    }

    @GetMapping("/test-data")
    public ResponseEntity<String> testData(){
        return ResponseEntity.ok(myRepository.findByValueString("").toString());
    }

}
