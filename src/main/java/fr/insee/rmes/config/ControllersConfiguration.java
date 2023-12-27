package fr.insee.rmes.config;

import fr.insee.rmes.controllers.IndicatorsResources;
import fr.insee.rmes.controllers.PublicResources;
import fr.insee.rmes.utils.EndPointMethodReplacer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class ControllersConfiguration {

    private static final Set<Class<? extends Annotation>> endpointsAnnotations = Set.of(RequestMapping.class,
            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class);

    @Autowired
    public ControllersConfiguration(GenericApplicationContext genericApplicationContext) {
        var replacerBeanName = "endPointMethodReplacer";
        genericApplicationContext.registerBean(replacerBeanName, EndPointMethodReplacer.class);

        controllerInterfacesForEndpointImplementation().forEach(
                clazz -> lookupEnpointsImplementation(clazz, genericApplicationContext, replacerBeanName)
        );

    }

    private void lookupEnpointsImplementation(Class<?> controllerInterfaceClass, GenericApplicationContext genericApplicationContext, String replacerBeanName) {
        var beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(controllerInterfaceClass)
                .setLazyInit(true)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .getBeanDefinition();
        var methodOverrides = new MethodOverrides();
        findEndpointMethods(controllerInterfaceClass)
                .map(Method::getName)
                .forEach(name -> methodOverrides.addOverride(new ReplaceOverride(name, replacerBeanName)));

        beanDefinition.setMethodOverrides(methodOverrides);

        genericApplicationContext.registerBeanDefinition(controllerInterfaceClass.getSimpleName(), beanDefinition);
    }

    protected static Stream<Method> findEndpointMethods(Class<?> controllerInterfaceClass) {
        return Arrays.stream(controllerInterfaceClass.getDeclaredMethods())
                .filter(ControllersConfiguration::isEndpointMethod);
    }

    private static boolean isEndpointMethod(Method method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(annotation->endpointsAnnotations.stream().anyMatch(type-> type.isInstance(annotation)));
    }

    private Stream<Class<?>> controllerInterfacesForEndpointImplementation() {
        return Stream.of(PublicResources.class, IndicatorsResources.class);
    }


}
