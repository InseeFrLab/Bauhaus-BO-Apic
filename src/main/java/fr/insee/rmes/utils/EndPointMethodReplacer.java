package fr.insee.rmes.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Optional;


@Slf4j
public record EndPointMethodReplacer(@Autowired PassePlatUtility passePlatUtility,
                                     @Autowired HttpServletRequest webRequest
                                     /*,@Autowired HttpServletResponse webResponse*/) implements MethodReplacer {
    @Override
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        log.info("REQUEST : {}",webRequest);
        Optional<String> body=webRequest.getReader().lines().reduce((a, b)->a+"\n"+b);
        HttpHeaders headers=headers(webRequest);
        return passePlatUtility.allRequest(HttpMethod.valueOf(webRequest.getMethod()),webRequest.getServletPath(), headers,body);
    }

    private HttpHeaders headers(HttpServletRequest webRequest) {
        var retour=new HttpHeaders();
        webRequest.getHeaderNames().asIterator().forEachRemaining(name-> retour.add(name, webRequest.getHeader(name)));
        return retour;
    }
}
