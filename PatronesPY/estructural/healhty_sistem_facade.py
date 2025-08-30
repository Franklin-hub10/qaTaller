# facade_healthcheck_ultra_simple.py
# Ejecuta:  python facade_healthcheck_ultra_simple.py

import os
import socket
import sqlite3

class HealthFacade:
    def check(self):
        resultados = {}

        # --- DISCO: probar escritura/lectura en la carpeta actual ---
        try:
            fname = "hc_test.tmp"
            with open(fname, "w", encoding="utf-8") as f:
                f.write("ok")
            with open(fname, "r", encoding="utf-8") as f:
                contenido = f.read()
            os.remove(fname)
            ok = (contenido == "ok")
            msg = "Escritura/lectura en carpeta OK" if ok else "Lectura inesperada"
            resultados["disco"] = (ok, msg)
        except Exception as e:
            resultados["disco"] = (False, f"No se pudo escribir/leer: {e}")

        # --- RED (ping simple): DNS + conexión TCP breve ---
        try:
            socket.setdefaulttimeout(0.8)
            # DNS
            socket.gethostbyname("example.com")
            # TCP breve a DNS público
            s = socket.socket()
            s.settimeout(0.8)
            s.connect(("8.8.8.8", 53))
            s.close()
            resultados["red"] = (True, "DNS y conexión TCP breve OK")
        except Exception as e:
            resultados["red"] = (False, f"Fallo de red: {type(e).__name__}: {e}")

        # --- SQLITE: operación básica en memoria ---
        try:
            conn = sqlite3.connect(":memory:")
            cur = conn.cursor()
            cur.execute("CREATE TABLE t (id INTEGER PRIMARY KEY, v TEXT)")
            cur.execute("INSERT INTO t (v) VALUES ('hola')")
            conn.commit()
            cur.execute("SELECT COUNT(*) FROM t")
            n = cur.fetchone()[0]
            cur.close()
            conn.close()
            resultados["sqlite"] = (n >= 1, f"Operación básica OK, filas={n}")
        except Exception as e:
            resultados["sqlite"] = (False, f"SQLite error: {type(e).__name__}: {e}")

        # --- Impresión simple, sin íconos ---
        print("=== Healthcheck ===")
        ok_global = True
        for nombre in ("disco", "red", "sqlite"):
            ok, msg = resultados[nombre]
            ok_global = ok_global and ok
            estado = "OK" if ok else "ERROR"
            print(f"{nombre.upper()}: {estado} - {msg}")
        print("Resultado global:", "SALUDABLE" if ok_global else "PROBLEMAS")
        return resultados


if __name__ == "__main__":
    HealthFacade().check()
