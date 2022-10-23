package antifraud.model;

import javax.persistence.Id;

import javax.persistence.Entity;

@Entity
public class CardAmountLimits {

    @Id
    private String cardNumber;
    private Long maxAllowed;
    private Long maxManual;

    public CardAmountLimits() {
    }

    public CardAmountLimits(String cardNumber, Long maxAllowed, Long maxManual) {
        this.cardNumber = cardNumber;
        this.maxAllowed = maxAllowed;
        this.maxManual = maxManual;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getMaxAllowed() {
        return maxAllowed;
    }

    public void setMaxAllowed(Long maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    public Long getMaxManual() {
        return maxManual;
    }

    public void setMaxManual(Long maxManual) {
        this.maxManual = maxManual;
    }

    @Override
    public String toString() {
        return "CardAmountLimits{" +
                "cardNumber='" + cardNumber + '\'' +
                ", maxAllowed=" + maxAllowed +
                ", maxManual=" + maxManual +
                '}';
    }
}
