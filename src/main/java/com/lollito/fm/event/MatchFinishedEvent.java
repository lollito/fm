package com.lollito.fm.event;

import org.springframework.context.ApplicationEvent;
import com.lollito.fm.model.Match;
import lombok.Getter;

@Getter
public class MatchFinishedEvent extends ApplicationEvent {
    private final Match match;

    public MatchFinishedEvent(Object source, Match match) {
        super(source);
        this.match = match;
    }
}
