package com.hireforge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireforge.dto.CareerAiRequest;
import com.hireforge.dto.CareerAiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerAiServiceTest {
    @Mock
    private GroqClient groqClient;

    private CareerAiService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new CareerAiService(groqClient, objectMapper);
    }

    @Test
    void convertsMockedResumeAnalysisToTypedResponseAndRequestsEvidenceBasedReview() throws Exception {
        when(groqClient.completeJson(anyString(), anyString(), eq("resume_analysis"), anyMap()))
                .thenReturn(objectMapper.readTree("""
                        {"overallScore":84,"summary":"Clear engineering background.","strengths":["Java projects"],"weaknesses":["Few outcomes"],"missingSkills":["Kubernetes not evidenced"],"atsSuggestions":["Use consistent headings"],"actionableImprovements":["Add verified impact"]}
                        """));

        CareerAiResponse.ResumeAnalysis result = service.analyzeResume("Java projects", "Backend Engineer");

        assertThat(result.overallScore()).isEqualTo(84);
        assertThat(result.strengths()).containsExactly("Java projects");
        assertThat(result.missingSkills()).containsExactly("Kubernetes not evidenced");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        verify(groqClient).completeJson(systemPrompt.capture(), anyString(), eq("resume_analysis"), anyMap());
        assertThat(systemPrompt.getValue()).contains("Never invent or imply qualifications");
    }

    @Test
    void mapsMockedJobMatchAndInterviewResponses() throws Exception {
        when(groqClient.completeJson(anyString(), anyString(), eq("job_match"), anyMap()))
                .thenReturn(objectMapper.readTree("""
                        {"matchScore":71,"summary":"Some role overlap.","matchingSkills":[{"skill":"Java","resumeEvidence":"Built Java services","jobRequirement":"Java experience"}],"missingKeywords":["Kubernetes"],"experienceAlignment":"Backend work is relevant.","recommendations":["Add only verified deployment experience."]}
                        """));
        when(groqClient.completeJson(anyString(), anyString(), eq("interview_questions"), anyMap()))
                .thenReturn(objectMapper.readTree("""
                        {"questions":[{"id":"q1","category":"Behavioral","question":"Tell me about a project.","whatItAssesses":"Communication"},{"id":"q2","category":"Technical","question":"How do you debug?","whatItAssesses":"Reasoning"},{"id":"q3","category":"Technical","question":"How do you test?","whatItAssesses":"Quality"},{"id":"q4","category":"Behavioral","question":"How do you handle conflict?","whatItAssesses":"Collaboration"},{"id":"q5","category":"Role","question":"What would you prioritize?","whatItAssesses":"Judgment"},{"id":"q6","category":"Behavioral","question":"What would you learn?","whatItAssesses":"Growth"}],"preparationNote":"Use concrete examples and verified outcomes."}
                        """));
        when(groqClient.completeJson(anyString(), anyString(), eq("answer_evaluation"), anyMap()))
                .thenReturn(objectMapper.readTree("""
                        {"score":78,"overallFeedback":"Good example; clarify your own contribution.","strengths":["Relevant example"],"improvementAreas":["Add a verified result"],"sampleAnswer":"I built the service and [add a verified result]."}
                        """));

        CareerAiResponse.JobMatch match = service.matchJob(new CareerAiRequest.JobMatch("Java services", "Java and Kubernetes"));
        CareerAiResponse.InterviewSet questions = service.prepareInterview(new CareerAiRequest.InterviewPrep("Backend Engineer", "Acme", "Java services"));
        CareerAiResponse.AnswerEvaluation evaluation = service.evaluateAnswer(new CareerAiRequest.InterviewAnswer("Backend Engineer", "Describe a project", "I built a service", "Acme"));

        assertThat(match.matchScore()).isEqualTo(71);
        assertThat(match.matchingSkills().getFirst().skill()).isEqualTo("Java");
        assertThat(questions.questions()).hasSize(6);
        assertThat(evaluation.sampleAnswer()).contains("[add a verified result]");
    }
}
