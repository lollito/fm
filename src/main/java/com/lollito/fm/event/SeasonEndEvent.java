package com.lollito.fm.event;

import org.springframework.context.ApplicationEvent;
import com.lollito.fm.model.Season;
import lombok.Getter;

@Getter
public class SeasonEndEvent extends ApplicationEvent {
    private final Season season;

    public SeasonEndEvent(Object source, Season season) {
        super(source);
        this.season = season;
    }
}
