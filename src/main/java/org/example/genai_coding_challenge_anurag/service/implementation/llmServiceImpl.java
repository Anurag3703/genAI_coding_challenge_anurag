package org.example.genai_coding_challenge_anurag.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.genai_coding_challenge_anurag.model.ExtractionFields;
import org.example.genai_coding_challenge_anurag.service.llmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service

public class llmServiceImpl implements llmService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    //Why used GROQ - I am utilizing the GROQ api for my portfolio project "Alfred AI" my personal buttler a RAG based AI agent that know everything about my life .

    @Value("${groq.api.key}")
    private String groqApiKey ;
    @Value("${groq.model}")
    private  String groqModel ; // llama3-8b-8192 smaller model
    @Value("${grok.url}")
    private  String groqUrl ;
    private static final Logger log = LoggerFactory.getLogger(llmServiceImpl.class);

    @Autowired
    public llmServiceImpl(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }



    @Override
    public ExtractionFields getExtractionFields(String resumeText) {

        try{

            if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
                log.error("groqApiKey is null or empty");
                return getFallbackExtractionFields();
            }

            String prompt = buildPrompt(resumeText);
            log.info("Prompt Created and ready to be sent");

            // Used GROK API Documentation
            Map<String ,Object> requestBody = Map.of(
                    "model",groqModel,
                    "messages", List.of(
                            Map.of("role","system","content","You extract structured fields from resumes"),
                            Map.of("role","user","content",prompt)

                    ),
                    "temperature",0.2 // As we are dealing professionally I kept the temperature less
            );

            log.info("Request body created and sending request to groq model : {}",groqModel);


            //Usually Before Used RestTemplate But they are Quite slow and old-fashioned , Learned web-client
            // Json node is part of Jackson's com.fasterxml.jackson.databind package.
            // It helps me to represent Json structure irrespective of the type.
            JsonNode response = webClient.post()
                    .uri(groqUrl)
                    .header("Authorization","Bearer " + groqApiKey)
                    .header("Content-Type","application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)  // Two things (FLUX and MONO) mono if one json object , FLUX when there is a list
                    .block(); //Made it syn as we are parsing one doc at a time

            if(response == null){
                log.error("response is null");
                return getFallbackExtractionFields();
            }
            log.info("API response received");

            if(response.has("error")){
                log.error("API response {}", response.get("error"));
                return getFallbackExtractionFields();
            }


            //GROK Documentation
            JsonNode choices = response.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("choices is null");
                return getFallbackExtractionFields();
            }

            String content = choices.get(0)
                    .get("message")
                    .get("content")
                    .asText();

            log.info("LLM response content: {}", content);

            // Parse the raw JSON string 'content' into a JsonNode object for structured
            JsonNode node = objectMapper.readTree(content);
            log.info("LLM response node: {}", node);

            if (node == null || !node.has("work_experience")) {
                log.error("Parsing failed or missing required fields. Content: {}", content);
                return getFallbackExtractionFields();
            }

            return mapToExtractionFields(node);

        } catch (JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            return getFallbackExtractionFields();
        }catch (WebClientResponseException e) {
            log.error("HTTP Error calling Groq API: Status={}, Body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return getFallbackExtractionFields();
        }catch (Exception e) {
            log.error("Unexpected error during field extraction: {}", e.getMessage(), e);
            return getFallbackExtractionFields();
        }

    }


    // Fall back for unwanted errors
    @Override
    public ExtractionFields getFallbackExtractionFields() {
        ExtractionFields fallback = new ExtractionFields();
        fallback.setWorkExperience("0");
        fallback.setSkills(List.of());
        fallback.setLanguages(List.of());
        fallback.setProfile("Unable to extract profile information");
        return fallback;
    }


    // Prompt – After reading the question, I tried to resolve the issue and explain as much as I could.
    // One challenge I encountered was with 'workExperience' as it was being detected inconsistently.
    public String buildPrompt(String resumeText) {
        return """
You are a professional resume parser. Extract the following fields from the CV text and return ONLY a valid JSON object.
DO NOT include any explanation, introductory text, or markdown formatting.

CRITICAL INSTRUCTIONS:

1. WORK_EXPERIENCE:
   - Calculate total years of PROFESSIONAL WORK EXPERIENCE ONLY from employment history
   - IGNORE education dates, academic periods, and degree durations
   - Only count positions in "Work Experience", "Employment", "Professional Experience" sections
   - For date ranges like "2023-2024", calculate the difference (e.g., 1 year)
   - For ongoing positions "2024-Present" or "2024-Current", calculate from start year to 2025
   - For positions less than 1 year, round to nearest year (6+ months = 1 year, <6 months = 0 years)
   - Include internships, part-time work, and contract work ONLY if in work experience sections
   - Sum all work experiences together
   - DO NOT count education, university, college, or academic periods
   - If the CV explicitly states experience duration (e.g., "one year of experience"), use that number
   - Return ONLY the total number as a string (e.g., "0", "1", "2", "5")
   - If no work experience found, return "0"

2. SKILLS:
   - Extract ALL technical skills, programming languages, frameworks, tools
   - Include soft skills if explicitly mentioned
   - Look for sections like "Skills", "Technical Skills", "Competencies"
   - Normalize skill names (e.g., "JavaScript" not "js", "Java" not "JAVA")
   - Return as array of strings: ["skill1", "skill2", ...]
   - If no skills found, return empty array: []

3. LANGUAGES:
   - Extract ONLY the language names without proficiency levels
   - Remove any proficiency indicators like (Fluent), (Basic), (Native), (Intermediate), etc.
   - Return only the clean language names
   - Look in dedicated language sections or within text
   - Return as array of strings: ["language1", "language2", ...]
   - If no languages found, return empty array: []

4. PROFILE:
   - Extract or create a comprehensive professional summary
   - Include career objectives, interests, and aspirations
   - Look for sections like "Summary", "Objective", "About", "Profile"
   - If no dedicated section exists, synthesize from the overall resume content
   - Focus on professional goals, interests in technologies, career direction
   - Should be 2-4 sentences capturing the person's professional identity
   - If insufficient information, return brief summary based on available data

RESPONSE FORMAT:
Return EXACTLY this JSON structure with no additional text:

{
  "work_experience": "number_only",
  "skills": ["skill1", "skill2"],
  "languages": ["language1", "language2"],
  "profile": "professional summary text"
}

EXAMPLES:
- Employment: "Marketing Manager at Kyembura Studio (2024-2025)" → work_experience: "1"
- Education: "University (2021-2024)" → DO NOT COUNT THIS
- Skills mentioned: "Project Management, Teamwork, LLM" → skills: ["Project Management", "Teamwork", "LLM"]
- Languages: "English (Fluent), French (Fluent), Hungarian (Basics)" → languages: ["English", "French", "Hungarian"]

CV Text:
%s
""".formatted(resumeText);
    }

    //Helper method for extracting required fields from JSON Node
    private ExtractionFields mapToExtractionFields(JsonNode node) {
        ExtractionFields extractionFields = new ExtractionFields();

        try {
            // Work experience with better null checking
            JsonNode workExpNode = node.get("work_experience");
            extractionFields.setWorkExperience(
                    workExpNode != null && !workExpNode.isNull() ? workExpNode.asText() : "0"
            );

            // Skills with proper null and empty array handling
            JsonNode skillsNode = node.get("skills");
            if (skillsNode != null && !skillsNode.isNull() && skillsNode.isArray() && !skillsNode.isEmpty()) {
                extractionFields.setSkills(objectMapper.convertValue(skillsNode, List.class));
            } else {
                extractionFields.setSkills(List.of()); // Empty list instead of null
            }

            // Languages with proper null and empty array handling
            JsonNode languagesNode = node.get("languages");
            if (languagesNode != null && !languagesNode.isNull() && languagesNode.isArray() && !languagesNode.isEmpty()) {
                extractionFields.setLanguages(objectMapper.convertValue(languagesNode, List.class));
            } else {
                extractionFields.setLanguages(List.of()); // Empty list instead of null
            }

            // Profile with better null checking
            JsonNode profileNode = node.get("profile");
            extractionFields.setProfile(
                    profileNode != null && !profileNode.isNull() ? profileNode.asText() : "No profile information available"
            );

            return extractionFields;
        } catch (Exception e) {
            log.error("Error mapping fields: {}", e.getMessage());
            return getFallbackExtractionFields();
        }
    }
}
