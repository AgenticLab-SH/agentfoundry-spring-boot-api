package com.skala.agentfoundry.domain;

import com.skala.agentfoundry.dto.AgentOfferingRequest;
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
@Table(name = "agent_offerings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Member provider;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false, length = 30)
    private ListingType listingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false, length = 30)
    private ArtifactType artifactType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgentDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionEnvironment environment;

    @Column(name = "minimum_memory_gb", nullable = false)
    private Integer minimumMemoryGb;

    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours;

    @Column(name = "credit_cost", nullable = false)
    private Integer creditCost;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "available_slots", nullable = false)
    private Integer availableSlots;

    @Column(name = "acceptance_criteria", nullable = false, length = 500)
    private String acceptanceCriteria;

    @Column(name = "license_name", nullable = false, length = 60)
    private String licenseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferingStatus status;

    @Column(name = "completed_count", nullable = false)
    private Integer completedCount;

    @Column(name = "recommendation_count", nullable = false)
    private Integer recommendationCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private AgentOffering(Member provider, AgentOfferingRequest request, LocalDateTime createdAt) {
        this.provider = provider;
        apply(request);
        this.availableSlots = request.capacity();
        this.status = OfferingStatus.OPEN;
        this.completedCount = 0;
        this.recommendationCount = 0;
        this.createdAt = createdAt;
    }

    public static AgentOffering create(
        Member provider,
        AgentOfferingRequest request,
        LocalDateTime createdAt
    ) {
        return new AgentOffering(provider, request, createdAt);
    }

    public void update(AgentOfferingRequest request) {
        int occupiedSlots = capacity - availableSlots;
        if (request.capacity() < occupiedSlots) {
            throw new IllegalArgumentException("Capacity cannot be lower than occupied slots");
        }
        apply(request);
        this.availableSlots = request.capacity() - occupiedSlots;
        syncStatus();
    }

    private void apply(AgentOfferingRequest request) {
        this.title = request.title();
        this.summary = request.summary();
        this.listingType = request.listingType();
        this.artifactType = request.artifactType();
        this.domain = request.domain();
        this.environment = request.environment();
        this.minimumMemoryGb = request.minimumMemoryGb();
        this.estimatedHours = request.estimatedHours();
        this.creditCost = request.creditCost();
        this.capacity = request.capacity();
        this.acceptanceCriteria = request.acceptanceCriteria();
        this.licenseName = request.licenseName();
    }

    public boolean hasAvailableSlot() {
        return status == OfferingStatus.OPEN && availableSlots > 0;
    }

    public void reserveSlot() {
        if (!hasAvailableSlot()) {
            throw new IllegalStateException("No available slot");
        }
        availableSlots--;
        syncStatus();
    }

    public void releaseSlot() {
        if (availableSlots >= capacity) {
            throw new IllegalStateException("All slots are already available");
        }
        availableSlots++;
        syncStatus();
    }

    public void completeOne() {
        completedCount++;
    }

    public void addRecommendation() {
        recommendationCount++;
    }

    public void archive() {
        status = OfferingStatus.ARCHIVED;
    }

    private void syncStatus() {
        if (status != OfferingStatus.ARCHIVED) {
            status = availableSlots > 0 ? OfferingStatus.OPEN : OfferingStatus.CLOSED;
        }
    }
}

