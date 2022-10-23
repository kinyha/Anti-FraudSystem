package antifraud.dto;

import antifraud.model.Status;

public class FeedbackRequest {
    private Long transactionId;
    private Status feedback;

    public FeedbackRequest() {
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Status getFeedback() {
        return feedback;
    }

    public void setFeedback(Status feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "FeedbackRequest{" +
                "transactionId=" + transactionId +
                ", feedback=" + feedback +
                '}';
    }
}
