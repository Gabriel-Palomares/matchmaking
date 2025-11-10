package com.meuprojeto.matchmaking.repository;

import com.meuprojeto.matchmaking.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <<< NOVO IMPORT
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    /**
     * <<< MÉTODO ATUALIZADO (A CORREÇÃO) >>>
     *
     * Busca todas as partidas, mas também força o carregamento (JOIN FETCH)
     * de todos os dados relacionados (ModoDeJogo, Equipes, Membros, Jogadores e Resultados)
     * que a UI da Aba 3 ("Histórico de Partidas") precisará.
     * * O 'DISTINCT' é crucial para evitar duplicatas de 'Partida'
     * ao fazer JOIN com múltiplas coleções.
     */
    @Query("SELECT DISTINCT p FROM Partida p " +
            "JOIN FETCH p.modoDeJogo " +
            "LEFT JOIN FETCH p.equipes e " + // LEFT JOIN caso uma partida seja criada sem equipes (embora não deva acontecer)
            "LEFT JOIN FETCH e.membros m " +
            "LEFT JOIN FETCH m.jogador " +
            "LEFT JOIN FETCH e.resultado " +
            "ORDER BY p.dataHora DESC") // Ordena pela data/hora da mais recente
    List<Partida> findAllByOrderByDataHoraDesc();
}