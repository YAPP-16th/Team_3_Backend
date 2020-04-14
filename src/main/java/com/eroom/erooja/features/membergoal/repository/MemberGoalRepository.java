package com.eroom.erooja.features.membergoal.repository;

import com.eroom.erooja.domain.model.MemberGoal;
import com.eroom.erooja.domain.model.MemberGoalPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberGoalRepository extends JpaRepository<MemberGoal, MemberGoalPK> {
}