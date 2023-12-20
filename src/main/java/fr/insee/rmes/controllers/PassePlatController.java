package fr.insee.rmes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestController
public record PassePlatController(RestClient restClient) {

    @Autowired
    public PassePlatController(@Value("${fr.insee.rmes.apic.bauhaus-bo.url}") String bauhausBackOfficeUrl){
        this(RestClient.builder().baseUrl(bauhausBackOfficeUrl).build());
    }

    @RequestMapping("/**")
    public ResponseEntity<String> allRequest(HttpMethod method, ServletWebRequest request, HttpEntity<?> httpEntityRequest){
        var path=request.getRequest().getRequestURI();
        var response=addBody(restClient.method(method).uri(path), httpEntityRequest)
                .headers(headers->{
                    headers.clear();
                    HttpHeaders requestHeaders = httpEntityRequest.getHeaders();
                    headers.addAll(requestHeaders);
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                })
                .retrieve();
        var responseEntity=response.toEntity(String.class);
        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    private RestClient.RequestBodySpec addBody(RestClient.RequestBodySpec requestBodySpec, HttpEntity<?> httpEntity) {
        return httpEntity.hasBody()?requestBodySpec.body(httpEntity.getBody()):requestBodySpec;
    }

}
