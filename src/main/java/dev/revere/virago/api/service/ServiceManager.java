package dev.revere.virago.api.service;

import lombok.Getter;

import java.util.LinkedHashMap;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
public class ServiceManager {
    @Getter
    private final LinkedHashMap<Class<?>, IService> services = new LinkedHashMap<>();

    /**
     * Get the instance of a service by class
     *
     * @param service the class of the service
     * @param <T>     the type of the service
     * @return the instance of the service
     */
    public <T extends IService> T getService(Class<T> service) {
        return (T) services.get(service);
    }

    /**
     * Add a new service to the map
     *
     * @param serviceInstance the instance of the service
     */
    public void addService(IService serviceInstance) {
        services.put(serviceInstance.getClass(), serviceInstance);
    }
}