package org.example.genai_coding_challenge_anurag.service;

import org.example.genai_coding_challenge_anurag.model.ExtractionFields;

public interface llmService {
    public ExtractionFields getExtractionFields(String resumeText);
    public ExtractionFields getFallbackExtractionFields();
}
