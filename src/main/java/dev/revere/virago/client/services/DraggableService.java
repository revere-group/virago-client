package dev.revere.virago.client.services;

import dev.revere.virago.api.draggable.Draggable;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.util.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Remi
 * @project Virago
 * @date 3/23/2024
 */
@Getter
public class DraggableService implements IService {
    private final LinkedHashMap<String, Draggable> draggables = new LinkedHashMap<>();

    @Override
    public void initService() {
        Logger.info("Draggable service initialized!", getClass());
    }

    /**
     * Adds a draggable.
     *
     * @param draggable the draggable
     * @return
     */
    public Draggable addDraggable(Draggable draggable) {
        draggables.put(draggable.getName(), draggable);
        return draggable;
    }

    /**
     * Gets a draggable by name.
     *
     * @param name the name
     * @return the draggable
     */
    public Draggable getDraggable(String name) {
        return draggables.get(name);
    }

    /**
     * Gets a list of all draggables.
     *
     * @return the draggable list
     */
    public List<Draggable> getDraggableList() {
        List<Draggable> draggableList = new ArrayList<>(draggables.values());
        if (draggables.isEmpty()) {
            Logger.err("No draggables found!", getClass());
        }
        return draggableList;
    }
}
