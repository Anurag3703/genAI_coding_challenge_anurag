package org.example.genai_coding_challenge_anurag.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractionFields {

    private String workExperience;
    private List<String> skills;
    private List<String> languages;
    private String profile;

}
