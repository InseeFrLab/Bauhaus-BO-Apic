package fr.insee.rmes.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

class PassePlatControllerTest {

    public static final String BASE_URI = "https://bauhaus";

    @ParameterizedTest
    @ValueSource(strings = {
            "//users/stamp",
            "/users//stamp",
            "/users/stamp//",
            "users/stamp//"
    })
    @DisplayName("Test process of URI by Spring framework")
    void testNormalizedPathForUris(String badUri){
        UriComponentsBuilder baseUri=UriComponentsBuilder.fromUriString(BASE_URI);
        var passePlatController=new PassePlatController(BASE_URI);
        URI uriResult = springComputationOfUri(passePlatController.normalizedPath(badUri), baseUri);
        Assertions.assertEquals(java.net.URI.create(BASE_URI+(("/"+badUri).replace("//","/").replace("//","/"))), uriResult);
    }

    private static URI springComputationOfUri(String badUri, UriComponentsBuilder baseUri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(badUri);
        UriComponents uri = builder.build();
        UriComponentsBuilder uriComponentsBuilder = (uri.getHost() == null ? baseUri.cloneBuilder().uriComponents(uri) : builder);
        return java.net.URI.create(uriComponentsBuilder.build().expand(Collections.emptyMap()).toString());
    }

}