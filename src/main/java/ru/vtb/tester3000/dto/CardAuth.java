package ru.vtb.tester3000.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardAuth {

    @Schema(example = "true")
    private Boolean presence;
    private Boolean pinChecked;

    public Boolean getPresence() {
        return presence;
    }

    public void setPresence(Boolean presence) {
        this.presence = presence;
    }

    public Boolean getPinChecked() {
        return pinChecked;
    }

    public void setPinChecked(Boolean pinChecked) {
        this.pinChecked = pinChecked;
    }
}
