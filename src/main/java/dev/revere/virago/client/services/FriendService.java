package dev.revere.virago.client.services;

import dev.revere.virago.api.service.IService;
import dev.revere.virago.util.Logger;

import java.util.ArrayList;
import java.util.List;


public class FriendService implements IService {
    private List<String> friends;

    @Override
    public void initService() {
       friends = new ArrayList<>();
       Logger.info("Friend service initialized!", getClass());
    }

    public boolean isFriend(String string) {
        return friends.contains(string);
    }

    public void addFriend(String string) {
        friends.add(string);
    }

    public void removeFriend(String string) {
        friends.remove(string);
    }
}
