package antifraud.service;

import antifraud.dto.FeedbackRequest;
import antifraud.dto.TransactionResponse;
import antifraud.exceptions.HttpConflictException;
import antifraud.exceptions.UnprocessableEntityException;
import antifraud.model.Transaction;
import antifraud.repository.IpRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static antifraud.model.Status.*;

@Service
public class TransactionService {

    private final IpRepository ipRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final CardAmountLimitsService cardAmountLimitsService;


    static Transaction thirdTransaction = new Transaction();

    public TransactionService(IpRepository ipRepository, StolenCardRepository stolenCardRepository, TransactionRepository transactionRepository, CardAmountLimitsService cardAmountLimitsService) {
        this.ipRepository = ipRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
        this.cardAmountLimitsService = cardAmountLimitsService;
    }


    public void process(Transaction request, TransactionResponse response) {
        Long amount = request.getAmount();
        String number = request.getNumber();
        int validateAmount = validateAmount(amount, number);


        if (!StolenCardService.isCardNumberValid(request.getNumber()) || !IpService.checkCorrect(request.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (request.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (validateAmount == 1) {
            response.setResult(ALLOWED);
            request.setResult("ALLOWED");
            if (ipRepository.existsByIp(request.getIp()) || stolenCardRepository.existsByNumber(request.getNumber())) {
                response.setInfo("none");
                checkIpAndNumber(request, response, "");
            } else {
                response.setInfo("none");
            }
        } else if (validateAmount == 2) {
            response.setResult(MANUAL_PROCESSING);
            request.setResult("MANUAL_PROCESSING");
            if (ipRepository.existsByIp(request.getIp()) || stolenCardRepository.existsByNumber(request.getNumber())) {
                checkIpAndNumber(request, response, "");
            } else {

                response.setInfo("amount");
            }
        } else if (validateAmount == 3) {
            response.setResult(PROHIBITED);
            request.setResult("PROHIBITED");
            if (ipRepository.existsByIp(request.getIp()) || stolenCardRepository.existsByNumber(request.getNumber())) {
                checkIpAndNumber(request, response, "amount, ");
            } else {
                response.setInfo("amount");
            }
        }


        LocalDateTime transactionTime = request.getDate();
        LocalDateTime transactionTimeMinusHours = transactionTime.minusHours(1);
        List<Transaction> transactions = transactionRepository.findAllByNumberAndDateBetween(request.getNumber(),
                transactionTimeMinusHours, transactionTime);

        if (transactions.size() != 0) {
            Set<Transaction> allowTransactions = getAllowTransaction(transactions); //find 2 iniq transactions
            System.out.println(allowTransactions);


            int countIp = 0;
            int countRegion = 0;

            for (Transaction transaction : transactions) {
                if (transactions.size() >= 3 && !allowTransactions.contains(transaction)) {
                    for (Transaction allowTransaction : allowTransactions) {
                        if (!transaction.getIp().equals(allowTransaction.getIp())) {
                            countIp++;
                        }
                        if (!transaction.getRegion().equals(allowTransaction.getRegion())) {
                            countRegion++;
                        }
                    }
                }
            }

            if (((countIp == 2 || countRegion == 2) || (thirdTransaction.equals(request)) && response.getResult() != PROHIBITED)) {
                thirdTransaction = request;
                response.setResult(MANUAL_PROCESSING);
                request.setResult("MANUAL_PROCESSING");
                if (response.getInfo().equals("none")) {
                    response.setInfo("");
                }


                if (countIp == 2 || (thirdTransaction.equals(request) && countIp >= 3)) {
                    if (response.getInfo().equals("")) {
                        response.setInfo("ip-correlation");
                    } else {
                        response.setInfo(response.getInfo() + ", ip-correlation");
                    }
                }
                if (countRegion == 2 || (thirdTransaction.equals(request) && countRegion >= 3)) {

                    if (response.getInfo().equals("")) {
                        response.setInfo("region-correlation");
                    } else {
                        response.setInfo(response.getInfo() + ", region-correlation");
                    }

                }
            }

            if (((countIp >= 3 || countRegion >= 3) && (!thirdTransaction.equals(request) || response.getResult() == PROHIBITED))) { //if it is not third transaction
                response.setResult(PROHIBITED);
                request.setResult("PROHIBITED");

                if (response.getInfo().equals("none")) {
                    response.setInfo("");
                }
                if (countIp >= 3) {
                    if (response.getInfo().equals("")) {
                        response.setInfo("ip-correlation");
                    } else {
                        response.setInfo(response.getInfo() + ", ip-correlation");
                    }
                }
                if (countRegion >= 3) {
                    if (response.getInfo().equals("")) {
                        response.setInfo("region-correlation");
                    } else {
                        response.setInfo(response.getInfo() + ", region-correlation");
                    }
                }
            }

        }
        request.setFeedback("");
        transactionRepository.save(request);

    }

    private int validateAmount(Long amount, String cardNumber) {
        return cardAmountLimitsService.processAmount(amount, cardNumber);
    }

    private Set<Transaction> getAllowTransaction(List<Transaction> transactions) {
        Set<Transaction> allowTransactions = new HashSet<>();
        for (Transaction transaction : transactions) {
            if (isAllow(transaction) && allowTransactions.size() < 2) {
                allowTransactions.add(transaction);
            }
        }
        return allowTransactions;
    }

    private boolean isAllow(Transaction transaction) {
        return transaction.getAmount() <= 200 && transaction.getAmount() > 0;
    }


    private void checkIpAndNumber(Transaction request, TransactionResponse response, String amount) {
        if (stolenCardRepository.existsByNumber(request.getNumber())) {
            response.setResult(PROHIBITED);
            response.setInfo(amount + "card-number");
        }
        if (ipRepository.existsByIp(request.getIp())) {
            response.setResult(PROHIBITED);
            response.setInfo(amount + "ip");
        }
        if (ipRepository.existsByIp(request.getIp()) && stolenCardRepository.existsByNumber(request.getNumber())) {
            response.setResult(PROHIBITED);
            response.setInfo(amount + "card-number, ip");
        }
    }

    public Optional<Transaction> giveFeedbackToTransaction(FeedbackRequest feedbackRequest) {
        String requestFeedback = feedbackRequest.getFeedback().toString();

        Optional<Transaction> optionalTransaction = transactionRepository.findById(feedbackRequest.getTransactionId());

        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            String feedback = transaction.getFeedback();
            String result = transaction.getResult();
            String number = transaction.getNumber();
            Long amount = transaction.getAmount();

            if (!feedback.isEmpty()) {
                throw new HttpConflictException("Feedback already given");
            }

            if (result.equals(requestFeedback)) {
                throw new UnprocessableEntityException("Provided feedback not allowed");
            }

            cardAmountLimitsService.processLimits(number, amount, result, requestFeedback);
            transaction.setFeedback(feedbackRequest.getFeedback().toString());
            transactionRepository.save(transaction);
        }

        return optionalTransaction;
    }

    public List<Transaction> getTransactionHistoryByCardNumber(String number) {

        boolean validCreditCard = validateCreditCard(number);

        if (!validCreditCard) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<Transaction> transactionsByNumber = transactionRepository.findByNumber(number);

        if (transactionsByNumber.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return transactionsByNumber;
    }

    private boolean validateCreditCard(String cardNum) {
        // using Luhn algorithm
        int nDigits = cardNum.length();
        int nSum = 0;
        for (int i = 0; i < nDigits; i++) {
            int d = cardNum.charAt(i) - '0';
            if (i % 2 == 0) {
                d *= 2;
                nSum += d / 10;
                nSum += d % 10;
            } else {
                nSum += d;
            }
        }
        return nSum % 10 == 0;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAll();
    }
}
