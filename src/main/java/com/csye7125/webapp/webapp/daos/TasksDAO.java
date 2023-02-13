package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.taskLists.TaskList;
import com.csye7125.webapp.webapp.models.tasks.Task;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TasksDAO extends JpaRepository<Task, Long> {

    @Timed(value = "get.all.tasks.by.list.and.user.time", description = "Time taken to get all the tasks by list and user")
    Optional<List<Task>> findByTaskListAndUserId(TaskList taskList, UUID id);

    @Timed(value = "get.task.by.task.id.list.and.user.time", description = "Time taken to get the task by task id, list and user")
    Optional<Task> findByTaskListAndUserIdAndId(TaskList taskList, UUID id, long taskId);

    @Timed(value = "get.task.by.task.id.and.user.time", description = "Time taken to get the task by task id and user")
    Optional<Task> findByIdAndUserId(long taskId, UUID id);
}
