package org.example.genai_coding_challenge_anurag.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class FrontendController {

    @GetMapping(value = "/")
    public ResponseEntity<String> home()  throws IOException {
        try{
            ClassPathResource classPathResource = new ClassPathResource("static/index.html");
            String content =new String(classPathResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);

        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
