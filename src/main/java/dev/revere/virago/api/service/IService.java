package dev.revere.virago.api.service;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
public interface IService {
    /**
     * Initializes the service.
     */
    default void initService() {}

    /**
     * Starts the service.
     */
    default void startService() {}

    /**
     * Stops the service.
     */
    default void stopService() {}

    /**
     * Destroys the service.
     * Save tasks must be performed here.
     * This method is called when all other activities are done.
     */
    default void destroyService() {}
}
