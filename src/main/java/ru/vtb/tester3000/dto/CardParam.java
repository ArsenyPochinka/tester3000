package ru.vtb.tester3000.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "CardParam")
public class CardParam {

    @Valid
    @Schema(description = "Параметры auth карты")
    private CardAuth auth;

    @NotBlank
    @Schema(example = "15c812a5-edc4-4dd2-a842-6fd53be44369")
    private String plasticId;

    @NotBlank
    @Schema(example = "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a")
    private String cardId;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-[01]\\d-[0-3]\\dT[012]\\d:[0-5]\\d:[0-5]\\d\\.\\d{3}$")
    @Schema(example = "2030-01-01T00:00:00.000")
    private String expDate;

    @NotNull
    @Valid
    private CardEmv emv;

    private String entryMode;

    public CardAuth getAuth() {
        return auth;
    }

    public void setAuth(CardAuth auth) {
        this.auth = auth;
    }

    public String getPlasticId() {
        return plasticId;
    }

    public void setPlasticId(String plasticId) {
        this.plasticId = plasticId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public CardEmv getEmv() {
        return emv;
    }

    public void setEmv(CardEmv emv) {
        this.emv = emv;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }
}
