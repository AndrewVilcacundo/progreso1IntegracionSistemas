import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Mantiene el sistema corriendo por 5 minutos para procesar archivos CSV.
 */
public class MainApp {

    public static void main(String[] args) throws Exception {

        // 1. Crear el contexto de Camel
        CamelContext context = new DefaultCamelContext();

        // 2. Agregar la ruta de procesamiento
        context.addRoutes(new FileRoute());

        // 3. Iniciar el contexto
        context.start();

        System.out.println("==================================================");
        System.out.println("  Sistema de procesamiento CSV corriendo...");
        System.out.println("  Carpeta de entrada: data/input");
        System.out.println("  Válidos   -> data/output  + data/archive");
        System.out.println("  Inválidos -> data/error");
        System.out.println("==================================================");

        // 4. Mantener activo por 5 minutos
        Thread.sleep(5 * 60 * 1000);

        // 5. Detener limpiamente
        context.stop();
        System.out.println("Sistema detenido.");
    }
}
