package com.lollito.fm.model.dto;
import lombok.Builder;
import lombok.Data;
import com.lollito.fm.model.ConfigCategory;
import com.lollito.fm.model.ConfigType;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigurationDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String configDescription;
    private ConfigCategory category;
    private ConfigType valueType;
    private Boolean isEditable;
    private LocalDateTime lastModified;
    private String modifiedBy;
}
