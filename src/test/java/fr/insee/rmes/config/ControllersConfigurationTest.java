package fr.insee.rmes.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

class ControllersConfigurationTest {

    @Test
    void findEndpointMethodsTest() {
        var expected= Set.of("endpointDelete", "endpointPut", "endpointPost", "endpoint3", "endpoint2", "endpoint1");
        Assertions.assertEquals(expected,
                ControllersConfiguration.findEndpointMethods(TestEndPoints.class).map(Method::getName).collect(Collectors.toSet()));

    }

    @RequestMapping("/toto")
    public interface TestEndPoints {

        private void noop() {
        }

        void noEndpoint();

        String noEndpoint2();

        default String noEndpoint3() {
            return "";
        }

        @GetMapping
        default String endpoint1() {
            return "";
        }

        @GetMapping
        String endpoint2();

        @RequestMapping
        String endpoint3();

        @PostMapping
        ResponseEntity<Void> endpointPost();

        @PutMapping
        ResponseEntity<?> endpointPut();

        @DeleteMapping
        default void endpointDelete(){}
    }

}