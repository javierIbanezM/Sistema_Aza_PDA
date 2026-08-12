// snippets/java/builder.java
// Builder pattern con Lombok — ver patterns/builder.md para el razonamiento completo.

@Value
@Builder
public class CrearPedidoRequest {
    String clienteId;
    List<LineaPedidoRequest> lineas;
    String notas; // opcional

    public static CrearPedidoRequestBuilder builderConValoresPorDefecto() {
        return CrearPedidoRequest.builder()
            .lineas(new ArrayList<>());
    }
}

// Uso:
// CrearPedidoRequest request = CrearPedidoRequest.builder()
//     .clienteId("c-123")
//     .lineas(lineas)
//     .build();

// --- Builder por pasos (fuerza campos obligatorios en tiempo de compilación) ---

public class PeticionHttpBuilder {

    public interface ConUrl {
        ConMetodo url(String url);
    }

    public interface ConMetodo {
        Buildable metodo(String metodo);
    }

    public interface Buildable {
        Buildable header(String nombre, String valor);
        PeticionHttp build();
    }

    public static ConUrl nueva() {
        return new Impl();
    }

    private static class Impl implements ConUrl, ConMetodo, Buildable {
        private String url;
        private String metodo;
        private final Map<String, String> headers = new LinkedHashMap<>();

        public ConMetodo url(String url) { this.url = url; return this; }
        public Buildable metodo(String metodo) { this.metodo = metodo; return this; }
        public Buildable header(String nombre, String valor) { headers.put(nombre, valor); return this; }
        public PeticionHttp build() { return new PeticionHttp(url, metodo, headers); }
    }
}

// Uso:
// PeticionHttp peticion = PeticionHttpBuilder.nueva()
//     .url("https://api.ejemplo.com")
//     .metodo("POST")
//     .header("Authorization", "Bearer ...")
//     .build();
