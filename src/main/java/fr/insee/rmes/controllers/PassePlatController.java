package fr.insee.rmes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RestController
public record PassePlatController(RestClient restClient) {

    @Autowired
    public PassePlatController(@Value("${fr.insee.rmes.apic.bauhaus-bo.url}") String bauhausBackOfficeUrl){
        this(RestClient.builder().baseUrl(bauhausBackOfficeUrl).build());
    }

    @RequestMapping("/{*path}")
    public ResponseEntity<String> allRequest(HttpMethod method, @PathVariable String path, @RequestHeader HttpHeaders requestHeaders, @RequestBody Optional<String> body){
        var response=addBody(restClient.method(method).uri(path), body)
                .headers(headers->{
                    headers.clear();
                    headers.addAll(requestHeaders);
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                })
                .retrieve();
        var responseEntity=response.toEntity(String.class);
        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    private RestClient.RequestBodySpec addBody(RestClient.RequestBodySpec requestBodySpec, Optional<String> body) {
        return body.map(requestBodySpec::body).orElse(requestBodySpec);
    }

}
