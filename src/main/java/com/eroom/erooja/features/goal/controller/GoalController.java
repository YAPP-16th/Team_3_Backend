package com.eroom.erooja.features.goal.controller;

import com.eroom.erooja.common.exception.EroojaException;
import com.eroom.erooja.domain.model.Goal;
import com.eroom.erooja.features.goal.dto.CreateGoalRequest;
import com.eroom.erooja.features.goal.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RequiredArgsConstructor
@RequestMapping("/api/v1/goal")
@RestController
public class GoalController {
    private final GoalService goalService;
    private static final Logger logger = LoggerFactory.getLogger(GoalController.class);

    @GetMapping("/{goalId}")
    ResponseEntity getGoalDetail() {
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @GetMapping("/interest/{interestId}")
    ResponseEntity getGoalList(@PathVariable("interestId}") String interest) {
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @GetMapping
    ResponseEntity searchGoal(@PathVariable String filter, @PathVariable String keyword) {
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity createGoal(@RequestBody @Valid CreateGoalRequest createGoalRequest,
                              Errors errors) {
        if(errors.hasErrors()){
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }

        Goal newGoal = goalService.createGoal(createGoalRequest);
        return new ResponseEntity(newGoal, HttpStatus.CREATED);
    }

    @PutMapping("/{goalId}")
    ResponseEntity updateGoal() {
        return new ResponseEntity(null, HttpStatus.OK);
    }
}