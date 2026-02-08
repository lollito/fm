package com.lollito.fm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.AchievementType;
import com.lollito.fm.model.UserAchievement;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    boolean existsByUserIdAndAchievement(Long userId, AchievementType achievement);

    List<UserAchievement> findByUserId(Long userId);
}
