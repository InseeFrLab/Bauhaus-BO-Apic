package fr.insee.rmes.config;

import fr.insee.rmes.utils.ClassificationsResources;
import fr.insee.rmes.utils.EndPointMethodReplacer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
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
public class ControllersConfiguration implements ApplicationListener<ApplicationContextInitializedEvent>{

    public static final String INTERFACE_CONTROLLERS_PACKAGE_KEY="fr.insee.rmes.interface-controllers.package";

    private static final Set<Class<? extends Annotation>> endpointsAnnotations = Set.of(RequestMapping.class,
            GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class);

    private String interfaceControllerPackage;


    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        GenericApplicationContext genericApplicationContext= (GenericApplicationContext) event.getApplicationContext();
        interfaceControllerPackage=genericApplicationContext.getEnvironment().getProperty(INTERFACE_CONTROLLERS_PACKAGE_KEY);

        var replacerBeanName = "endPointMethodReplacer";
        genericApplicationContext.registerBean(replacerBeanName, EndPointMethodReplacer.class);

        controllerInterfacesForEndpointImplementation().forEach(
                beanDefinition -> lookupEnpointsImplementation(beanDefinition, genericApplicationContext, replacerBeanName)
        );

        /*var beanDefinition= BeanDefinitionBuilder.genericBeanDefinition(ClassificationsResources.class)
                .setLazyInit(false)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .getBeanDefinition();

        var methodOverrides=new MethodOverrides();
        methodOverrides.addOverride(new ReplaceOverride("updateClassification", replacerBeanName));
        beanDefinition.setMethodOverrides(methodOverrides);*/
        var resourceFactory=new ProxyFactoryBean();
        var methodName="updateClassification";
        var advisorName=ClassificationsResources.class.getCanonicalName()+"_"+methodName+"_replacer";
        genericApplicationContext.registerBean(advisorName,NameMatchEnpointMethodPointcutAdvisor.class);
        resourceFactory.setSingleton(true);
        //resourceFactory.setTargetClass(ClassificationsResources.class);
        resourceFactory.setInterfaces(ClassificationsResources.class);
        //resourceFactory.addAdvisor(methodReplacementAdvisor);
        resourceFactory.setInterceptorNames(advisorName);
        genericApplicationContext.registerBean("toto",FactoryBean.class, ()->resourceFactory);
    }


    private void lookupEnpointsImplementation(ScannedGenericBeanDefinition genericBeanDefinition, GenericApplicationContext genericApplicationContext, String replacerBeanName) {
        log.debug("Declaration of interface controller {} as a bean", genericBeanDefinition.getBeanClassName());

        /*
        ScannedGenericBeanDefinition(MetadataReader metadataReader)
		  this.metadata = metadataReader.getAnnotationMetadata();
		  setBeanClassName(this.metadata.getClassName());
		  setResource(metadataReader.getResource());
         */
        /*default values to keep :
          genericBeanDefinition.setAbstract(false);
         */
        /*Unused properties :
          genericBeanDefinition.setOriginatingBeanDefinition();
          genericBeanDefinition.setParentName();
         */
        /*
        Errors :
          genericBeanDefinition.getBeanClass() => java.lang.IllegalStateException: Bean class name [fr.insee.rmes.bauhauscontrollers....] has not been resolved into an actual Class
         */
        genericBeanDefinition.setLazyInit(false);

        genericBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        //the bean must not be synthetic because if it is, AbstractAutowireCapableBeanFactory.initializeBean does not call `applyBeanPostProcessorsAfterInitialization()`
        genericBeanDefinition.setSynthetic(false);
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
