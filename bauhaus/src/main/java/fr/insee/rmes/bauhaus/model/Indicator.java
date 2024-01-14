package fr.insee.rmes.bauhaus.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public record Indicator(String id,
                 String prefLabelLg1,


                 String prefLabelLg2,


                 String altLabelLg1,


                 String altLabelLg2,


                 String abstractLg1,


                 String abstractLg2,


                 String historyNoteLg1,


                 String historyNoteLg2,


                 String accrualPeriodicityCode,


                 String accrualPeriodicityList,


                 //@JsonFormat(shape = Shape.ARRAY)

                 List<OperationsLink> publishers,


                 List<OperationsLink> contributors,


                 @JsonFormat(shape = JsonFormat.Shape.ARRAY)
                 List<String> creators,


                 List<OperationsLink> seeAlso,


                 List<OperationsLink> replaces,


                 List<OperationsLink> isReplacedBy,


                 List<OperationsLink> wasGeneratedBy,


                 String idSims,


                 String created,


                 @JsonAlias({"updated", "modified"})
                 String updated,

                 String validationState) {
}
