package com.henry.common.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EventService {
    private final ApplicationEventPublisher eventPublisher;

    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
