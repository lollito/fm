package com.lollito.fm.model.dto;

import com.lollito.fm.model.ManagerPerk;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockPerkRequest {
    private ManagerPerk perkId;
}
