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

import static java.util.Optional.ofNullable;

@Service
@Slf4j
public record PassePlatUtility(RestClient restClient) {

    @Autowired
    public PassePlatUtility(@Value("${fr.insee.rmes.apic.bauhaus-bo.url}") String bauhausBackOfficeUrl) {
        this(RestClient.builder().baseUrl(bauhausBackOfficeUrl).build());
    }

    //@CrossOrigin(origins = "${fr.insee.rmes.apic.cors.allowed-origins}")
    public ResponseEntity<String> allRequest(@NonNull HttpMethod method, /*@NonNull*/ String path, @NonNull HttpHeaders requestHeaders, @NonNull Optional<String> body) {
        final ResponseEntityBuilder<String> responseEntityBuilder = new ResponseEntityBuilder<>();
        log.atDebug().log(() -> "Process " + method + " " + path + " [" + requestHeaders.keySet() + "] with body.length = " + body.orElse("").length());
        var normalizedPath = normalizedPath(path);
        try {
            var remoteResponse = addBody(restClient.method(method).uri(normalizedPath), body)
                    .headers(headers -> {
                        headers.clear();
                        headers.addAll(requestHeaders);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    })
                    /*.exchange((request, response) -> {
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
                    });*/
                    .retrieve()
                    .toEntity(String.class);
            responseEntityBuilder.setStatus(remoteResponse.getStatusCode());
            responseEntityBuilder.setHeaders(cloneRemoving(remoteResponse.getHeaders()));
            responseEntityBuilder.setBody(remoteResponse.getBody());

        } catch (Exception e) {
            processLogError(method, normalizedPath, e, responseEntityBuilder);
            responseEntityBuilder.setStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .setBody("Error for Api consultation")
                    .setHeaders(new HttpHeaders());
        }
        log.atDebug().log(()->"SEND RESPONSE : "+responseEntityBuilder);

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

        private Optional<HttpStatusCode> status;
        private Optional<T> body;
        private Optional<HttpHeaders> headers;

        public ResponseEntityBuilder() {
            this(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public ResponseEntityBuilder<T> setStatus(@NonNull HttpStatusCode status) {
            this.status = Optional.of(status);
            return this;
        }

        public ResponseEntityBuilder<T> setHeaders(@NonNull HttpHeaders headers) {
            this.headers = Optional.of(headers);
            return this;
        }

        public ResponseEntityBuilder<T> setBody(@Nullable T body) {
            this.body = ofNullable(body);
            return this;
        }

        public ResponseEntity<T> build() {
            var retour = ResponseEntity.status(this.status.get()).headers(headers.get());
            this.body.ifPresent(retour::body);
            return retour.build();
        }

    }
}
