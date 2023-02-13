package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.comments.Comment;
import com.csye7125.webapp.webapp.models.tasks.Task;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentsDAO extends JpaRepository<Comment, Long> {

    @Timed(value = "get.all.comments.by.task.and.user.time", description = "Time taken to get all the comments by task and user")
    Optional<List<Comment>> findByTaskAndUserId(Task task, UUID id);

    @Timed(value = "get.comment.by.id.and.user.time", description = "Time taken to get the comment by id and user")
    Optional<Comment> findByIdAndUserId(long commentId, UUID id);
}
