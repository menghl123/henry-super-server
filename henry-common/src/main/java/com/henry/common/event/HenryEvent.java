package com.henry.common.event;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.ApplicationEvent;

public class HenryEvent extends ApplicationEvent {
    public HenryEvent(final Object source) {
        super(source);
    }

    public void publish() {
        final EventService eventService = SpringUtil.getBean(EventService.class);
        eventService.publish(this);
    }
}
