package com.meuprojeto.matchmaking;

import javafx.application.Application; // Import do JavaFX
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Ponto de entrada principal.
 * Esta classe agora APENAS lança a aplicação JavaFX (JavaFxApplication.class).
 * O Spring Boot será inicializado *dentro* da classe JavaFxApplication.
 */
@SpringBootApplication
public class MatchmakingApplication {

    public static void main(String[] args) {
        // Isso irá lançar a classe JavaFxApplication,
        // que por sua vez inicializará o Spring Boot.
        Application.launch(JavaFxApplication.class, args);
    }
}