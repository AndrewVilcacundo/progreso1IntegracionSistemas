import org.apache.camel.builder.RouteBuilder;

public class FileRoute extends RouteBuilder {

    @Override
    public void configure() {

        onException(Exception.class)
                .useOriginalMessage() // esencial: preservar msg original antes de error
                .log(" ERROR en '${file:name}': ${exception.message}")
                .handled(true) 
                .to("file:data/error"); 

        from("file:data/input?delete=true")
                .routeId("ruta-csv")
                .log(" Archivo recibido: ${file:name}")
                .process(exchange -> {

                    String body = exchange.getIn().getBody(String.class);

                    if (body == null || body.trim().isEmpty()) {
                        throw new Exception("El archivo está vacío");
                    }

                    body = body.replace("\r\n", "\n").replace("\r", "\n");

                    String[] lines = body.split("\n");

                    String expectedHeader = "patient_id,full_name,appointment_date,insurance_code";
                    String actualHeader = lines[0].trim();

                    if (!actualHeader.equalsIgnoreCase(expectedHeader)) {
                        throw new Exception(
                                "Encabezado inválido. Se esperaba: [" + expectedHeader +
                                        "] pero se recibió: [" + actualHeader + "]");
                    }

                    for (int i = 1; i < lines.length; i++) {

                        String line = lines[i].trim();

                        if (line.isEmpty())
                            continue;

                        String[] fields = line.split(",");

                        if (fields.length < 4) {
                            throw new Exception(
                                    "Fila " + i + " incompleta: solo tiene " + fields.length + " campo(s)");
                        }

                        for (int j = 0; j < 4; j++) {
                            if (fields[j].trim().isEmpty()) {
                                throw new Exception(
                                        "Campo vacío en fila " + i + ", columna " + (j + 1));
                            }
                        }

                        String fecha = fields[2].trim();
                        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            throw new Exception(
                                    "Fecha inválida en fila " + i + ": '" + fecha +
                                            "'. Formato esperado: yyyy-MM-dd");
                        }

                        String seguro = fields[3].trim();
                        if (!seguro.equals("IESS") &&
                                !seguro.equals("PRIVADO") &&
                                !seguro.equals("NINGUNO")) {
                            throw new Exception(
                                    "Seguro inválido en fila " + i + ": '" + seguro +
                                            "'. Valores permitidos: IESS, PRIVADO, NINGUNO");
                        }
                    }
                })
                .log(" Archivo válido: ${file:name}")
                
                // esencial: archivo se escribe antes de output para mantener consistencia atómica
                .toD("file:data/archive?fileName=${file:name.noext}_${date:now:yyyy-MM-dd_HHmmss}.csv")
                .log(" Archivado en data/archive con timestamp")
                .to("file:data/output")
                .log(" Enviado a data/output")
                .log("─────────────────────────────────────────────────────────");
    }
}

