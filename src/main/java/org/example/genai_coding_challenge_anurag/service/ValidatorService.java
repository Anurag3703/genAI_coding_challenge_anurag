package org.example.genai_coding_challenge_anurag.service;

import org.example.genai_coding_challenge_anurag.model.ExtractionFields;
import org.example.genai_coding_challenge_anurag.model.ValidationResult;

public interface ValidatorService {
    public ValidationResult validate(ExtractionFields extractionFields);

}
