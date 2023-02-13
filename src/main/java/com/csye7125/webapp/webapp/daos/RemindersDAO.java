package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.reminders.Reminder;
import com.csye7125.webapp.webapp.models.tasks.Task;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RemindersDAO extends JpaRepository<Reminder, Long> {

    @Timed(value = "get.all.reminders.by.task.and.user.time", description = "Time taken to get all the reminders by task and user")
    Optional<List<Reminder>> findByTaskAndUserId(Task task, UUID id);

    @Timed(value = "get.reminder.by.id.and.user.time", description = "Time taken to get the reminder by id and user")
    Optional<Reminder> findByIdAndUserId(long reminderId, UUID id);
}
