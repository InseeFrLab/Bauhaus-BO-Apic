package fr.insee.rmes.integration.authorizations;

import fr.insee.apic.config.SecurityConfiguration;
import fr.insee.apic.utils.PassePlatUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
        properties = {"jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.apic.security.csrf.disable=true"}
)
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
public class TestGeographyResourcesAuthorizations {


    @Autowired
    private MockMvc mvc;

    @MockBean
    private PassePlatUtility passPlat;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";

    @BeforeEach
    void init() {
        when(this.passPlat.allRequest(any(), any(), any(), any())).thenReturn(ResponseEntity.noContent().build());
    }


    @Test
        // /geo/territories
        //Error message = No static resource geo/territories.
    void getTerritoriesAuthentified_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));

        mvc.perform(get("/geo/territories").header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void getTerritoriesUnauthentified_401() throws Exception {
        mvc.perform(get("/geo/territories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postTerritoriesUnauthentified_401() throws Exception {
        mvc.perform(post("/geo/territory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postTerritoriesNoAdmin_403() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));
        mvc.perform(post("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());

    }

    @Test
    void postTerritoriesAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));

        mvc.perform(post("/geo/territory").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void optionCreateTerritoriesNoAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));

        mvc.perform(options("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void optionCreateTerritoriesUnauthentified_ok() throws Exception {
        mvc.perform(options("/geo/territory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void optionCreateTerritoriesAsAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(options("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

}
