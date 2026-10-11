package com.anurag.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    @JsonIgnoreProperties({"service", "provider"})
    private Slot slot;

    @Column(nullable = false)
    private Integer serviceScore;

    @Column(nullable = false)
    private Integer dateScore;

    @Column(nullable = false)
    private Integer timeScore;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false)
    private String status = "PENDING";

    public Match() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Waitlist getWaitlist() {
        return waitlist;
    }

    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Integer getServiceScore() {
        return serviceScore;
    }

    public void setServiceScore(Integer serviceScore) {
        this.serviceScore = serviceScore;
    }

    public Integer getDateScore() {
        return dateScore;
    }

    public void setDateScore(Integer dateScore) {
        this.dateScore = dateScore;
    }

    public Integer getTimeScore() {
        return timeScore;
    }

    public void setTimeScore(Integer timeScore) {
        this.timeScore = timeScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
