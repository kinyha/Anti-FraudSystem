package antifraud.service;

import antifraud.repository.IpRepository;
import antifraud.dto.IpRequest;
import antifraud.model.Ip;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IpService {
    private final IpRepository ipRepository;

    public IpService(IpRepository ipRepository) {
        this.ipRepository = ipRepository;
    }

    public void process(IpRequest request, Ip ipResponse) {
        if (checkIp(request.getIp())) {
            if (!ipRepository.existsByIp(request.getIp())) {
                ipResponse.setIp(request.getIp());
                ipRepository.save(ipResponse);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
                ipResponse.setIp(request.getIp());
                ipRepository.save(ipResponse);
        }
    }



    boolean checkIp(String ip) { //check full
        if (checkCorrect(ip)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    static boolean checkCorrect(String ip) {
        if (ip.split("\\.").length != 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        int[] ipArray = new int[4];
        for (int i = 0; i < ipArray.length; i++) {
            ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
        }

        for (int ipCheck : ipArray) {
            if (ipCheck < 0 || ipCheck > 255) {
                return false;
            }
        }
        return true;
    }

    public void delete(String ip) {
        IpRequest request = new IpRequest();
        request.setIp(ip);
        if (!checkIp(request.getIp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (ipRepository.existsByIp(ip)) {
            ipRepository.delete(ipRepository.findByIp(ip));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
