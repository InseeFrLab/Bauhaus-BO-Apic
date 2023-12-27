package fr.insee.rmes.config;

import fr.insee.rmes.controllers.PublicResources;
import fr.insee.rmes.utils.EndPointMethodReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class ControllersConfiguration {

    @Autowired
    public ControllersConfiguration(GenericApplicationContext genericApplicationContext){
        var replacerBeanName="publicResourcesInitReplacer";
        var beanDefinition=BeanDefinitionBuilder.genericBeanDefinition(PublicResources.class)
                .setLazyInit(true)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .getBeanDefinition();
        var methodOverrides=new MethodOverrides();
        methodOverrides.addOverride(new ReplaceOverride("getProperties", replacerBeanName));
        /*methodOverrides.addOverride(new ReplaceOverride("getGeoFeature", replacerBeanName));
        methodOverrides.addOverride(new ReplaceOverride("createGeography", replacerBeanName));
        methodOverrides.addOverride(new ReplaceOverride("updateGeography", replacerBeanName));*/
        beanDefinition.setMethodOverrides(methodOverrides);
        genericApplicationContext.registerBean(replacerBeanName, EndPointMethodReplacer.class);
        genericApplicationContext.registerBeanDefinition("publicResources",beanDefinition);
        //genericApplicationContext.refresh();
    }



}
