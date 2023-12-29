package fr.insee.rmes.config;

import fr.insee.rmes.utils.EndPointMethodReplacer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Stream;


@Slf4j
public class ControllersConfiguration implements ApplicationListener<ApplicationContextInitializedEvent> {

    public static final String INTERFACE_CONTROLLERS_PACKAGE_KEY="fr.insee.rmes.interface-controllers.package";

    private static final Set<Class<? extends Annotation>> endpointsAnnotations = Set.of(RequestMapping.class,
            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class);

    private String interfaceControllerPackage;


    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        GenericApplicationContext genericApplicationContext= (GenericApplicationContext) event.getApplicationContext();
        interfaceControllerPackage=event.getApplicationContext().getEnvironment().getProperty(INTERFACE_CONTROLLERS_PACKAGE_KEY);

        var replacerBeanName = "endPointMethodReplacer";
        genericApplicationContext.registerBean(replacerBeanName, EndPointMethodReplacer.class);

        controllerInterfacesForEndpointImplementation().forEach(
                clazz -> lookupEnpointsImplementation(clazz, genericApplicationContext, replacerBeanName)
        );

    }


    private void lookupEnpointsImplementation(ScannedGenericBeanDefinition genericBeanDefinition, GenericApplicationContext genericApplicationContext, String replacerBeanName) {
        log.debug("Instanciation of interface controller {} as a bean", genericBeanDefinition.getBeanClassName());

        genericBeanDefinition.setLazyInit(true);
        genericBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        var methodOverrides = new MethodOverrides();
        findEndpointMethods(genericBeanDefinition)
                .map(MethodMetadata::getMethodName)
                .forEach(name -> {
                    log.trace("Add ReplaceOverride instance for method {}", name);
                    methodOverrides.addOverride(new ReplaceOverride(name, replacerBeanName));
                });

        genericBeanDefinition.setMethodOverrides(methodOverrides);

        genericApplicationContext.registerBeanDefinition(genericBeanDefinition.getBeanClassName(), genericBeanDefinition);
    }

    protected static Stream<MethodMetadata> findEndpointMethods(ScannedGenericBeanDefinition genericBeanDefinition) {
        return genericBeanDefinition.getMetadata().getDeclaredMethods().stream()
                .filter(ControllersConfiguration::isEndpointMethod);
    }

    private static boolean isEndpointMethod(MethodMetadata method) {
        return endpointsAnnotations.stream().anyMatch(method.getAnnotations()::isDirectlyPresent);
    }

    private Stream<ScannedGenericBeanDefinition> controllerInterfacesForEndpointImplementation() {
        return findAllInterfacesRestControllerInPackage(interfaceControllerPackage);
    }

    private Stream<ScannedGenericBeanDefinition> findAllInterfacesRestControllerInPackage(String interfaceControllerPackage) {
        var scanner=new ClassPathScanningCandidateComponentProvider(false){
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                var metadata=beanDefinition.getMetadata();
                return metadata.isIndependent() && metadata.isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var beandefs=scanner.findCandidateComponents(interfaceControllerPackage);
        return beandefs.stream().map(ScannedGenericBeanDefinition.class::cast);
    }


}
