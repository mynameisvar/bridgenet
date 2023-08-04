package me.moonways.bridgenet.rsi.service;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.Depend;
import me.moonways.bridgenet.api.jaxb.XmlJaxbParser;
import me.moonways.bridgenet.rsi.endpoint.Endpoint;
import me.moonways.bridgenet.rsi.endpoint.EndpointController;
import me.moonways.bridgenet.rsi.module.*;
import me.moonways.bridgenet.rsi.xml.XmlModule;
import me.moonways.bridgenet.rsi.xml.XmlConfiguration;
import me.moonways.bridgenet.rsi.xml.XmlService;
import me.moonways.bridgenet.api.inject.DependencyInjection;
import me.moonways.bridgenet.api.inject.PostConstruct;
import me.moonways.bridgenet.api.inject.Inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

@Getter
@Log4j2
@Depend
public final class RemoteServiceRegistry {

    private XmlConfiguration xmlConfiguration;

    private final Map<String, ServiceInfo> servicesInfos = Collections.synchronizedMap(new HashMap<>());
    private final Map<ServiceInfo, RemoteService> servicesImplements = Collections.synchronizedMap(new HashMap<>());

    private final Map<ModuleID, ModuleFactory> modulesFactories = Collections.synchronizedMap(new HashMap<>());

    private final Map<ServiceInfo, ServiceModulesContainer> modulesContainerMap = Collections.synchronizedMap(new HashMap<>());

    @Inject
    private DependencyInjection dependencyInjection;

    @Inject
    private XmlJaxbParser jaxbParser;

    private final EndpointController endpointController = new EndpointController();

    @PostConstruct
    void init() {
        dependencyInjection.injectFields(endpointController);
    }

    public void initializeXmlConfiguration() {
        xmlConfiguration = jaxbParser.parseCopiedResource(getClass().getClassLoader(), "rsiconfig.xml", XmlConfiguration.class);

        log.info("Parsed RMI XML-Configuration content: {}", xmlConfiguration);

        List<XmlModule> xmlModulesList = xmlConfiguration.getModulesList();
        List<XmlService> xmlServicesList = xmlConfiguration.getServicesList();

        if (xmlModulesList != null) {
            initXmlModules(xmlModulesList);
        }

        if (xmlServicesList != null) {
            initXmlServices(xmlServicesList);
        }
    }

    public void initializeEndpointsController() {
        endpointController.injectInternalComponents();
        endpointController.findEndpoints();

        List<Endpoint> endpointsList = endpointController.getEndpoints();

        for (Endpoint endpoint : endpointsList) {

            ServiceInfo serviceInfo = endpoint.getServiceInfo();
            ServiceModulesContainer serviceModulesContainer = new ServiceModulesContainer(serviceInfo);

            for (ModuleFactory moduleFactory : modulesFactories.values()) {
                serviceModulesContainer.injectModule(moduleFactory);
            }

            modulesContainerMap.put(serviceInfo, serviceModulesContainer);
        }

        endpointController.bindEndpoints();
    }

    private void initXmlModules(List<XmlModule> xmlModulesList) {
        log.info("§e{} §rmodules found", xmlModulesList.size());
        log.info("Running modules registration process...");

        for (XmlModule xmlService : xmlModulesList) {

            String name = xmlService.getName().toUpperCase();
            String configClass = xmlService.getConfigClass();
            String targetClass = xmlService.getTargetClass();

            log.info("Register module: §f{} §r(targetClass={}, configClass={})", name, targetClass, configClass);
            registerModule(xmlService);
        }
    }

    private void initXmlServices(List<XmlService> xmlServicesList) {
        log.info("§e{} §rremote services found", xmlServicesList.size());
        log.info("Running remote services registration process...");

        for (XmlService xmlService : xmlServicesList) {

            String name = xmlService.getName().toUpperCase();
            String bindPort = xmlService.getBindPort();
            String modelPath = xmlService.getModelPath();

            log.info("Register service: §f{} §r(port={}, class={})", name, bindPort, modelPath);

            servicesInfos.put(name.toLowerCase(), createServiceInfo(xmlService));
        }
    }

    private ServiceInfo createServiceInfo(XmlService wrapper) {
        String name = wrapper.getName();
        Class<?> modelClass;

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            modelClass = classLoader.loadClass(wrapper.getModelPath());
        }
        catch (ClassNotFoundException exception) {
            throw new ServiceException(exception);
        }

        if (!RemoteService.class.isAssignableFrom(modelClass) || !modelClass.isInterface()) {
            throw new ServiceException("model of service " + name + " is not valid");
        }

        int port = Integer.parseInt(wrapper.getBindPort());

        @SuppressWarnings("unchecked") ServiceInfo serviceInfo = new ServiceInfo(
                name, port, (Class<? extends RemoteService>) modelClass
        );

        return serviceInfo;
    }

    private RemoteService findServiceInstance(ServiceInfo serviceInfo) {
        return servicesImplements.get(serviceInfo);
    }

    private void registerService(ServiceInfo serviceInfo, RemoteService remoteService) {
        dependencyInjection.injectFields(remoteService);

        log.info("Service {} was registered", serviceInfo.getName());
        servicesImplements.put(serviceInfo, remoteService);
    }

    private ModuleID getModuleID(Class<? extends RemoteModule> cls) {
        ModuleID moduleID;
        try {
            RemoteModule<?> module = cls.newInstance();
            moduleID = module.getId();
        }
        catch (InstantiationException | IllegalAccessException exception) {
            throw new ServiceException(exception);
        };

        return moduleID;
    }

    private ModuleFactory createModuleFactory(XmlModule wrapper) {
        Class<?> targetClass;
        Class<?> configClass;

        try {
            ClassLoader classLoader = getClass().getClassLoader();

            targetClass = classLoader.loadClass(wrapper.getTargetClass());
            configClass = classLoader.loadClass(wrapper.getConfigClass());
        }
        catch (ClassNotFoundException exception) {
            throw new ServiceException(exception);
        }

        final Class<? extends RemoteModule> checkedModuleClass = targetClass.asSubclass(RemoteModule.class);
        final ModuleID moduleID = getModuleID(checkedModuleClass);

        Function<ServiceInfo, RemoteModule<?>> factoryFunc = (serviceInfo) -> {
            try {
                RemoteModule<?> module = checkedModuleClass.newInstance();
                dependencyInjection.injectFields(module);

                Method bindMethod = Arrays.stream(checkedModuleClass.getMethods())
                        .filter(method -> method.getName().equals("bind"))
                        .findFirst()
                        .orElse(null);

                bindMethod.invoke(module, xmlConfiguration, serviceInfo, configClass);

                return (RemoteModule<?>) module;
            }
            catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                throw new ServiceException(exception);
            }
        };

        return new ModuleFactory(moduleID, factoryFunc);
    }

    private void registerModule(XmlModule wrapper) {
        ModuleFactory moduleFactory = createModuleFactory(wrapper);
        ModuleID id = moduleFactory.getId();

        log.info("{} was registered", id);

        modulesFactories.put(id, moduleFactory);
    }
}
