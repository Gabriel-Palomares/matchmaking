package com.meuprojeto.matchmaking.repository;

import com.meuprojeto.matchmaking.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Long> {

    /**
     * NOVO MÉTODO (Implementa a Sugestão 3)
     * Essencial para o JogadorService validar nomes duplicados.
     * O Spring Data JPA cria a consulta automaticamente pelo nome do método.
     */
    Optional<Jogador> findByNome(String nome);
}