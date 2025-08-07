package org.example.genai_coding_challenge_anurag.controller;


import org.example.genai_coding_challenge_anurag.model.ExtractionFields;
import org.example.genai_coding_challenge_anurag.model.ExtractionResult;
import org.example.genai_coding_challenge_anurag.model.ValidationResult;
import org.example.genai_coding_challenge_anurag.service.implementation.PDFExtractionServiceImpl;
import org.example.genai_coding_challenge_anurag.service.implementation.ValidationServiceImpl;
import org.example.genai_coding_challenge_anurag.service.implementation.llmServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class UploadController {
    private final llmServiceImpl llmServiceImpl;
    private final PDFExtractionServiceImpl pdfExtractionServiceImpl;
    private final ValidationServiceImpl validationServiceImpl;

    @Autowired
    public UploadController(llmServiceImpl llmServiceImpl, PDFExtractionServiceImpl pdfExtractionServiceImpl,ValidationServiceImpl validationServiceImpl) {
            this.llmServiceImpl = llmServiceImpl;
            this.pdfExtractionServiceImpl = pdfExtractionServiceImpl;
            this.validationServiceImpl = validationServiceImpl;
    }

    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPDF(@RequestParam("file") MultipartFile file) {
        try{
            String resumeText = pdfExtractionServiceImpl.extractTextFromPDF(file.getInputStream());
            ExtractionFields extractionFields = llmServiceImpl.getExtractionFields(resumeText);
            ValidationResult validationResult = validationServiceImpl.validate(extractionFields);

            return ResponseEntity.ok().body(new ExtractionResult(extractionFields, validationResult));
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error " +e.getMessage());
        }
    }

    @PostMapping(value = "/filepath")
    public ResponseEntity<?> uploadFile(@RequestBody String filePath) throws IOException {
        try{
            String cleanPath = filePath.trim();
            if (cleanPath.startsWith("\"") && cleanPath.endsWith("\"")) {
                cleanPath = cleanPath.substring(1, cleanPath.length() - 1);
            }

            String decodedPath = URLDecoder.decode(cleanPath, StandardCharsets.UTF_8.toString());
            String resumeText = pdfExtractionServiceImpl.extractTextFromFilePath(decodedPath);
            ExtractionFields extractionFields = llmServiceImpl.getExtractionFields(resumeText);
            ValidationResult validationResult = validationServiceImpl.validate(extractionFields);
            return ResponseEntity.ok().body(new ExtractionResult(extractionFields, validationResult));
        } catch (IOException e) {
            throw new IOException(e);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error " +e.getMessage());
        }

    }
}
