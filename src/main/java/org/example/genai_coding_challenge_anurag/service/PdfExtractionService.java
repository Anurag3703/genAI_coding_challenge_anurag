package org.example.genai_coding_challenge_anurag.service;

import java.io.InputStream;

public interface PdfExtractionService {

    public String extractTextFromPDF(InputStream stream) throws Exception;
    public String extractTextFromFilePath(String filePath) throws Exception;
}
