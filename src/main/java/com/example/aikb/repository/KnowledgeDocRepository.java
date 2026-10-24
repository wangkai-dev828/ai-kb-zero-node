package com.example.aikb.repository;

import com.example.aikb.entity.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
}
