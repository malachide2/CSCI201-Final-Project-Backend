package com.hikehub.backend.repository;

import com.hikehub.backend.model.Hike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HikeRepository extends JpaRepository<Hike, Long> {
}

