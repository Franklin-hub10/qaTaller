package estructural;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * Facade — Healthcheck simple (Java)
 * Equivalente del Python "healhty_sistem_facade.py".
 * Nota: en Python se usó sqlite3; en Java evitamos librerías externas.
 *       Para la "DB" usamos un archivo Properties como verificación básica
 *       de escritura/lectura (comportamiento similar para consola).
 *
 * Ejecutar:
 *   javac estructural/FacadeHealthcheckUltraSimple.java
 *   java estructural.FacadeHealthcheckUltraSimple
 */
class HealthFacade {
    static class Resultado { final boolean ok; final String detalle;
        Resultado(boolean ok, String d){ this.ok = ok; this.detalle = d; } }

    public Resultado checkDisco() {
        File tmp = new File("hc_test.tmp");
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("ok");
        } catch (Exception e) {
            return new Resultado(false, "No se pudo escribir: " + e);
        }
        try (BufferedReader r = new BufferedReader(new FileReader(tmp))) {
            String s = r.readLine();
            tmp.delete();
            return new Resultado("ok".equals(s), "Escritura/lectura en carpeta " + ("ok".equals(s) ? "OK" : "falló"));
        } catch (Exception e) {
            return new Resultado(false, "No se pudo leer: " + e);
        }
    }

    public Resultado checkRed() {
        try {
            InetAddress.getByName("example.com"); // DNS
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("8.8.8.8", 53), 800);
            }
            return new Resultado(true, "DNS y conexión TCP breve OK");
        } catch (Exception e) {
            return new Resultado(false, "Fallo de red: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public Resultado checkDBSimulada() {
        // "DB" simple con java.util.Properties
        File f = new File(System.getProperty("java.io.tmpdir"), "health_demo.properties");
        try {
            Properties p = new Properties();
            if (f.exists()) try (FileInputStream in = new FileInputStream(f)) { p.load(in); }
            int n = Integer.parseInt(p.getProperty("filas", "0")) + 1;
            p.setProperty("filas", Integer.toString(n));
            try (FileOutputStream out = new FileOutputStream(f)) { p.store(out, "health_demo"); }
            return new Resultado(true, "db='" + f.getAbsolutePath() + "' filas=" + n);
        } catch (Exception e) {
            return new Resultado(false, "DB simulada error: " + e);
        }
    }

    public void check() {
        System.out.println("=== Healthcheck ===");
        Resultado d = checkDisco();
        Resultado r = checkRed();
        Resultado b = checkDBSimulada();
        boolean ok = d.ok && r.ok && b.ok;
        System.out.println("DISCO : " + (d.ok ? "OK" : "ERROR") + " - " + d.detalle);
        System.out.println("RED   : " + (r.ok ? "OK" : "ERROR") + " - " + r.detalle);
        System.out.println("DB    : " + (b.ok ? "OK" : "ERROR") + " - " + b.detalle);
        System.out.println("Resultado global: " + (ok ? "SALUDABLE" : "PROBLEMAS"));
    }
}

public class FacadeHealthcheckUltraSimple {
    public static void main(String[] args) {
        new HealthFacade().check();
    }
}
