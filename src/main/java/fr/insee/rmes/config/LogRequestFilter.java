package fr.insee.rmes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class LogRequestFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("REQUEST : {} - hashCode : {}\n{}\n{}",request,request.hashCode(),request.getMethod()+" "+request.getRequestURI(), request.getHeaderNames());
        filterChain.doFilter(request,response);
        log.info("RESPONSE : {} - hashCode : {} => {}\n{}\ncommited : {}",response,response.hashCode(),response.getStatus(),response.getHeaderNames(),response.isCommitted());
    }
}
