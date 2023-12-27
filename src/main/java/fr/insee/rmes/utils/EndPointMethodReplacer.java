package fr.insee.rmes.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;


@Slf4j
public record EndPointMethodReplacer(@Autowired PassePlatUtility passePlatUtility,
                                     @Autowired HttpServletRequest webRequest
        /*@Autowired ContentCachingRequestWrapper contentCachingRequestWrapper*/) implements MethodReplacer {
    @Override
    public Object reimplement(Object obj, Method method, Object[] args) {
        log.debug("REQUEST : {} : {} {}", webRequest, webRequest.getMethod(), webRequest.getServletPath());
        try {
            Optional<String> body = readBody(webRequest, method, args);
            HttpHeaders headers = headers(webRequest);
            return passePlatUtility.allRequest(HttpMethod.valueOf(webRequest.getMethod()), webRequest.getServletPath(), headers, body);
        } catch (IOException e) {
            log.error("While preparing request {} {} for remote call", webRequest.getMethod(), webRequest.getServletPath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private Optional<String> readBody(HttpServletRequest webRequest, Method method, Object[] args) throws IOException {
        var hasCertainlyNoBody = hasCertainlyNoBody(webRequest);
        log.debug("web request has certainly not a body : {}", hasCertainlyNoBody);
        boolean hasCertainlyBody=hasCertainlyBody(webRequest);
        log.debug("web request has certainly a body : {}", hasCertainlyBody);
        if(hasCertainlyNoBody){
            return Optional.empty();
        }
        Optional<String> contentBody;
        Exception exceptionReading = null;
        try {
            contentBody = readContentBodyFromInputStream(webRequest);
            log.trace("Content body read from web request inputStream with encoding {} : {}", webRequest.getCharacterEncoding(), contentBody);
        } catch (IOException e) {
            contentBody = Optional.empty();
            exceptionReading = e;
        }
        try {
            if (contentBody.isEmpty()) {
                contentBody = readContentBodyFromArguments(method, args);
                log.trace("Content body read from web request argument : {}", contentBody);
            }
        } catch (Exception e) {
            contentBody = Optional.empty();
            if(exceptionReading!=null){
                log.error("First error while reading body of request {} {} for remote call", webRequest.getMethod(), webRequest.getServletPath(), exceptionReading);
            }
            exceptionReading = e;
        }
        if (hasCertainlyBody && contentBody.isEmpty()) {
            throw new IOException("The web request has a body (length " + webRequest.getContentLengthLong() + ") but it cannot be read", exceptionReading);
        }
        return contentBody;
    }

    private boolean hasCertainlyBody(HttpServletRequest webRequest) {
        return webRequest.getContentLengthLong()>0;
    }

    private Optional<String> readContentBodyFromArguments(Method method, Object[] args) throws JsonProcessingException {
        var indexOfArgumentWithBody = findIndexOfArgumentWithBody(method);
        if (0 < indexOfArgumentWithBody && indexOfArgumentWithBody < args.length) {
            return Optional.of(serialize(args[indexOfArgumentWithBody]));
        }
        return Optional.empty();
    }

    private String serialize(Object arg) throws JsonProcessingException {
        log.trace("Atempt to serialize object {}", arg);
        JsonMapper mapper = new JsonMapper();
        return mapper.writeValueAsString(arg);
    }

    private int findIndexOfArgumentWithBody(Method method) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                log.trace("Parameter {} is a request body : {}", i, parameters[i]);
                return i;
            }
        }
        log.debug("No parameter found as a request body for method {}", method);
        return -1;
    }

    private Optional<String> readContentBodyFromInputStream(HttpServletRequest webRequest) throws IOException {
        /*var cachedContent=contentCachingRequestWrapper.getContentAsString();*/
        // Utiliser FastByteArrayOutputStream ?
        return new BufferedReader(new InputStreamReader(webRequest.getInputStream(), webRequest.getCharacterEncoding())).lines()
                .reduce((a, b) -> a + "\n" + b);
    }

    private boolean hasCertainlyNoBody(HttpServletRequest webRequest) {
        return webRequest.getContentLengthLong() == 0;
    }

    private HttpHeaders headers(HttpServletRequest webRequest) {
        var retour = new HttpHeaders();
        webRequest.getHeaderNames().asIterator().forEachRemaining(name -> retour.add(name, webRequest.getHeader(name)));
        return retour;
    }
}
