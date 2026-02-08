package com.lollito.fm.event;

import org.springframework.context.ApplicationEvent;
import com.lollito.fm.model.ScoutingAssignment;
import lombok.Getter;

@Getter
public class ScoutingCompletedEvent extends ApplicationEvent {
    private final ScoutingAssignment assignment;

    public ScoutingCompletedEvent(Object source, ScoutingAssignment assignment) {
        super(source);
        this.assignment = assignment;
    }
}
