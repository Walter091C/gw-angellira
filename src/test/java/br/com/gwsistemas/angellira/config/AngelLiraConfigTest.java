package br.com.gwsistemas.angellira.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AngelLiraConfigTest {
    @Test void validaCredenciaisSemExibirValores() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new AngelLiraConfig(AngelLiraConfig.DEFAULT_ENDPOINT, " ", "senha-teste", true, false));
        assertFalse(error.getMessage().contains("senha-teste"));
        assertThrows(IllegalArgumentException.class,
                () -> new AngelLiraConfig(AngelLiraConfig.DEFAULT_ENDPOINT, "usuario", null, true, false));
    }
    @Test void rejeitaEndpointInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new AngelLiraConfig("file:///tmp/wsdl", "usuario", "senha", true, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AngelLiraConfig("https://usuario:senha@example.com", "usuario", "senha", true, false));
    }
    @Test void exigeTimeoutsPositivos() {
        assertThrows(IllegalArgumentException.class,
                () -> new AngelLiraConfig(AngelLiraConfig.DEFAULT_ENDPOINT, "usuario", "senha", true, false, 0, 1000));
    }
    @Test void preservaCredenciaisETimeoutsInformados() {
        AngelLiraConfig config = new AngelLiraConfig(AngelLiraConfig.DEFAULT_ENDPOINT,
                "usuario", " senha com espaços ", false, true, 2000, 5000);
        assertEquals(" senha com espaços ", config.getPassword());
        assertEquals(2000, config.getConnectionTimeoutMillis());
        assertEquals(5000, config.getReceiveTimeoutMillis());
        assertFalse(config.isHomologacao());
        assertTrue(config.isApiInterna());
    }
}
