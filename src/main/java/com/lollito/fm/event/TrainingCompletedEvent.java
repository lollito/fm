package com.lollito.fm.event;

import org.springframework.context.ApplicationEvent;
import com.lollito.fm.model.TrainingSession;
import lombok.Getter;

@Getter
public class TrainingCompletedEvent extends ApplicationEvent {
    private final TrainingSession session;

    public TrainingCompletedEvent(Object source, TrainingSession session) {
        super(source);
        this.session = session;
    }
}
