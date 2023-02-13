package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.tags.Tag;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagsDAO extends JpaRepository<Tag, Long> {

    @Timed(value = "get.tag.by.name.time", description = "Time taken to get the tag by name")
    Optional<Tag> findTagByName(String tagName);

    @Timed(value = "get.tag.by.id.and.user.time", description = "Time taken to get the tag by id and user")
    Optional<Tag> findByIdAndUserId(Long tagId, UUID id);

    @Timed(value = "get.tag.by.name.and.user.time", description = "Time taken to get the tag by name and user")
    Optional<Tag> findTagByNameAndUserId(String tagName, UUID id);

    @Timed(value = "get.all.tags.by.user.time", description = "Time taken to get all the tags by user")
    Optional<List<Tag>> findAllByUserId(UUID id);
}
