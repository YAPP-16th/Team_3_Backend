package com.eroom.erooja.features.membergoal.service;

import com.eroom.erooja.common.constants.ErrorEnum;
import com.eroom.erooja.common.exception.EroojaException;
import com.eroom.erooja.domain.enums.GoalRole;
import com.eroom.erooja.domain.model.Goal;
import com.eroom.erooja.domain.model.MemberGoal;
import com.eroom.erooja.domain.model.MemberGoalPK;
import com.eroom.erooja.domain.model.Members;
import com.eroom.erooja.features.goal.exception.GoalNotFoundException;
import com.eroom.erooja.features.goal.repository.GoalRepository;
import com.eroom.erooja.features.goal.service.GoalService;
import com.eroom.erooja.features.membergoal.dto.GoalJoinMemberDTO;
import com.eroom.erooja.features.membergoal.dto.GoalJoinRequestDTO;
import com.eroom.erooja.features.membergoal.dto.GoalJoinTodoDto;
import com.eroom.erooja.features.membergoal.dto.UpdateJoinRequestDTO;
import com.eroom.erooja.features.membergoal.repository.MemberGoalRepository;
import com.eroom.erooja.features.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberGoalService {
    private final MemberGoalRepository memberGoalRepository;
    private final GoalRepository goalRepository;
    private final GoalService goalService;
    private final TodoService todoService;

    public MemberGoal joinExistGoal(String uid, GoalJoinRequestDTO goalJoinRequest) {
        Goal goal = goalRepository.findById(goalJoinRequest.getGoalId())
                .orElseThrow(() -> new EroojaException(ErrorEnum.GOAL_NOT_FOUND));

        if (goal.getIsEnd())
            throw new EroojaException(ErrorEnum.GOAL_TERMINATED);

        if (goalJoinRequest.isExistOwnerUid())
            increaseCopyCount(goalJoinRequest.getOwnerUid(), goalJoinRequest.getGoalId());

        goalService.increaseJoinCount(goal.getId());

        MemberGoal memberGoal = null;
        if (goal.getIsDateFixed())
            memberGoal = addMemberGoal(uid, goal.getId(), goal.getEndDt(), GoalRole.PARTICIPANT);
        else
            memberGoal = addMemberGoal(uid, goal.getId(), goalJoinRequest.getEndDt(), GoalRole.PARTICIPANT);

        todoService.addTodo(uid, goal.getId(), goalJoinRequest.getTodoList());
        return memberGoal;
    }

    public void increaseCopyCount(String uid, Long goalId) {
        MemberGoal memberGoal = memberGoalRepository.findById(new MemberGoalPK(uid, goalId))
                .orElseThrow(() -> new GoalNotFoundException(ErrorEnum.GOAL_NOT_FOUND));

        memberGoal.increaseCopyCount();
        memberGoalRepository.save(memberGoal);
    }

    public Boolean isAlreadyExistJoin(String uid, Long goalId) {
        Optional<MemberGoal> memberGoal = memberGoalRepository.findById(new MemberGoalPK(uid, goalId));

        if (memberGoal.isPresent())
            return true;

        return false;
    }

    public MemberGoal addMemberGoal(String uid, Long goalId, LocalDateTime endDt, GoalRole goalRole) {
        return memberGoalRepository.save(MemberGoal.builder()
                .goalId(goalId)
                .uid(uid)
                .copyCount(0)
                .startDt(LocalDateTime.now())
                .endDt(endDt)
                .role(goalRole)
                .isEnd(false).build());
    }

    @Transactional
    public Page<GoalJoinMemberDTO> getEndedGoalJoinPageByUid(String uid, Pageable pageable) {
        Page<MemberGoal> memberGoals = memberGoalRepository.findAllByUidAndEndDtIsBeforeOrIsEndTrue(uid, LocalDateTime.now(), pageable);
        return convertPage2DTO(memberGoals);
    }

    @Transactional
    public Page<GoalJoinMemberDTO> getGoalJoinPageByUid(String uid, Pageable pageable) {
        Page<MemberGoal> memberGoals = memberGoalRepository.findAllByUidAndEndDtIsAfterAndIsEndFalse(uid, LocalDateTime.now(), pageable);
        return convertPage2DTO(memberGoals);
    }

    private Page<GoalJoinMemberDTO> convertPage2DTO(Page<MemberGoal> origin) {
        return new PageImpl<>(
                origin.getContent().stream()
                        .map(mg -> GoalJoinMemberDTO.of(mg, goalService.findGoalById(mg.getGoalId())))
                        .collect(Collectors.toList()),
                origin.getPageable(), origin.getTotalElements());
    }

    public Page<Members> getMembersAllByGoalId(Long goalId, Pageable pageable) {
        Page<MemberGoal> memberGoalPage = memberGoalRepository.findAllByGoal_Id(goalId, pageable);
        List<Members> members = memberGoalPage.getContent().stream()
                .map(MemberGoal::getMember)
                .collect(Collectors.toList());

        return new PageImpl<>(members, memberGoalPage.getPageable(), memberGoalPage.getTotalElements());
    }

    public int countGoalJoinByGoalId(Long goalId) {
        return memberGoalRepository.countMemberGoalByGoalId(goalId);
    }

    public Page<GoalJoinTodoDto> getJoinTodoListByGoalId(Long goalId, Pageable pageable) {
        return memberGoalRepository.getJoinTodoListByGoalId(goalId, pageable);
    }

    public MemberGoal againJoin(UpdateJoinRequestDTO updateGoalJoinRequest, String uid, Long goalId) {
        MemberGoal memberGoal = memberGoalRepository.findById(new MemberGoalPK(uid, goalId))
                .orElseThrow(() -> new EroojaException(ErrorEnum.GOAL_JOIN_NOT_FOUND));
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EroojaException(ErrorEnum.GOAL_NOT_FOUND));

        memberGoal.setIsEnd(false);
        memberGoal.setStartDt(LocalDateTime.now());

        if (goal.getIsDateFixed()) {
            if(goal.isExpire())
                throw new EroojaException(ErrorEnum.GOAL_TERMINATED);
            memberGoal.setEndDt(goal.getEndDt());
        }else{
            memberGoal.setEndDt(updateGoalJoinRequest.getEndDt());
        }

        return memberGoalRepository.save(memberGoal);
    }

    public MemberGoal changeJoinToEnd(String uid, Long goalId) {
        MemberGoal memberGoal = memberGoalRepository.findById(new MemberGoalPK(uid, goalId))
                .orElseThrow(() -> new EroojaException(ErrorEnum.GOAL_JOIN_NOT_FOUND));

        memberGoal.setIsEnd(true);

        return memberGoalRepository.save(memberGoal);
    }

    public MemberGoal getGoalJoinByUidAndGoalId(String uid, Long goalId) {
        return memberGoalRepository.findById(new MemberGoalPK(uid, goalId))
                .orElseThrow(() -> new GoalNotFoundException(ErrorEnum.GOAL_JOIN_NOT_FOUND));
    }

    public GoalRole getGoalRole(String uid, Long goalId) {
        MemberGoal memberGoal = memberGoalRepository.findById(new MemberGoalPK(uid, goalId))
                .orElseThrow(() -> new GoalNotFoundException(ErrorEnum.GOAL_JOIN_NOT_FOUND));

        return memberGoal.getRole();
    }

    public List<MemberGoal> getAllEndedYesterday() {
        LocalDateTime targetTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        return memberGoalRepository.findAllByEndDtIsAfter(targetTime);
    }
}
