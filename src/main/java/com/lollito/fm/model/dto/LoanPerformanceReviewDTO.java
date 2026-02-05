package com.lollito.fm.model.dto;

import java.time.LocalDate;
import com.lollito.fm.model.ReviewPeriod;
import com.lollito.fm.model.LoanRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPerformanceReviewDTO {
    private Long id;
    private Long loanAgreementId;

    private LocalDate reviewDate;

    private ReviewPeriod period;

    private Integer matchesPlayed;
    private Integer goals;
    private Integer assists;
    private Double averageRating;
    private Integer yellowCards;
    private Integer redCards;

    private Double skillImprovement;
    private String developmentNotes;
    private Boolean targetsMet;

    private LoanRecommendation recommendation;

    private String reviewNotes;
    private String parentClubFeedback;
    private String loanClubFeedback;
}
