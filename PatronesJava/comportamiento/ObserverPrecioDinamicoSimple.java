package comportamiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer — Precio dinámico de transporte (Java)
 * Equivalente del Python "precio_dinamico_transporte_observer.py".
 * Ejecutar:
 *   javac comportamiento/ObserverPrecioDinamicoSimple.java
 *   java comportamiento.ObserverPrecioDinamicoSimple
 */
interface Observador { void update(String nivel); }

class Market {
    private final List<Observador> obs = new ArrayList<>();
    private final long debounceMs;
    private long lastTs = 0L;

    public Market(double debounceSeconds) {
        this.debounceMs = (long)(debounceSeconds * 1000);
    }
    public void attach(Observador o){ if (!obs.contains(o)) obs.add(o); }
    public void detach(Observador o){ obs.remove(o); }

    public void setDemanda(String nivel) {
        long now = System.currentTimeMillis();
        if (debounceMs > 0 && now - lastTs < debounceMs) {
            System.out.println("Demanda: " + nivel + " - cambio ignorado por intervalo corto");
            return;
        }
        lastTs = now;
        System.out.println("Demanda: " + nivel);
        for (Observador o : new ArrayList<>(obs)) {
            try { o.update(nivel); }
            catch (Exception e) { System.out.println("Observer con error: " + o.getClass().getSimpleName() + " - " + e); }
        }
    }
}
class RepriceService implements Observador {
    public void update(String nivel) {
        double base = 1.20, tarifa;
        if ("alta".equals(nivel)) tarifa = base * 1.50;
        else if ("media".equals(nivel)) tarifa = base * 1.20;
        else tarifa = base * 0.90;
        System.out.println("Reprice: tarifa por km = $" + String.format("%.2f", tarifa));
    }
}
class FleetService implements Observador {
    public void update(String nivel) {
        if ("alta".equals(nivel)) System.out.println("Fleet: activar +20% de unidades");
        else if ("media".equals(nivel)) System.out.println("Fleet: activar +5% de unidades");
        else System.out.println("Fleet: reducir -15% de unidades");
    }
}
class EtaService implements Observador {
    public void update(String nivel) {
        if ("alta".equals(nivel)) System.out.println("ETA: tiempos estimados -10%");
        else if ("media".equals(nivel)) System.out.println("ETA: tiempos estables");
        else System.out.println("ETA: tiempos +10%");
    }
}
class BadObserver implements Observador {
    public void update(String nivel) { throw new RuntimeException("Falla simulada en observer"); }
}

public class ObserverPrecioDinamicoSimple {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Observer — Precio dinámico (Java) ===");
        Market market = new Market(0.5);
        market.attach(new RepriceService());
        market.attach(new FleetService());
        market.attach(new EtaService());
        market.attach(new BadObserver());

        market.setDemanda("alta");
        Thread.sleep(600);
        market.setDemanda("media");
        market.setDemanda("baja");   // ignorado por debounce
        Thread.sleep(600);
        market.setDemanda("baja");   // ahora sí
    }
}
