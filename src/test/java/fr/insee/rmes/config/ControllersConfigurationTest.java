package fr.insee.rmes.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

class ControllersConfigurationTest {

    @Test
    void findEndpointMethodsTest() throws IOException {
        var expected = Set.of("endpointDelete", "endpointPut", "endpointPost", "endpoint3", "endpoint2", "endpoint1");

        ScannedGenericBeanDefinition testEndpointsMetadata =
                new ScannedGenericBeanDefinition((new SimpleMetadataReaderFactory()).getMetadataReader(TestEndPoints.class.getName()));
        Assertions.assertEquals(expected,
                ControllersConfiguration.findEndpointMethods(testEndpointsMetadata).map(MethodMetadata::getMethodName).collect(Collectors.toSet()));

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
        default void endpointDelete() {
        }
    }

}