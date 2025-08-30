package creacional;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory Method — Notificaciones multicanal (Java)
 * Equivalente del Python "notificacion_metodo_factory.py".
 * Ejecutar:
 *   javac creacional/NotificacionFactorySimple.java
 *   java creacional.NotificacionFactorySimple
 */
interface Notificacion {
    void enviar(String destinatario, String mensaje);
}

class NotificacionSMS implements Notificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[SMS] a " + destinatario + ": " + mensaje);
    }
}
class NotificacionEmail implements Notificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[EMAIL] a " + destinatario + ": " + mensaje);
    }
}
class NotificacionWhatsApp implements Notificacion {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[WHATSAPP] a " + destinatario + ": " + mensaje);
    }
}

class NotificacionFactory {
    private static final Map<String, Class<? extends Notificacion>> map = new HashMap<>();
    static {
        map.put("sms", NotificacionSMS.class);
        map.put("email", NotificacionEmail.class);
        map.put("wa", NotificacionWhatsApp.class);
    }
    public static Notificacion crear(String canal) {
        try {
            Class<? extends Notificacion> cls = map.get(canal.toLowerCase());
            if (cls == null) throw new IllegalArgumentException("Canal no soportado: " + canal);
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear notificación: " + e.getMessage(), e);
        }
    }
}

public class NotificacionFactorySimple {
    public static void main(String[] args) {
        Object[][] casos = {
            {"sms", "+593900000000"},
            {"email", "fabo@ejemplo.com"},
            {"wa", "+593911111111"}
        };
        System.out.println("Factory Method — Notificaciones (Java)");
        for (Object[] c : casos) {
            Notificacion n = NotificacionFactory.crear((String)c[0]);
            n.enviar((String)c[1], "Hola Fabo, este es un mensaje de prueba.");
        }
    }
}
