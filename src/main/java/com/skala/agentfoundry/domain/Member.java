package com.skala.agentfoundry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true, length = 30)
    private String memberId;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "display_name", nullable = false, length = 30)
    private String displayName;

    @Column(name = "credit_balance", nullable = false)
    private Integer creditBalance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Member(
        String memberId,
        String password,
        String displayName,
        Integer creditBalance,
        LocalDateTime createdAt
    ) {
        this.memberId = memberId;
        this.password = password;
        this.displayName = displayName;
        this.creditBalance = creditBalance;
        this.createdAt = createdAt;
    }

    public static Member create(
        String memberId,
        String password,
        String displayName,
        int initialCredits,
        LocalDateTime createdAt
    ) {
        return new Member(memberId, password, displayName, initialCredits, createdAt);
    }

    public boolean passwordMatches(String rawPassword) {
        return password.equals(rawPassword);
    }

    public boolean hasCredits(int amount) {
        return creditBalance >= amount;
    }

    public void useCredits(int amount) {
        if (amount <= 0 || creditBalance < amount) {
            throw new IllegalArgumentException("Invalid credit use");
        }
        creditBalance -= amount;
    }

    public void addCredits(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid credit amount");
        }
        creditBalance += amount;
    }
}

