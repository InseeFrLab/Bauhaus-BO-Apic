package fr.insee.rmes.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.OptionalInt;

@RestController
@Slf4j
public record PassePlatController(RestClient restClient) {

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_LOWER_CASE = HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toLowerCase();

    @Autowired
    public PassePlatController(@Value("${fr.insee.rmes.apic.bauhaus-bo.url}") String bauhausBackOfficeUrl){
        this(RestClient.builder().baseUrl(bauhausBackOfficeUrl).build());
    }

    @RequestMapping("/{*path}")
    @CrossOrigin(origins = "${fr.insee.rmes.apic.cors.allowed-origins}")
    public ResponseEntity<String> allRequest(HttpMethod method, @PathVariable String path, @RequestHeader HttpHeaders requestHeaders, @RequestBody Optional<String> body){
        ResponseEntity<String> responseEntity;
        RestClient.ResponseSpec response=null;
        var normalizedPath=normalizedPath(path);
        try {
             response = addBody(restClient.method(method).uri(normalizedPath), body)
                    .headers(headers -> {
                        headers.clear();
                        headers.addAll(requestHeaders);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    })
                    .retrieve();
            responseEntity=response.toEntity(String.class);
        } catch (Exception e) {
            responseEntity=ResponseEntity.internalServerError().body("Error for Api consultation");
            processLogError(method, normalizedPath, e, response);
        }

        ResponseEntity<String> finalResponseEntity = responseEntity;
        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(headers->{
                    headers.clear();
                    headers.addAll(finalResponseEntity.getHeaders());
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                    headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                    headers.remove(ACCESS_CONTROL_ALLOW_ORIGIN_LOWER_CASE);
                })
                .body(responseEntity.getBody());
    }

    String normalizedPath(String path){
        return StringUtils.trimLeadingCharacter(path, '/');
    }

    private void processLogError(HttpMethod method, String path, Exception e, RestClient.ResponseSpec response) {
        log.error("Error while processing {} -> {}", method +" /"+ path, status(response), e);
    }

    private OptionalInt status(RestClient.ResponseSpec response) {
        return switch (response){
            case null -> OptionalInt.empty();
            default -> OptionalInt.of(response.toBodilessEntity().getStatusCode().value());
        };
    }

    private RestClient.RequestBodySpec addBody(RestClient.RequestBodySpec requestBodySpec, Optional<String> body) {
        return body.map(requestBodySpec::body).orElse(requestBodySpec);
    }

}
