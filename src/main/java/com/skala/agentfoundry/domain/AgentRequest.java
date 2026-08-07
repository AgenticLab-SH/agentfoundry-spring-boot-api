package com.skala.agentfoundry.domain;

import com.skala.agentfoundry.dto.AgentRequestRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agent_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 500)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "desired_artifact_type", nullable = false, length = 30)
    private ArtifactType desiredArtifactType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgentDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionEnvironment environment;

    @Column(name = "available_memory_gb", nullable = false)
    private Integer availableMemoryGb;

    @Column(name = "max_credits", nullable = false)
    private Integer maxCredits;

    @Column(name = "expected_hours", nullable = false)
    private Integer expectedHours;

    @Column(nullable = false, length = 500)
    private String constraints;

    @Column(name = "acceptance_criteria", nullable = false, length = 500)
    private String acceptanceCriteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private AgentRequest(Member requester, AgentRequestRequest request, LocalDateTime createdAt) {
        this.requester = requester;
        apply(request);
        this.status = RequestStatus.OPEN;
        this.createdAt = createdAt;
    }

    public static AgentRequest create(
        Member requester,
        AgentRequestRequest request,
        LocalDateTime createdAt
    ) {
        return new AgentRequest(requester, request, createdAt);
    }

    public void update(AgentRequestRequest request) {
        if (status != RequestStatus.OPEN) {
            throw new IllegalStateException("Only open requests can be updated");
        }
        apply(request);
    }

    private void apply(AgentRequestRequest request) {
        this.title = request.title();
        this.goal = request.goal();
        this.requestType = request.requestType();
        this.desiredArtifactType = request.desiredArtifactType();
        this.domain = request.domain();
        this.environment = request.environment();
        this.availableMemoryGb = request.availableMemoryGb();
        this.maxCredits = request.maxCredits();
        this.expectedHours = request.expectedHours();
        this.constraints = request.constraints();
        this.acceptanceCriteria = request.acceptanceCriteria();
    }

    public void markMatched() {
        if (status != RequestStatus.OPEN) {
            throw new IllegalStateException("Request is not open");
        }
        status = RequestStatus.MATCHED;
    }

    public void reopen() {
        if (status != RequestStatus.MATCHED) {
            throw new IllegalStateException("Request is not matched");
        }
        status = RequestStatus.OPEN;
    }

    public void close() {
        if (status != RequestStatus.MATCHED) {
            throw new IllegalStateException("Request is not matched");
        }
        status = RequestStatus.CLOSED;
    }

    public void cancel() {
        if (status != RequestStatus.OPEN) {
            throw new IllegalStateException("Only open requests can be canceled");
        }
        status = RequestStatus.CANCELED;
    }
}

