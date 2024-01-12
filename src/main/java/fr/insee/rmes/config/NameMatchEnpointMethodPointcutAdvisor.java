package fr.insee.rmes.config;

import fr.insee.rmes.utils.EndPointMethodReplacer;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class NameMatchEnpointMethodPointcutAdvisor extends NameMatchMethodPointcutAdvisor {

    @Autowired
    public NameMatchEnpointMethodPointcutAdvisor(EndPointMethodReplacer interceptorMethodReplacer){
        super(interceptorMethodReplacer);
    }

}
