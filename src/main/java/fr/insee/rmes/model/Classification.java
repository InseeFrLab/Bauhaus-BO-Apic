package fr.insee.rmes.model;

public record Classification(
        String id,


        String prefLabelLg1,


        String prefLabelLg2,


        String altLabelLg1,


        String altLabelLg2,


        String descriptionLg1,


        String descriptionLg2,


        String changeNoteLg1,


        String changeNoteLg2,


        String scopeNoteLg1,


        String scopeNoteLg2,


        String scopeNoteUriLg1,


        String scopeNoteUriLg2,


        String changeNoteUriLg1,


        String changeNoteUriLg2,


        String idSeries,


        String idBefore,


        String idAfter,


        String idVariant,


        String disseminationStatus,


        String additionalMaterial,


        String legalMaterial,


        String homepage,


        String creator,


        String contributor,


        String validationState
) {
}
