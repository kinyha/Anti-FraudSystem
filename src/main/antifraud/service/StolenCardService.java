package antifraud.service;

import antifraud.repository.StolenCardRepository;
import antifraud.dto.StolenCardRequest;
import antifraud.model.StolenCard;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
public class StolenCardService {
    private final StolenCardRepository stolenCardRepository;

    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public void process(StolenCardRequest request, StolenCard response) {
        if (checkCard(request.getNumber())) {

            if (isCardNumberUnique(request.getNumber())) {
                response.setNumber(request.getNumber());
                stolenCardRepository.save(response);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
            response.setNumber(request.getNumber());
            stolenCardRepository.save(response);
        }
    }

    boolean checkCard(String number) {
        if (isCardNumberValid(number)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isCardNumberUnique(String number) {
        return !stolenCardRepository.existsByNumber(number);
    }

    public void delete(String number) {
        StolenCardRequest request = new StolenCardRequest();
        request.setNumber(number);
        if (!checkCard(request.getNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (stolenCardRepository.existsByNumber(number)) {
            stolenCardRepository.delete(stolenCardRepository.findByNumber(number));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public static boolean isCardNumberValid(String cardNumber) { //algo luna
        if (cardNumber == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        var numbers = stringToIntArray(cardNumber);
        var numbersSize = numbers.length;

        for (int i = 0; i < numbersSize; i++) {
            if (i % 2 == 0) {
                numbers[i] *= 2;
                var oddNUmber = numbers[i];
                if (oddNUmber > 9) {
                    numbers[i] = Arrays.stream(stringToIntArray(String.valueOf(oddNUmber))).sum();
                }
            }
        }
        int sumOfDigits = Arrays.stream(numbers).sum();

        return sumOfDigits % 10 == 0;
    }

    public static int[] stringToIntArray(String string) {
        return string.chars()
                .map(c -> c - '0')
                .toArray();
    }

}
