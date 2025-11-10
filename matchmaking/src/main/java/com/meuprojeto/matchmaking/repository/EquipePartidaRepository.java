package com.meuprojeto.matchmaking.repository;

import com.meuprojeto.matchmaking.model.EquipePartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipePartidaRepository extends JpaRepository<EquipePartida, Long> {
}