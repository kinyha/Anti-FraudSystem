package antifraud.dto;

import antifraud.model.Region;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Positive;
import java.util.Date;

@Getter
@Setter
public class TransactionRequest {
    @Positive
    private long amount;
    private String ip;
    private String number;
    private Region region;
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    private Date date;

    public TransactionRequest() {
    }

    public TransactionRequest(Long amount, String ip, String number, Region region, Date date) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
    }

}
