package com.meuprojeto.matchmaking.repository;

import com.meuprojeto.matchmaking.model.ModoDeJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório Spring Data JPA para a entidade ModoDeJogo.
 */
@Repository
public interface ModoDeJogoRepository extends JpaRepository<ModoDeJogo, Long> {
    // Métodos CRUD básicos (save, findById, findAll, delete) são herdados.
}