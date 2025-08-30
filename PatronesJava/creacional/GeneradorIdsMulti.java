package creacional;

import java.util.Map;
import java.util.TreeMap;

/**
 * Singleton — Generador de IDs multiserie (Java)
 * Equivalente del Python "creacion_tickets_singleton.py".
 * Ejecutar:
 *   javac creacional/GeneradorIdsMulti.java
 *   java creacional.GeneradorIdsMulti
 */
public class GeneradorIdsMulti {
    private static GeneradorIdsMulti INSTANCE;
    private final Map<String,Integer> series = new TreeMap<>();
    private final int ancho = 4; private final String sep = "-";

    private GeneradorIdsMulti(){}

    public static synchronized GeneradorIdsMulti getInstance() {
        if (INSTANCE == null) INSTANCE = new GeneradorIdsMulti();
        return INSTANCE;
    }

    public synchronized String siguiente(String prefijo) {
        String p = prefijo.toUpperCase().trim();
        int n = series.getOrDefault(p, 0) + 1;
        series.put(p, n);
        return String.format("%s%s%0" + ancho + "d", p, sep, n);
    }
    public synchronized int valorActual(String prefijo) {
        return series.getOrDefault(prefijo.toUpperCase().trim(), 0);
    }
    public synchronized void setInicio(String prefijo, int valor) {
        series.put(prefijo.toUpperCase().trim(), Math.max(0, valor));
    }
    public synchronized Map<String,Integer> estado(){ return new TreeMap<>(series); }

    // Demo
    public static void main(String[] args) {
        System.out.println("=== Singleton: Generador de IDs multiserie (Java) ===");
        GeneradorIdsMulti g = GeneradorIdsMulti.getInstance();
        System.out.println("[Ticket] " + g.siguiente("TICKET"));
        System.out.println("[Ticket] " + g.siguiente("TICKET"));
        System.out.println("[Auto]   " + g.siguiente("AUTO"));
        System.out.println("[Sin]    " + g.siguiente("SIN"));
        System.out.println("TICKET actual: " + g.valorActual("TICKET"));
        g.setInicio("SIN", 100);
        System.out.println("Nuevo SIN: " + g.siguiente("SIN"));
        System.out.println("Estado: " + g.estado());
    }
}
