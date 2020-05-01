package com.eroom.erooja.features.todo.repository;

import com.eroom.erooja.domain.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {
    Page<Todo> getTodoListByGoalIdAndUid(Pageable pageable, Long goalId, String uid);
}
