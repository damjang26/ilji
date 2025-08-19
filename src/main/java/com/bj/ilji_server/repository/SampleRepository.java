// src/main/java/com/bj/ilji_server/repository/SampleRepository.java
package com.bj.ilji_server.repository;

import com.bj.ilji_server.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<SampleEntity, Long> {
}