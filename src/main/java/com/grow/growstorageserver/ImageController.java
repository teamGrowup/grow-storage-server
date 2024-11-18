package com.grow.growstorageserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@RestController
@RequestMapping("/images")
public class ImageController {

    private final String uploadDir = System.getProperty("user.dir") + "/images/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("File uploaded: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir + fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String ext = fileName.substring(fileName.lastIndexOf(".")+1);
                log.info("filename={}, ext={}", fileName, ext);
                switch (ext){
                    case "jpg", "jpeg":
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(resource);
                    case "png":
                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_PNG)
                                .body(resource);
                    default:
                        return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
