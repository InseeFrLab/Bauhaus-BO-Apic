package fr.insee.apic.config;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfiguration {

    public static final String[] PUBLIC_RESOURCES_ANT_PATTERNS = {"/init", "/stamps", "/disseminationStatus", "/roles"};

    private static final Stream<String> EMPTY_ROLES = Stream.empty();

    public static final String DEFAULT_ROLE_PREFIX = "";

    private final String roleClaim;

    private final String keyForRolesInRoleClaim;

    private final String idClaim;
    private final boolean disableCsrf;


    public SecurityConfiguration(@Value("${jwt.role-claim}") String roleClaim,
                                 @Value("${jwt.role-claim.roles}") String keyForRolesInRoleClaim,
                                 @Value("${jwt.id-claim}") String idClaim,
                                 @Value("${fr.insee.apic.security.csrf.disable:false}")boolean disableCsrf) {
        this.roleClaim = roleClaim;
        this.keyForRolesInRoleClaim = keyForRolesInRoleClaim;
        this.idClaim = idClaim;
        this.disableCsrf = disableCsrf;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2ResourceServer->oauth2ResourceServer.jwt(withDefaults()))
                .cors(withDefaults())
                .authorizeHttpRequests(
                        authorizeHttpRequest -> authorizeHttpRequest
                                .requestMatchers(PUBLIC_RESOURCES_ANT_PATTERNS).permitAll()
                                .requestMatchers("/documents/document/*/file").permitAll()
                                .requestMatchers("/operations/operation/codebook").permitAll()
                                .requestMatchers("/healthcheck").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .anyRequest().authenticated()
                );
        if (this.disableCsrf){
            http.csrf(AbstractHttpConfigurer::disable);
        }
        log.info("OpenID authentication activated ");
        return http.build();
    }


    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(DEFAULT_ROLE_PREFIX);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(this::extractAuthoritiesFromJwt);
        jwtAuthenticationConverter.setPrincipalClaimName(idClaim);
        return jwtAuthenticationConverter;
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        return extractRoles(jwt.getClaims()).map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast).toList();
    }

    private Stream<String> extractRoles(Map<String, Object> claims) {
        var objectForRoles = (JsonObject) claims.get(roleClaim);
        return switch (objectForRoles){
            case null -> EMPTY_ROLES;
            default -> {
                var jsonArray=(JsonArray) objectForRoles.get(keyForRolesInRoleClaim);
                yield StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.size(),0),false)
                        .map(JsonElement::getAsString);
            }
        };

    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${fr.insee.apic.cors.allowed-origins}") Optional<String> allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        log.info("Allowed origins : {}", allowedOrigin);
        configuration.setAllowedOrigins(List.of(allowedOrigin.orElse("*")));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new
                UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    static public MethodSecurityExpressionHandler createExpressionHandler() {
        log.trace("Initializing GlobalMethodSecurityConfiguration with DefaultRolePrefix = {}", DEFAULT_ROLE_PREFIX);
        var expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
        return expressionHandler;
    }

}

