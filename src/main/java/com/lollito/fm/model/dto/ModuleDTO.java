package com.lollito.fm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private Long id;
    private String name;
    private Integer cd;
    private Integer wb;
    private Integer mf;
    private Integer wng;
    private Integer fw;
}
