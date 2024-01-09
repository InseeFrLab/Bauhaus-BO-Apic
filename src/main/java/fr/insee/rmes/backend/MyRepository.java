package fr.insee.rmes.backend;

import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(value ="fr.insee.rmes.backend.MyRepository")
public interface MyRepository extends CrudRepository<MyPojo, Long> {

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    Optional<MyPojo> findByValueString(String valueString);

}
