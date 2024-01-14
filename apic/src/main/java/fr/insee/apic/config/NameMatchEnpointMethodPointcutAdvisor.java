package fr.insee.apic.config;

import fr.insee.apic.utils.EndPointMethodReplacer;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;

public class NameMatchEnpointMethodPointcutAdvisor extends NameMatchMethodPointcutAdvisor {

    @Autowired
    public NameMatchEnpointMethodPointcutAdvisor(EndPointMethodReplacer interceptorMethodReplacer){
        super(interceptorMethodReplacer);
    }

}
