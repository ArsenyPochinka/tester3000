package ru.vtb.tester3000.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardEmv {

    @Schema(example = "90")
    private Long mbr;

    public Long getMbr() {
        return mbr;
    }

    public void setMbr(Long mbr) {
        this.mbr = mbr;
    }
}
