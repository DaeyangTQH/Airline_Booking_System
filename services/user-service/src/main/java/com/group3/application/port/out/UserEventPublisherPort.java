package com.group3.application.port.out;

public interface UserEventPublisherPort {

    void publishProfileUpdated(String email);

}