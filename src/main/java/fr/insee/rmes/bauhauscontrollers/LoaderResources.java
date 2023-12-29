package fr.insee.rmes.bauhauscontrollers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/loader")
public interface LoaderResources {
    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> uploadRdf(
            @RequestPart(value = "database") String database,
            @RequestPart(value = "graph", required = false) String graph,
            @RequestPart(value = "file") MultipartFile file);

    @GetMapping(value = "/download/graph", produces = "*/*")
    ResponseEntity<Object> downloadDocument(@RequestBody String urlGraph, @RequestBody String database);

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @GetMapping(value = "/download/graphs", produces = "*/*")
    ResponseEntity<Object> downloadDocument(@RequestBody LoaderResources database);

    @GetMapping(value = "/graphs", produces = "*/*")
    ResponseEntity<String> getAllGraphs(@RequestBody String database);

}
