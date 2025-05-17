package org.grupouno.pubsub;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private final String name;
    private final List<ISubscriber> subscribers;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addSubscriber(ISubscriber ISubscriber) {
        subscribers.add(ISubscriber);
    }

    public void removeSubscriber(ISubscriber ISubscriber) {
        subscribers.remove(ISubscriber);
    }

    public void notifySubscribers(String message) {
        for (ISubscriber ISubscriber : subscribers) {
            ISubscriber.receiveMessage(message);
        }
    }
}
