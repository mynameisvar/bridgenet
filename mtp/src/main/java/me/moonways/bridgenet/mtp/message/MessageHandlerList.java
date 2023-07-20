package me.moonways.bridgenet.mtp.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.injection.Component;
import me.moonways.bridgenet.injection.DependencyInjection;
import me.moonways.bridgenet.injection.Inject;
import me.moonways.bridgenet.injection.proxy.ProxiedKeepTimeMethod;
import me.moonways.bridgenet.injection.proxy.ProxiedObject;
import me.moonways.bridgenet.mtp.message.exception.MessageHandleException;
import me.moonways.bridgenet.mtp.message.inject.MessageHandler;
import me.moonways.bridgenet.mtp.message.inject.MessageTrigger;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Component
@ProxiedObject
public class MessageHandlerList {

    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    @Getter
    private Set<HandlerMethodWrapper> messageHandlers;

    @Inject
    private DependencyInjection dependencyInjection;

    public void detectHandlers() {
        dependencyInjection.findComponentsIntoBasePackage(MessageHandler.class);
        messageHandlers = dependencyInjection.getContainer().getFoundComponents(MessageHandler.class)
                .stream()
                .flatMap(object -> Arrays.stream(object.getClass().getDeclaredMethods())
                        .map(method -> new HandlerMethodWrapper(object, object.getClass(), method)))
                .filter(method -> method.hasAnnotation(MessageTrigger.class))
                .collect(Collectors.toSet());
    }

    @ProxiedKeepTimeMethod
    public void handle(@NotNull MessageWrapper wrapper, @NotNull Object message) {
        for (HandlerMethodWrapper handlerMethod : messageHandlers) {

            Method method = handlerMethod.getMethod();
            Class<?> messageClass = wrapper.getMessageType();

            if (method.getParameterCount() != 1) {
                throw new MessageHandleException(
                        String.format("Can't handle message %s in handler %s", messageClass.getName(),
                                handlerMethod.getSourceClass().getName()));
            }

            Class<?> methodMessageClass = method.getParameterTypes()[0];
            if (messageClass.equals(methodMessageClass)) {
                try {
                    method.invoke(handlerMethod.getSource(), message);
                }
                catch (IllegalAccessException | InvocationTargetException exception) {
                    log.error(exception);
                }
            }
        }
    }

   //@ProxiedKeepTimeMethod
   //public void handle(@NotNull MessageWrapper wrapper, @NotNull Object message) {
   //    for (HandlerMethodWrapper handlerMethod : messageHandlers) {

   //        Method method = handlerMethod.getMethod();
   //        Class<?> messageClass = wrapper.getMessageType();

   //        //if (method.getParameterCount() != 1) {
   //        //    throw new MessageHandleException(
   //        //            String.format("Can't handle message %s in handler %s", messageClass.getName(),
   //        //                    handlerMethod.getSourceClass().getName()));
   //        //}

   //        try {
   //            MethodType methodType = MethodType.methodType(void.class, messageClass);

   //            MethodHandle methodHandle = PUBLIC_LOOKUP.findVirtual(handlerMethod.getSourceClass(), method.getName(), methodType);
   //            methodHandle.invoke(handlerMethod.getSource(), message);
   //        }
   //        catch (Throwable exception) {
   //            log.error(exception);
   //        }
   //    }
   //}

    @Getter
    @ToString
    @RequiredArgsConstructor
    public static class HandlerMethodWrapper {

        private final Object source;
        private final Class<?> sourceClass;

        private final Method method;

        public boolean hasAnnotation(Class<? extends Annotation> annotation) {
            return method.isAnnotationPresent(annotation);
        }
    }
}
