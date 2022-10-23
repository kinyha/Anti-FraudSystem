package antifraud.service;

import antifraud.repository.CardAmountLimitsRepository;
import antifraud.model.CardAmountLimits;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardAmountLimitsService {

    private final CardAmountLimitsRepository repository;

    public CardAmountLimitsService(CardAmountLimitsRepository repository) {
        this.repository = repository;
    }

    public int processAmount(Long amount, String cardNumber) {
        boolean existsById = repository.existsById(cardNumber);

        if (!existsById) {
            CardAmountLimits amountLimits = new CardAmountLimits(cardNumber, 200L, 1500L);
            repository.save(amountLimits);
        }

        CardAmountLimits cardAmountLimits = repository.findById(cardNumber).get();

        Long maxAllowed = cardAmountLimits.getMaxAllowed();
        Long maxManual = cardAmountLimits.getMaxManual();

        if (amount <= maxAllowed) {
            return 1;
        } else if (amount <= maxManual) {
            return 2;
        } else {
            return 3;
        }
    }

    public void processLimits(String number, Long amount, String result, String feedbackRequest) {
        Optional<CardAmountLimits> optionalCardAmountLimits = repository.findById(number);

        if (optionalCardAmountLimits.isPresent()) {
            CardAmountLimits cardAmountLimits = optionalCardAmountLimits.get();
            switch (feedbackRequest) {
                case "ALLOWED":
                    if (result.equals("MANUAL_PROCESSING")) {
                        cardAmountLimits.setMaxAllowed(increaseLimit(cardAmountLimits.getMaxAllowed(), amount));
                    } else if (result.equals("PROHIBITED")) {
                        cardAmountLimits.setMaxManual(increaseLimit(cardAmountLimits.getMaxManual(), amount));
                        cardAmountLimits.setMaxAllowed(increaseLimit(cardAmountLimits.getMaxAllowed(), amount));
                    }
                    break;
                case "MANUAL_PROCESSING":
                    if (result.equals("ALLOWED")) {
                        cardAmountLimits.setMaxAllowed(decreaseLimit(cardAmountLimits.getMaxAllowed(), amount));
                    } else if (result.equals("PROHIBITED")) {
                        cardAmountLimits.setMaxManual(increaseLimit(cardAmountLimits.getMaxManual(), amount));
                    }
                    break;
                case "PROHIBITED":
                    if (result.equals("ALLOWED")) {
                        cardAmountLimits.setMaxAllowed(decreaseLimit(cardAmountLimits.getMaxAllowed(), amount));
                        cardAmountLimits.setMaxManual(decreaseLimit(cardAmountLimits.getMaxManual(), amount));
                    } else if (result.equals("MANUAL_PROCESSING")) {
                        cardAmountLimits.setMaxManual(decreaseLimit(cardAmountLimits.getMaxManual(), amount));
                    }
                    break;
            }

            repository.save(cardAmountLimits);
        }
    }

    private long increaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }

    private long decreaseLimit(long currentLimit, long amount) {
        return (long) Math.ceil(0.8 * currentLimit - 0.2 * amount);
    }
}
