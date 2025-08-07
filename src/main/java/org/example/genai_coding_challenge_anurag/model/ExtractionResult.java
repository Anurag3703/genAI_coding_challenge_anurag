package org.example.genai_coding_challenge_anurag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractionResult {
    private ExtractionFields extractionFields;
    private ValidationResult validationResult;

}
