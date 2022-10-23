package antifraud.model;


import antifraud.dto.TransactionRequest;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "transaction")
public class Transaction {

    @Id
   //@GeneratedValue
    @SequenceGenerator(name="seq",sequenceName="h2_seq")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
    private Long transactionId;

    @Column(name = "amount")
    private long amount;

    @Column(name = "ip")
    private String ip;

    @Column(name = "number")
    private String number;

    @Enumerated(EnumType.STRING)
    private Region region;

    private LocalDateTime date;


    private String result;
    private String feedback;

    public Transaction() {
    }

    public Transaction(Long id, Long amount, String ip, String number, Region region, LocalDateTime date) {
        this.transactionId = id;
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = "";
        this.feedback = "";
    }

    public Transaction(TransactionRequest transactionRequest) {
        this.amount = transactionRequest.getAmount();
        this.ip = transactionRequest.getIp();
        this.number = transactionRequest.getNumber();
        this.region = transactionRequest.getRegion();
        this.date = LocalDateTime.parse((CharSequence) transactionRequest.getDate());
        this.result = "";
        this.feedback = "";
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!Objects.equals(ip, that.ip)) return false;
        if (!Objects.equals(number, that.number)) return false;
        return region == that.region;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + transactionId +
                ", amount=" + amount +
                ", ip='" + ip + '\'' +
                ", number='" + number + '\'' +
                ", region=" + region +
                ", date=" + date +
                ", result='" + result + '\'' +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}
