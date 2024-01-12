package fr.insee.rmes.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;


@Slf4j
public class ControllersConfiguration implements ApplicationListener<ApplicationContextInitializedEvent> {

    public static final String INTERFACE_CONTROLLERS_PACKAGE_KEY = "fr.insee.rmes.interface-controllers.package";

    private static final Set<Class<? extends Annotation>> endpointsAnnotations = Set.of(RequestMapping.class,
            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class);

    private String interfaceControllerPackage;


    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) event.getApplicationContext();
        interfaceControllerPackage = genericApplicationContext.getEnvironment().getProperty(INTERFACE_CONTROLLERS_PACKAGE_KEY);


        controllerInterfacesForEndpointImplementation().forEach(
                metadata -> lookupEnpointsImplementation(metadata, genericApplicationContext)
        );
    }


    private void lookupEnpointsImplementation(AnnotationMetadata classMetadata, GenericApplicationContext genericApplicationContext) {
        log.debug("Declaration of interface controller {} as a bean", classMetadata.getClassName());

        Class<?> controllerInterface;
        try {
            // LIKE org.springframework.data.util.AnnotatedTypeScanner.findTypes  !!
            controllerInterface = ClassUtils.forName(classMetadata.getClassName(), null);
        } catch (ClassNotFoundException e) {
            log.error("Unable to find the class for " + classMetadata.getClassName(), e);
            return;
        }
        var resourceFactory = new ProxyFactoryBean();
        resourceFactory.setSingleton(true);
        resourceFactory.setInterfaces(controllerInterface);

        var advisorBeanDefinition=registerInterceptorBeanForEndpointMethods(classMetadata);
        var advisorName = classMetadata.getClassName() + "_endpointsReplacer";
        genericApplicationContext.registerBeanDefinition(advisorName, advisorBeanDefinition);
        resourceFactory.setInterceptorNames(advisorName);
        log.trace("Advisor {} registred",advisorName);
        genericApplicationContext.registerBean(classMetadata.getClassName(), FactoryBean.class, () -> resourceFactory);

    }

    protected static BeanDefinition registerInterceptorBeanForEndpointMethods(AnnotationMetadata classMetadata) {

        var advisorBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(NameMatchEnpointMethodPointcutAdvisor.class)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .getBeanDefinition();
        var propertyValues = new MutablePropertyValues();
        var namesOfMethodsToIntercept=classMetadata.getDeclaredMethods().stream()
                .filter(ControllersConfiguration::isEndpointMethod)
                .map(MethodMetadata::getMethodName)
                .toArray(String[]::new);
        propertyValues.addPropertyValue("mappedNames", namesOfMethodsToIntercept);
        advisorBeanDefinition.setPropertyValues(propertyValues);

        log.atTrace().log(()->"Declare advisor to intercept methods "+ Arrays.toString(namesOfMethodsToIntercept));

        return advisorBeanDefinition;
    }

    private static boolean isEndpointMethod(MethodMetadata method) {
        return endpointsAnnotations.stream().anyMatch(method.getAnnotations()::isDirectlyPresent);
    }

    private Stream<? extends AnnotationMetadata> controllerInterfacesForEndpointImplementation() {
        return findAllInterfacesRestControllerInPackage(interfaceControllerPackage);
    }

    private Stream<? extends AnnotationMetadata> findAllInterfacesRestControllerInPackage(String interfaceControllerPackage) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                var metadata = beanDefinition.getMetadata();
                return metadata.isIndependent() && metadata.isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var beandefs = scanner.findCandidateComponents(interfaceControllerPackage);
        return beandefs.stream().map(ScannedGenericBeanDefinition.class::cast)
                .map(ScannedGenericBeanDefinition::getMetadata);
    }


}
