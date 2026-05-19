package com.havryliuk.movie_tracker.repository;

import com.havryliuk.movie_tracker.entity.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
}