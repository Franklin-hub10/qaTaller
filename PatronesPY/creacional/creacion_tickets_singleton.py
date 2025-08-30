"""
Singleton — Generador de IDs multiserie (consola)
"""

from __future__ import annotations
from typing import Dict

class GeneradorIdsMulti:
    _instance = None

    def __new__(cls, ancho: int = 4, sep: str = "-"):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self, ancho: int = 4, sep: str = "-"):
        # Evitar re-inicializar si ya fue creado
        if getattr(self, "_inicializado", False):
            return
        self._inicializado = True

        self._ancho = int(ancho)
        self._sep = sep
        self._series: Dict[str, int] = {}  # p.ej. {"TICKET": 3, "AUTO": 2}

    def siguiente(self, prefijo: str) -> str:
        """Devuelve el próximo ID para el prefijo dado (y avanza el contador)."""
        p = prefijo.upper().strip()
        n = self._series.get(p, 0) + 1
        self._series[p] = n
        return f"{p}{self._sep}{n:0{self._ancho}d}"

    def valor_actual(self, prefijo: str) -> int:
        """Devuelve el valor actual del contador para el prefijo (0 si no existe)."""
        return self._series.get(prefijo.upper().strip(), 0)

    def set_inicio(self, prefijo: str, valor: int) -> None:
        """Fija manualmente el contador de una serie (útil para arrancar en 100, por ejemplo)."""
        p = prefijo.upper().strip()
        self._series[p] = max(0, int(valor))

    def estado(self) -> Dict[str, int]:
        """Devuelve un dict con todas las series y su contador actual."""
        return dict(self._series)


# ------------------------- DEMO DE CONSOLA -------------------------

def crear_ticket():
    gen = GeneradorIdsMulti()  # misma instancia siempre
    ticket_id = gen.siguiente("TICKET")
    print(f"[Ticket] Creado: {ticket_id}")
    return ticket_id

def crear_autorizacion():
    gen = GeneradorIdsMulti()
    auto_id = gen.siguiente("AUTO")
    print(f"[Autorización] Creada: {auto_id}")
    return auto_id

def crear_siniestro():
    gen = GeneradorIdsMulti()
    sin_id = gen.siguiente("SIN")
    print(f"[Siniestro] Creado: {sin_id}")
    return sin_id

def demo():
    print("=== Singleton: Generador de IDs multiserie ===")
    gen1 = GeneradorIdsMulti(ancho=4, sep="-")
    gen2 = GeneradorIdsMulti()  # misma instancia

    print("¿Misma instancia?", id(gen1) == id(gen2), hex(id(gen1)))

    # Simular creación de entidades en distintos módulos/funciones:
    print("\n-- Creación inicial --")
    crear_ticket()      # TICKET-0001
    crear_ticket()      # TICKET-0002
    crear_autorizacion()# AUTO-0001
    crear_siniestro()   # SIN-0001
    crear_autorizacion()# AUTO-0002
    crear_siniestro()   # SIN-0002
    crear_ticket()      # TICKET-0003

    print("\nEstado actual por serie:", gen1.estado())

    # Mostrar que otra "instancia" ve los mismos contadores
    print("\n-- Desde otra 'instancia' (mismo singleton) --")
    print("TICKET actual:", gen2.valor_actual("TICKET"))
    print("Generar otro TICKET:", gen2.siguiente("TICKET"))  # TICKET-0004

    # Arrancar una serie en un número específico (ej: migración/continuidad)
    print("\n-- Fijar inicio de SIN en 100 --")
    gen2.set_inicio("SIN", 100)
    print("SIN actual:", gen2.valor_actual("SIN"))
    print("Nuevo SIN:", gen2.siguiente("SIN"))  # SIN-0101

    print("\nEstado final:", gen1.estado())


if __name__ == "__main__":
    demo()
