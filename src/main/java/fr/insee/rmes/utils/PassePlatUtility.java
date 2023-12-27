package fr.insee.rmes.utils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public record PassePlatUtility(RestClient restClient) {

    @Autowired
    public PassePlatUtility(@Value("${fr.insee.rmes.apic.bauhaus-bo.url}") String bauhausBackOfficeUrl) {
        this(RestClient.builder().baseUrl(bauhausBackOfficeUrl).build());
    }

    //@CrossOrigin(origins = "${fr.insee.rmes.apic.cors.allowed-origins}")
    public ResponseEntity<String> allRequest(@NonNull HttpMethod method, @NonNull String path, @NonNull HttpHeaders requestHeaders, @NonNull Optional<String> body) {
        final ResponseEntityBuilder<String> responseEntityBuilder = new ResponseEntityBuilder<>();
        log.atDebug().log(() -> "Process " + method + " " + path + " [" + requestHeaders.keySet() + "] with body.length = " + body.orElse("").length());
        var normalizedPath = normalizedPath(path);
        try {
            addBody(restClient.method(method).uri(normalizedPath), body)
                    .headers(headers -> {
                        headers.clear();
                        headers.addAll(requestHeaders);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    })
                    .exchange((request, response) -> {
                        try {
                            responseEntityBuilder.setStatus(response.getStatusCode())
                                    .setHeaders(cloneRemoving(response.getHeaders(),
                                            HttpHeaders.CONTENT_LENGTH,
                                            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                                    .setBody(response.bodyTo(String.class));
                        } catch (Exception e) {
                            log.error("Error while retrieving {} {} :", request.getMethod(), request.getURI(), e);
                            return "error";
                        }
                        return "complete";
                    });

        } catch (Exception e) {
            processLogError(method, normalizedPath, e, responseEntityBuilder);
            responseEntityBuilder.setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                    .setBody("Error for Api consultation")
                    .setHeaders(new HttpHeaders());
        }
        log.debug("SEND RESPONSE : {}", responseEntityBuilder);
        return responseEntityBuilder.build();

    }

    private HttpHeaders cloneRemoving(HttpHeaders headers, String... headerNamesToRemove) {
        HttpHeaders clone = new HttpHeaders();
        Set<String> keysToRemove = Set.of(headerNamesToRemove);
        headers.entrySet().stream()
                .filter(entry -> !keysToRemove.contains(entry.getKey()))
                .forEach((entry -> clone.addAll(entry.getKey(), entry.getValue())));
        return clone;
    }

    String normalizedPath(String path) {
        return StringUtils.trimLeadingCharacter(path, '/');
    }

    private void processLogError(HttpMethod method, String path, Exception e, ResponseEntityBuilder<String> response) {
        log.error("Error while processing {} -> {}", method + " /" + path, response.status, e);
    }

    private RestClient.RequestBodySpec addBody(RestClient.RequestBodySpec requestBodySpec, Optional<String> body) {
        return body.map(requestBodySpec::body).orElse(requestBodySpec);
    }

    @AllArgsConstructor
    @ToString
    private class ResponseEntityBuilder<T> {

        private HttpStatusCode status;
        private T body;
        private HttpHeaders headers;

        public ResponseEntityBuilder() {
            this(null, null, null);
        }

        public ResponseEntityBuilder<T> setStatus(@NonNull HttpStatusCode status) {
            this.status = status;
            return this;
        }

        public ResponseEntityBuilder<T> setHeaders(@NonNull HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public ResponseEntityBuilder<T> setBody(@Nullable T body) {
            this.body = body;
            return this;
        }

        public ResponseEntity<T> build() {
            var statusReturned=requireNonNull(this.status);
            T bodyReturned=statusReturned==HttpStatus.NO_CONTENT?
                    this.body:
                    requireNonNull(this.body);
            return ResponseEntity.status(statusReturned)
                    .headers(requireNonNull(this.headers))
                    .body(bodyReturned);
        }

    }
}
