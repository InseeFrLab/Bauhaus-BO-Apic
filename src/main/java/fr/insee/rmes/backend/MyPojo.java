package fr.insee.rmes.backend;

import org.springframework.data.annotation.Id;

public record MyPojo(@Id Long id, String valueString) {
}
