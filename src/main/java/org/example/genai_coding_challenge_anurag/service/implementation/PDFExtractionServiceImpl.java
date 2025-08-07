package org.example.genai_coding_challenge_anurag.service.implementation;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.genai_coding_challenge_anurag.service.PdfExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PDFExtractionServiceImpl implements PdfExtractionService {
    private static final Logger log = LoggerFactory.getLogger(PDFExtractionServiceImpl.class);

    //Used Apache PDF box for extraction of text from pdf
    @Override
    public String extractTextFromPDF(InputStream stream) throws IOException {
        try(PDDocument document = Loader.loadPDF(stream.readAllBytes())){
            PDFTextStripper stripper = new PDFTextStripper();
            System.out.println(stripper.getText(document));
            return stripper.getText(document);
        }catch (IOException e){
            throw new IOException("Failed to extract text from PDF", e);
        }
    }


    //Can test local file in /samples folder (Instruction - Copy the absolute path)
    @Override
    public String extractTextFromFilePath(String filePath) throws Exception {
        if(filePath == null || filePath.isEmpty()){
            log.error("File path - {} is null or empty ", filePath);
            throw new Exception("File path cannot be null or empty");
        }

        try{
                File file = new File(filePath);
                log.info("File loaded from path - {}",file.getAbsolutePath());
                try(PDDocument document = Loader.loadPDF(file)){
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    System.out.println(text);
                    log.info("Data Extracted from the file - {}",file.getAbsolutePath());
                    return text;
                }catch (IOException e){
                    throw new IOException("Failed to extract text from PDF ", e);
                }

        }catch (IOException e){
            throw new IOException("Failed to extract text ", e);
        }

    }
}
