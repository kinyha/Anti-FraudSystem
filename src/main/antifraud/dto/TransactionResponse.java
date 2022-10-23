package antifraud.dto;


import antifraud.model.Status;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class TransactionResponse {
    private Status result;
    private String info;
}