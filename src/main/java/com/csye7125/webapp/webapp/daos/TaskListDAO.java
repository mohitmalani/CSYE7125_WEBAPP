package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.taskLists.TaskList;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskListDAO extends JpaRepository<TaskList, Long> {

    @Timed(value = "get.list.by.name.time", description = "Time taken to get the list by name")
    Optional<TaskList> findTaskListByName(String taskListName);

    @Timed(value = "get.list.by.id.time", description = "Time taken to get the list by id")
    Optional<TaskList> findTaskListById(Long id);

    @Timed(value = "get.list.by.id.and.user.time", description = "Time taken to get the list by id and user")
    Optional<TaskList> findByIdAndUserId(Long listId, UUID id);

    @Timed(value = "get.all.lists.by.user.time", description = "Time taken to get all the lists by user")
    Optional<List<TaskList>> findAllByUserId(UUID id);
}
