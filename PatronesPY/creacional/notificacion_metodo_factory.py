"""
Factory Method — Notificaciones multicanal (simple)
"""

from abc import ABC, abstractmethod

# --------- Producto (interfaz común) ---------
class Notificacion(ABC):
    @abstractmethod
    def enviar(self, destinatario: str, mensaje: str) -> None:
        """Envía el mensaje al destinatario."""
        pass

# --------- Productos concretos ---------
class NotificacionSMS(Notificacion):
    def enviar(self, destinatario: str, mensaje: str) -> None:
        print(f"[SMS] a {destinatario}: {mensaje}")

class NotificacionEmail(Notificacion):
    def enviar(self, destinatario: str, mensaje: str) -> None:
        print(f"[EMAIL] a {destinatario}: {mensaje}")

class NotificacionWhatsApp(Notificacion):
    def enviar(self, destinatario: str, mensaje: str) -> None:
        print(f"[WHATSAPP] a {destinatario}: {mensaje}")

# --------- Fábrica (Factory Method) ---------
class NotificacionFactory:
    _map = {
        "sms": NotificacionSMS,
        "email": NotificacionEmail,
        "wa": NotificacionWhatsApp,
    }

    @classmethod
    def crear(cls, canal: str) -> Notificacion:
        canal = (canal or "").lower()
        try:
            return cls._map[canal]()
        except KeyError:
            raise ValueError(f"Canal no soportado: {canal}. Usa: {list(cls._map)}")

    @classmethod
    def registrar(cls, canal: str, clase_notificacion):
        """Permite agregar nuevos canales sin tocar el cliente."""
        cls._map[canal.lower()] = clase_notificacion

# --------- Demo de consola ---------
def demo():
    print("Factory Method — Notificaciones multicanal (simple)")
    canales = [
        ("sms", "+593900000000"),
        ("email", "fabo@ejemplo.com"),
        ("wa", "+593911111111"),
    ]
    for canal, destino in canales:
        notif = NotificacionFactory.crear(canal)
        notif.enviar(destino, "Hola Fabo, este es un mensaje de prueba.")

if __name__ == "__main__":
    demo()
