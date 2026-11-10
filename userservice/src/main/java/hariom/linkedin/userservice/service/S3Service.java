package hariom.linkedin.userservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

    public String uploadFile(MultipartFile file, String path) {
        return "https://aws.storage/" + path + "/photo.png";
    }
}
