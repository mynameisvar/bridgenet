package me.moonways.bridgenet.api.injection.scanner.controller;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.injection.DependencyContainer;
import me.moonways.bridgenet.api.injection.DependencyInjection;
import me.moonways.bridgenet.api.injection.factory.ObjectFactory;
import me.moonways.bridgenet.api.injection.scanner.ScannerFilter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

@Log4j2
public class ComponentScannerController implements ScannerController {

    private final TypeAnnotationsScanner typeAnnotationsScanner = new TypeAnnotationsScanner();
    private final SubTypesScanner subTypesScanner = new SubTypesScanner(false);
    private final ResourcesScanner resourcesScanner = new ResourcesScanner();

    private Set<Class<?>> mergeSets(Set<Set<Class<?>>> setOfSets) {
        Set<Class<?>> resultSet = new HashSet<>();

        for (Set<Class<?>> internal : setOfSets) {
            resultSet.addAll(internal);
        }

        return resultSet;
    }

    private Configuration createConfiguration(ScannerFilter scannerFilter) {
        log.info("Create scanner configuration by {}", scannerFilter);

        List<ClassLoader> classLoadersList = new LinkedList<>();

        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        FilterBuilder inputsFilter = new FilterBuilder()
                .includePackage(scannerFilter.getPackageNames().toArray(new String[0]));

        Collection<URL> urls = ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]));

        return new ConfigurationBuilder()
                .setUrls(urls)

                .setScanners(typeAnnotationsScanner, subTypesScanner, resourcesScanner)
                .filterInputsBy(inputsFilter);
    }

    @Override
    public Set<Class<?>> findAllComponents(@NotNull ScannerFilter filter) {
        Configuration configuration = createConfiguration(filter);

        Reflections reflections = new Reflections(configuration);

        Set<Class<? extends Annotation>> annotations = filter.getAnnotations();
        Set<Set<Class<?>>> setOfSets = new HashSet<>();

        for (Class<? extends Annotation> annotation : annotations) {
            Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(annotation);
            setOfSets.add(typesAnnotatedWith);
        }

        return mergeSets(setOfSets);
    }

    @Override
    public void whenFound(@NotNull DependencyInjection dependencyInjection,
                          @NotNull Class<?> resource,
                          @NotNull Class<? extends Annotation> annotation) {

        DependencyContainer container = dependencyInjection.getContainer();

        if (resource == DependencyInjection.class) {

            dependencyInjection.bind(this);
            container.addComponentWithAnnotation(resource, annotation);
            return;
        }

        if (container.isComponentFound(resource))
            return;

        ObjectFactory objectFactory = dependencyInjection.getScanner().getObjectFactory(annotation);
        Object object = objectFactory.create(resource);

        bind(dependencyInjection, resource, object, annotation);
    }

    public final void bind(@NotNull DependencyInjection dependencyInjection,
                     @NotNull Class<?> resource,
                     @NotNull Object object,
                     @NotNull Class<? extends Annotation> annotation) {

        DependencyContainer container = dependencyInjection.getContainer();

        dependencyInjection.bind(resource, object);
        container.addComponentWithAnnotation(resource, annotation);
    }
}