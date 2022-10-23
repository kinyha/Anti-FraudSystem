package antifraud.controller;

import antifraud.dto.FeedbackRequest;
import antifraud.dto.IpRequest;
import antifraud.dto.StolenCardRequest;
import antifraud.dto.TransactionResponse;
import antifraud.exceptions.UserNotFoundException;
import antifraud.model.Ip;
import antifraud.model.StolenCard;
import antifraud.model.Transaction;
import antifraud.repository.IpRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.service.IpService;
import antifraud.service.StolenCardService;
import antifraud.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
public class AntifraudController {
    @Autowired
    private IpService ipService;
    @Autowired
    IpRepository ipRepository;
    @Autowired
    private StolenCardService stolenCardService;
    @Autowired
    private StolenCardRepository stolenCardRepository;

    @Autowired
    private TransactionService transactionService;

    public AntifraudController(IpService ipService, IpRepository ipRepository, StolenCardService stolenCardService, StolenCardRepository stolenCardRepository, TransactionService transactionService) {
        this.ipService = ipService;
        this.ipRepository = ipRepository;
        this.stolenCardService = stolenCardService;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionService = transactionService;
    }

    @PostMapping("/transaction")
    @ResponseBody
    public TransactionResponse createTransaction(@RequestBody Transaction transactionRequest) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionService.process(transactionRequest, transactionResponse);
        return transactionResponse;
    }

    @PutMapping("/transaction")
    public Transaction giveFeedbackToTransaction(@RequestBody FeedbackRequest feedbackRequest) {
        return transactionService.giveFeedbackToTransaction(feedbackRequest)
                .orElseThrow(() -> new UserNotFoundException("no transaction with id - "
                        + feedbackRequest.getTransactionId()));
    }

    @GetMapping("/history")
    public List<Transaction> showAllTransactions() {
        return transactionService.getTransactionHistory();
    }

    @GetMapping("/history/{number}")
    public List<Transaction> showAllTransactionsByCardNumber(@PathVariable String number) {
        return transactionService.getTransactionHistoryByCardNumber(number);
    }

    @PostMapping("/suspicious-ip")
    public Ip suspiciousIp(@RequestBody IpRequest ipRequest) {
        Ip ipResponse = new Ip();
        ipService.process(ipRequest,ipResponse);
        return ipResponse;
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public String deleteSuspiciousIp(@PathVariable String ip) {
        ipService.delete(ip);
        return "{\n" +
                "   \"status\": \"IP "+ ip + " successfully removed!\"\n" +
                "}";
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<Ip>> getSuspiciousIp() {
        if (ipRepository.count() == 0) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return new ResponseEntity<>(ipRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), HttpStatus.OK);
    }

    @PostMapping("/stolencard")
    public StolenCard stolenCard(@RequestBody StolenCardRequest stolenCardRequest) {
        StolenCard stolenCardResponse = new StolenCard();
        stolenCardService.process(stolenCardRequest,stolenCardResponse);
        return stolenCardResponse;
    }

    @DeleteMapping("/stolencard/{number}")
    public String deleteStolenCard(@PathVariable String number) {
        stolenCardService.delete(number);
        return "{\n" +
                "   \"status\": \"Card "+ number + " successfully removed!\"\n" +
                "}";
    }

    @GetMapping("/stolencard")
    public ResponseEntity<List<StolenCard>> getStolenCard() {
        if (stolenCardRepository.count() == 0) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return new ResponseEntity<>(stolenCardRepository.findAll(Sort.by(Sort.Direction.ASC, "id")), HttpStatus.OK);
    }
}
