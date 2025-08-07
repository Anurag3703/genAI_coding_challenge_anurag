package org.example.genai_coding_challenge_anurag.service.implementation;

import org.example.genai_coding_challenge_anurag.model.ExtractionFields;
import org.example.genai_coding_challenge_anurag.model.ValidationResult;
import org.example.genai_coding_challenge_anurag.service.ValidatorService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


@Service
public class ValidationServiceImpl implements ValidatorService {

    private boolean isExperienceValid(String experience) {
        if(experience == null || experience.isEmpty()) {
            return false;
        }
        try{
            double years = Double.parseDouble(experience);
            return years >= 0 && years <= 2;
        }catch(NumberFormatException e){
            return false;
        }
    }

    private boolean hasRequiredSkills(List<String> skills) {
        if(skills == null || skills.isEmpty()) {
            return false;
        }
        return new HashSet<>(skills.stream()
                .filter(skill -> skill != null)
                .map(String::toLowerCase)
                .toList())
                .containsAll(List.of("java", "llm"));


    }

    private boolean hasRequiredLanguages(List<String> languages) {
        if(languages == null || languages.isEmpty()) {
            return false;
        }

        return new HashSet<>(languages.stream()
                .filter(lang -> lang !=null)
                .map(String::toLowerCase)
                .toList())
                .containsAll(List.of("english", "hungarian"));
    }

    private boolean isProfileValid(String profile) {
        if (profile == null) {
            return false;
        }
        String lowerProfile = profile.toLowerCase();
        return lowerProfile.contains("genai") ||
                lowerProfile.contains("java") ||
                lowerProfile.contains("generative ai") ||
                lowerProfile.contains("gen ai");
    }


    @Override
    public ValidationResult validate(ExtractionFields extractionFields) {
        if (extractionFields == null) {
            return new ValidationResult(false,false,false,false);

        }

        boolean expValid = isExperienceValid(extractionFields.getWorkExperience());
        boolean skillsValid = hasRequiredSkills(extractionFields.getSkills());
        boolean languagesValid = hasRequiredLanguages(extractionFields.getLanguages());
        boolean profileValid = isProfileValid(extractionFields.getProfile());

        return new ValidationResult(expValid,skillsValid,languagesValid,profileValid);

    }
}
