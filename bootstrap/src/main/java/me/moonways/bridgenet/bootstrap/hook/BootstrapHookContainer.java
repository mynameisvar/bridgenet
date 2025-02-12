package me.moonways.bridgenet.bootstrap.hook;

import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.api.inject.Inject;
import me.moonways.bridgenet.api.inject.bean.service.BeansService;
import me.moonways.bridgenet.assembly.ResourcesAssembly;
import me.moonways.bridgenet.assembly.ResourcesTypes;
import me.moonways.bridgenet.bootstrap.xml.XMLBootstrapConfigDescriptor;
import me.moonways.bridgenet.bootstrap.xml.XMLHookDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public final class BootstrapHookContainer {

    private final Map<Class<? extends BootstrapHook>, BootstrapHook> instancesByHooksTypesMap = new HashMap<>();
    private final Map<Class<?>, BootstrapHookPriority> prioririesByHooksTypesMap = new HashMap<>();
    private final Map<Class<?>, XMLHookDescriptor> xmlByHooksTypesMap = new HashMap<>();

    @Inject
    private BeansService beansService;
    @Inject
    private ResourcesAssembly assembly;

    private XMLBootstrapConfigDescriptor parseConfiguration() {
        return assembly.readXmlAtEntity(ResourcesTypes.BOOTSTRAP_XML,
                XMLBootstrapConfigDescriptor.class);
    }

    public void bindHooks() {
        if (!xmlByHooksTypesMap.isEmpty()) {
            return;
        }

        log.debug("BootstrapHookContainer.bindHooks() => begin;");

        XMLBootstrapConfigDescriptor xmlBootstrap = parseConfiguration();
        List<XMLHookDescriptor> hooks = xmlBootstrap.getHooks();

        for (XMLHookDescriptor xmlHook : hooks) {

            BootstrapHook bootstrapHook = parseHook(xmlHook);
            if (bootstrapHook == null)
                continue;

            log.debug("Hook '{}' was success parsed", xmlHook.getDisplayName());

            Class<? extends BootstrapHook> cls = bootstrapHook.getClass();

            xmlByHooksTypesMap.put(cls, xmlHook);
            instancesByHooksTypesMap.put(cls, bootstrapHook);
        }

        log.debug("BootstrapHookContainer.bindHooks() => end;");
    }

    private BootstrapHook parseHook(XMLHookDescriptor xmlHook) {
        final String displayName = xmlHook.getDisplayName();
        final String priorityName = xmlHook.getPriority();
        final String executorPath = xmlHook.getExecutorPath();

        try {
            final Class<?> cls = Class.forName(executorPath);
            Class<? extends BootstrapHook> hookCls = cls.asSubclass(BootstrapHook.class);

            BootstrapHookPriority priority = Enum.valueOf(BootstrapHookPriority.class, priorityName.toUpperCase());
            prioririesByHooksTypesMap.put(hookCls, priority);

            return hookCls.newInstance();
        } catch (Exception exception) {
            log.error("§4Cannot be parse hook '{}' from xml", displayName, exception);
        }

        return null;
    }

    public String findHookName(Class<? extends BootstrapHook> cls) {
        return xmlByHooksTypesMap.get(cls).getDisplayName();
    }

    public int findHookPriorityID(Class<? extends BootstrapHook> cls) {
        final XMLHookDescriptor xmlHook = xmlByHooksTypesMap.get(cls);
        final int defaultPriorityID = -1;

        if (xmlHook == null) {
            return defaultPriorityID;
        }

        final String priorityIDString = xmlHook.getPriorityID();
        if (priorityIDString == null) {
            return defaultPriorityID;
        }

        return Integer.parseInt(priorityIDString);
    }

    public BootstrapHook findHookInstance(Class<? extends BootstrapHook> cls) {
        BootstrapHook bootstrapHook = instancesByHooksTypesMap.get(cls);
        beansService.inject(bootstrapHook);
        return bootstrapHook;
    }

    public BootstrapHookPriority findHookPriority(Class<? extends BootstrapHook> cls) {
        return prioririesByHooksTypesMap.get(cls);
    }

    public Collection<BootstrapHook> getRegisteredHooks(@NotNull BootstrapHookPriority scope) {
        return instancesByHooksTypesMap.keySet()
                .stream()
                .filter(cls -> Objects.equals(findHookPriority(cls), scope))
                .map(this::findHookInstance)
                .collect(Collectors.toSet());
    }

    public Collection<BootstrapHook> findOrderedHooks(@NotNull BootstrapHookPriority priority) {
        Collection<BootstrapHook> registeredHooks = getRegisteredHooks(priority);

        if (registeredHooks.size() > 1) {
            if (registeredHooks.stream()
                    .filter(hook -> findHookPriorityID(hook.getClass()) >= 0)
                    .count() != registeredHooks.size()) {

                log.error("§4Registered hooks ({}) is not marked by priority ID", joinHooksToNamesLine(registeredHooks));
                return null;
            }

            Comparator<BootstrapHook> comparator = Comparator.comparingInt(
                    hook -> findHookPriorityID(hook.getClass()));

            registeredHooks = registeredHooks.stream().sorted(comparator).collect(Collectors.toList());
        }

        log.debug("Found §6{} §rregistered hooks: [{}]", registeredHooks.size(), joinHooksToNamesLine(registeredHooks));
        return registeredHooks;
    }

    private String joinHooksToNamesLine(Collection<BootstrapHook> registeredHooks) {
        return registeredHooks.stream()
                .map(instance -> findHookName(instance.getClass()))
                .collect(Collectors.joining(", "));
    }

    public void unbind(Class<? extends BootstrapHook> cls) {
        xmlByHooksTypesMap.remove(cls);
        instancesByHooksTypesMap.remove(cls);
    }
}
