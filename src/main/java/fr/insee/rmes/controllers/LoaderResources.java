package fr.insee.rmes.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface LoaderResources {
    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> uploadRdf(
            /*@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {LoaderResources.GESTION, LoaderResources.DIFFUSION}, type = "string"))*/
            @RequestPart(value = "database") String database,
            @RequestPart(value = "graph", required = false) String graph,
            @RequestPart(value = "file") MultipartFile file);

    @GetMapping(value = "/download/graph", produces = "*/*")
    ResponseEntity<Object> downloadDocument(
            @RequestBody String urlGraph,
            /*@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {LoaderResources.GESTION, LoaderResources.DIFFUSION}, type = "string"))*/
            @RequestBody String database);

    //@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @GetMapping(value = "/download/graphs", produces = "*/*")
    ResponseEntity<Object> downloadDocument(
            /*@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {LoaderResources.GESTION, LoaderResources.DIFFUSION}, type = "string"))*/
            @RequestBody String database);

    @GetMapping(value = "/graphs", produces = "*/*")
    ResponseEntity<String> getAllGraphs(
            /*@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {LoaderResources.GESTION, LoaderResources.DIFFUSION}, type = "string"))*/
            @RequestBody String database);

}
