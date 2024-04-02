package dev.revere.virago.client.events.player.window;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
public abstract class WindowClickRequest {
    private boolean completed;

    public abstract void performRequest();

    public boolean isCompleted() {
        return this.completed;
    }

    public void onCompleted() {
        this.completed = true;
    }
}