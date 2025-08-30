# Ejecuta:  python observer_precio_dinamico_simple.py

import time

# --------- Sujeto (emisor de eventos) ---------
class Market:
    def __init__(self, debounce_seconds=0.0):
        self._observers = []
        self._last_ts = 0.0
        self._debounce = float(debounce_seconds)

    def attach(self, obs):
        if obs not in self._observers:
            self._observers.append(obs)

    def detach(self, obs):
        if obs in self._observers:
            self._observers.remove(obs)

    def set_demanda(self, nivel):
        # Evita disparar eventos si llegan demasiado rápido
        now = time.time()
        if self._debounce and (now - self._last_ts) < self._debounce:
            print("Demanda:", nivel, "- cambio ignorado por intervalo corto")
            return

        self._last_ts = now
        print("Demanda:", nivel)
        for obs in list(self._observers):
            try:
                obs.update(nivel)
            except Exception as e:
                # Aísla fallas de un observador para que no afecte a los demás
                print("Observer con error:", obs.__class__.__name__, "-", e)

# --------- Observadores (reaccionan al evento) ---------
class RepriceService:
    def update(self, nivel):
        base = 1.20  # tarifa base por km (ejemplo)
        if nivel == "alta":
            tarifa = base * 1.50
        elif nivel == "media":
            tarifa = base * 1.20
        else:  # "baja" u otros
            tarifa = base * 0.90
        print("Reprice: tarifa por km =", f"${tarifa:.2f}")

class FleetService:
    def update(self, nivel):
        if nivel == "alta":
            print("Fleet: activar +20% de unidades")
        elif nivel == "media":
            print("Fleet: activar +5% de unidades")
        else:
            print("Fleet: reducir -15% de unidades")

class EtaService:
    def update(self, nivel):
        if nivel == "alta":
            print("ETA: tiempos estimados -10%")
        elif nivel == "media":
            print("ETA: tiempos estables")
        else:
            print("ETA: tiempos +10%")

# (Opcional) Observador defectuoso para mostrar aislamiento de errores
class BadObserver:
    def update(self, nivel):
        raise RuntimeError("Falla simulada en observer")

# --------- Demo de consola ---------
def demo():
    print("=== Observer — Precio dinámico de transporte (simple) ===")
    market = Market(debounce_seconds=0.5)  # ignora cambios con menos de 0.5s de separación

    # Suscripciones
    market.attach(RepriceService())
    market.attach(FleetService())
    market.attach(EtaService())
    market.attach(BadObserver())  # no debe romper a los demás

    # Cambios de demanda
    market.set_demanda("alta")
    time.sleep(0.6)
    market.set_demanda("media")
    # Este cambio llega muy rápido y será ignorado por debounce
    market.set_demanda("baja")
    time.sleep(0.6)
    market.set_demanda("baja")  # ahora sí se procesa

if __name__ == "__main__":
    demo()
