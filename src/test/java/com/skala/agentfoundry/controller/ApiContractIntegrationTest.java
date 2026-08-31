package com.skala.agentfoundry.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiContractIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("시드 Agent Offering 8개를 바로 조회할 수 있다")
    void seededOfferingsAreAvailable() throws Exception {
        mockMvc.perform(get("/api/offerings").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(8))
            .andExpect(jsonPath("$.data.content", hasSize(8)));
    }

    @Test
    @DisplayName("유형과 도메인 필터가 목록에 적용된다")
    void offeringFiltersWork() throws Exception {
        mockMvc.perform(get("/api/offerings")
                .param("listingType", "BUILD_SERVICE")
                .param("domain", "DEVELOPMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].id").value(101));
    }

    @Test
    @DisplayName("Offering 등록·조회·수정·보관 API가 이어서 동작한다")
    void offeringCrudJourneyWorks() throws Exception {
        MockHttpSession session = login("agent_maker", "demo1234");
        MvcResult created = mockMvc.perform(post("/api/offerings")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"테스트 Harness 구축 서비스",
                      "summary":"로컬 개발 환경에 맞춘 Agent Harness를 함께 설계합니다.",
                      "listingType":"BUILD_SERVICE",
                      "artifactType":"HARNESS",
                      "domain":"DEVELOPMENT",
                      "environment":"LOCAL",
                      "minimumMemoryGb":8,
                      "estimatedHours":6,
                      "creditCost":4,
                      "capacity":2,
                      "acceptanceCriteria":"실행 가능한 예제와 점검 목록을 함께 제공합니다.",
                      "licenseName":"Demo License"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("OPEN"))
            .andExpect(jsonPath("$.data.availableSlots").value(2))
            .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(
            created.getResponse().getContentAsString(), "$.data.id").toString();

        mockMvc.perform(get("/api/offerings/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("테스트 Harness 구축 서비스"));

        mockMvc.perform(put("/api/offerings/{id}", id)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"수정된 Harness 구축 서비스",
                      "summary":"수정된 조건으로 로컬 Agent Harness를 함께 설계합니다.",
                      "listingType":"BUILD_SERVICE",
                      "artifactType":"HARNESS",
                      "domain":"DEVELOPMENT",
                      "environment":"LOCAL",
                      "minimumMemoryGb":16,
                      "estimatedHours":8,
                      "creditCost":5,
                      "capacity":3,
                      "acceptanceCriteria":"수정된 실행 예제와 점검 목록을 함께 제공합니다.",
                      "licenseName":"Demo License"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("수정된 Harness 구축 서비스"))
            .andExpect(jsonPath("$.data.capacity").value(3));

        mockMvc.perform(delete("/api/offerings/{id}", id).session(session))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/offerings/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("Request 등록·조회·수정·취소 API가 이어서 동작한다")
    void requestCrudJourneyWorks() throws Exception {
        MockHttpSession session = login("demo_requester", "demo1234");
        MvcResult created = mockMvc.perform(post("/api/requests")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"Agent Harness 검토 요청",
                      "goal":"로컬 환경에서 반복 실행 가능한 Harness 구조를 검토받습니다.",
                      "requestType":"REVIEW_PROJECT",
                      "desiredArtifactType":"HARNESS",
                      "domain":"DEVELOPMENT",
                      "environment":"LOCAL",
                      "availableMemoryGb":16,
                      "maxCredits":5,
                      "expectedHours":6,
                      "constraints":"외부 API 없이 동작해야 합니다.",
                      "acceptanceCriteria":"실행 절차와 개선 항목을 문서로 확인합니다."
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("OPEN"))
            .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(
            created.getResponse().getContentAsString(), "$.data.id").toString();

        mockMvc.perform(get("/api/requests/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Agent Harness 검토 요청"));

        mockMvc.perform(put("/api/requests/{id}", id)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"수정된 Agent Harness 검토 요청",
                      "goal":"로컬 환경의 Harness 구조와 실행 절차를 함께 검토받습니다.",
                      "requestType":"REVIEW_PROJECT",
                      "desiredArtifactType":"HARNESS",
                      "domain":"DEVELOPMENT",
                      "environment":"LOCAL",
                      "availableMemoryGb":32,
                      "maxCredits":6,
                      "expectedHours":8,
                      "constraints":"외부 API와 파일 업로드 없이 동작해야 합니다.",
                      "acceptanceCriteria":"실행 절차와 우선순위가 있는 개선 항목을 확인합니다."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("수정된 Agent Harness 검토 요청"))
            .andExpect(jsonPath("$.data.availableMemoryGb").value(32));

        mockMvc.perform(delete("/api/requests/{id}", id).session(session))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/requests/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    @DisplayName("다른 회원은 Offering을 보관하거나 Request를 취소할 수 없다")
    void nonOwnersCannotArchiveOfferingOrCancelRequest() throws Exception {
        MockHttpSession requesterSession = login("demo_requester", "demo1234");

        mockMvc.perform(delete("/api/offerings/101").session(requesterSession))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mockMvc.perform(get("/api/offerings/101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("OPEN"));

        MockHttpSession providerSession = login("agent_maker", "demo1234");

        mockMvc.perform(delete("/api/requests/201").session(providerSession))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mockMvc.perform(get("/api/requests/201"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("로그인하지 않은 참여 생성은 401이다")
    void engagementRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/engagements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestId\":201,\"offeringId\":101}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("NOT_AUTHENTICATED"));
    }

    @Test
    @DisplayName("요청별 매칭은 점수와 항목별 근거를 반환한다")
    void matchingReturnsExplainableScore() throws Exception {
        MockHttpSession session = login("demo_requester", "demo1234");

        mockMvc.perform(get("/api/requests/201/matches").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].offering.id").value(101))
            .andExpect(jsonPath("$.data[0].compatibilityScore").value(99))
            .andExpect(jsonPath("$.data[0].scoreDetails.typeAndArtifact").value(25))
            .andExpect(jsonPath("$.data[0].scoreDetails.resource").value(20));
    }

    @Test
    @DisplayName("로그인 후 참여 생성과 취소 API가 이어서 동작한다")
    void createAndCancelJourneyWorks() throws Exception {
        MockHttpSession session = login("demo_requester", "demo1234");
        MvcResult created = mockMvc.perform(post("/api/engagements")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestId\":201,\"offeringId\":101}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.id").toString();

        mockMvc.perform(post("/api/engagements/{id}/cancel", id).session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    @DisplayName("잘못된 Offering 입력은 필드 오류를 포함한 400이다")
    void validationReturnsFieldErrors() throws Exception {
        MockHttpSession session = login("agent_maker", "demo1234");

        mockMvc.perform(post("/api/offerings")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"summary\":\"짧음\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.fieldErrors.length()").value(org.hamcrest.Matchers.greaterThan(5)));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405 계약으로 반환한다")
    void unsupportedMethodReturns405() throws Exception {
        mockMvc.perform(patch("/api/offerings/101"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("로그인 응답은 비밀번호를 노출하지 않는다")
    void loginDoesNotExposePassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"demo_requester\",\"password\":\"demo1234\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.password").doesNotExist())
            .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                .doesNotContain("demo1234"));
    }

    @Test
    @DisplayName("로그아웃하면 현재 회원 조회가 401로 바뀐다")
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = login("demo_requester", "demo1234");

        mockMvc.perform(post("/api/auth/logout").session(session))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("NOT_AUTHENTICATED"));
    }

    @Test
    @DisplayName("없는 Offering은 일관된 404를 반환한다")
    void missingOfferingReturns404() throws Exception {
        mockMvc.perform(get("/api/offerings/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("DATA_NOT_FOUND"));
    }

    @Test
    @DisplayName("OpenAPI 문서와 정적 첫 화면이 응답한다")
    void documentationAndWebAreAvailable() throws Exception {
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paths['/api/offerings'].get.parameters[?(@.name == 'page')]").exists())
            .andExpect(jsonPath("$.paths['/api/offerings'].get.parameters[?(@.name == 'pageable')]").isEmpty())
            .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                .contains("/api/engagements"));
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));
    }

    @Test
    @DisplayName("Actuator health는 필요한 최소 상태만 공개한다")
    void actuatorHealthIsAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components").doesNotExist());
    }

    private MockHttpSession login(String memberId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"" + memberId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        HttpSession session = result.getRequest().getSession(false);
        return (MockHttpSession) session;
    }
}
