package com.hireforge.dto;

import java.util.List;

public final class CareerAiResponse {
    private CareerAiResponse() {}

    public record ResumeAnalysis(
            int overallScore,
            String summary,
            List<String> strengths,
            List<String> weaknesses,
            List<String> missingSkills,
            List<String> atsSuggestions,
            List<String> actionableImprovements
    ) {}

    public record SkillMatch(String skill, String resumeEvidence, String jobRequirement) {}

    public record JobMatch(
            int matchScore,
            String summary,
            List<SkillMatch> matchingSkills,
            List<String> missingKeywords,
            String experienceAlignment,
            List<String> recommendations
    ) {}

    public record InterviewQuestion(String id, String category, String question, String whatItAssesses) {}

    public record InterviewSet(List<InterviewQuestion> questions, String preparationNote) {}

    public record AnswerEvaluation(
            int score,
            String overallFeedback,
            List<String> strengths,
            List<String> improvementAreas,
            String sampleAnswer
    ) {}
}
