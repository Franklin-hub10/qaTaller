import java.util.Scanner;

// Imports de cada demo (en sus paquetes)
import creacional.NotificacionFactorySimple;
import creacional.GeneradorIdsMulti;
import estructural.AdapterPasarelaPagosSimple;
import estructural.FacadeHealthcheckUltraSimple;
import comportamiento.ObserverPrecioDinamicoSimple;
import comportamiento.CommandFileSystemSimulado;

public class Main {

    private static final Scanner in = new Scanner(System.in);

    private static void pausa() {
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        try { in.nextLine(); } catch (Exception ignored) {}
        System.out.println();
    }

    private static void ejecutar(String titulo, Runnable r) {
        System.out.println("============================================================");
        System.out.println(" Ejecutando: " + titulo);
        System.out.println("============================================================");
        try { r.run(); }
        catch (Throwable t) {
            System.out.println("ERROR: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        }
        System.out.println("============================================================");
        pausa();
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("==== MENÚ DEMOS (Patrones en consola) ====");
            System.out.println(" 1) Factory Method — Notificaciones");
            System.out.println(" 2) Singleton — Generador de IDs multiserie");
            System.out.println(" 3) Adapter — Pasarela de pagos (simple)");
            System.out.println(" 4) Facade — Healthcheck simple");
            System.out.println(" 5) Observer — Precio dinámico de transporte");
            System.out.println(" 6) Command — Sistema de archivos simulado");
            System.out.println(" 0) Salir");
            System.out.print("Elige una opción: ");

            String opt = in.nextLine().trim();

            switch (opt) {
                case "1":
                    ejecutar("Factory Method — Notificaciones",
                        () -> NotificacionFactorySimple.main(new String[]{}));
                    break;
                case "2":
                    ejecutar("Singleton — Generador de IDs multiserie",
                        () -> GeneradorIdsMulti.main(new String[]{}));
                    break;
                case "3":
                    ejecutar("Adapter — Pasarela de pagos (simple)",
                        () -> AdapterPasarelaPagosSimple.main(new String[]{}));
                    break;
                case "4":
                    ejecutar("Facade — Healthcheck simple",
                        () -> FacadeHealthcheckUltraSimple.main(new String[]{}));
                    break;
                case "5":
                    ejecutar("Observer — Precio dinámico de transporte",
                        () -> {
                            try { ObserverPrecioDinamicoSimple.main(new String[]{}); }
                            catch (Exception e) { throw new RuntimeException(e); }
                        });
                    break;
                case "6":
                    ejecutar("Command — Sistema de archivos simulado",
                        () -> CommandFileSystemSimulado.main(new String[]{}));
                    break;
                case "0":
                    System.out.println("¡Listo! Saliendo.");
                    return;
                default:
                    System.out.println("Opción no válida.\n");
            }
        }
    }
}
