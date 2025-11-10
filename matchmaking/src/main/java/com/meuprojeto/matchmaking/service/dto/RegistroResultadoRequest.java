package com.meuprojeto.matchmaking.service.dto;

/**
 * DTO para enviar os dados do resultado de uma partida para o serviço.
 * Implementa a Sugestão 1: Inclui os IDs para MVP e Destaque do time perdedor.
 */
public class RegistroResultadoRequest {

    private Long idPartida;
    private Long idEquipeVencedora; // Nulo se for empate
    private boolean empate = false;
    private Long idMvp; // MVP geral (bônus de rating)
    private Long idDestaquePerdedor; // MVP do time perdedor (mitigação de perda)

    // Getters e Setters (Necessários para a UI preencher)
    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public Long getIdEquipeVencedora() { return idEquipeVencedora; }
    public void setIdEquipeVencedora(Long idEquipeVencedora) { this.idEquipeVencedora = idEquipeVencedora; }

    public boolean isEmpate() { return empate; }
    public void setEmpate(boolean empate) { this.empate = empate; }

    public Long getIdMvp() { return idMvp; }
    public void setIdMvp(Long idMvp) { this.idMvp = idMvp; }

    public Long getIdDestaquePerdedor() { return idDestaquePerdedor; }
    public void setIdDestaquePerdedor(Long idDestaquePerdedor) { this.idDestaquePerdedor = idDestaquePerdedor; }
}