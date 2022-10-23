package antifraud.dto;

import antifraud.model.StolenCard;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StolenCardResponse {
    private StolenCard stolenCard;
}
