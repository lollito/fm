package com.lollito.fm.event;

import java.math.BigDecimal;
import org.springframework.context.ApplicationEvent;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import lombok.Getter;

@Getter
public class TransferCompletedEvent extends ApplicationEvent {
    private final Player player;
    private final Club buyerClub;
    private final Club sellerClub;
    private final BigDecimal amount;

    public TransferCompletedEvent(Object source, Player player, Club buyerClub, Club sellerClub, BigDecimal amount) {
        super(source);
        this.player = player;
        this.buyerClub = buyerClub;
        this.sellerClub = sellerClub;
        this.amount = amount;
    }
}
