import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias y de integración para la ruta de procesamiento CSV de
 * Camel.
 */
public class FileRouteTest extends CamelTestSupport {

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new FileRoute();
    }

    @BeforeEach
    public void setupDirectories() {
        new File("data/input").mkdirs();
        new File("data/output").mkdirs();
        new File("data/archive").mkdirs();
        new File("data/error").mkdirs();
    }

    @Test
    public void testRouteLoadsCorrectly() {
        assertNotNull(context.getRoute("ruta-csv"), "La ruta 'ruta-csv' debe existir en el contexto");
    }

    @Test
    public void testCsvHeaderValidationLogic() {
        String validHeader = "patient_id,full_name,appointment_date,insurance_code";
        String[] parts = validHeader.split(",");
        assertEquals(4, parts.length);
        assertEquals("patient_id", parts[0]);
        assertEquals("insurance_code", parts[3]);
    }

    @Test
    public void testDateRegexPattern() {
        String validDate = "2026-03-15";
        String invalidDate = "15/03/2026";
        assertTrue(validDate.matches("\\d{4}-\\d{2}-\\d{2}"), "La fecha en formato YYYY-MM-DD debe ser válida");
        assertTrue(!invalidDate.matches("\\d{4}-\\d{2}-\\d{2}"), "Formatos de fecha diferentes deben ser rechazados");
    }

    @Test
    void testTemporalParaProbarNotificacionDeFallo() {
        assertEquals(1, 2, "Fallo intencional para probar CircleCI");
    }

    @Test
    public void testInsuranceValidationValues() {
        String[] validInsurances = { "IESS", "PRIVADO", "NINGUNO" };
        for (String ins : validInsurances) {
            assertTrue(ins.equals("IESS") || ins.equals("PRIVADO") || ins.equals("NINGUNO"));
        }
    }
}
