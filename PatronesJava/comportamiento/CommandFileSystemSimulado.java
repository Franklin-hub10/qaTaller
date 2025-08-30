package comportamiento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

/**
 * Command — Sistema de archivos simulado (Java)
 * Equivalente del Python "command_filesystem_simulado.py".
 * Ejecutar:
 *   javac comportamiento/CommandFileSystemSimulado.java
 *   java comportamiento.CommandFileSystemSimulado
 */
// ===== Receiver =====
class SistemaArchivos {
    private final Map<String,String> files = new TreeMap<>();
    boolean existe(String path){ return files.containsKey(path); }
    void escribir(String path,String contenido){ files.put(path, contenido); }
    String leer(String path){ if(!existe(path)) throw new IllegalStateException("No existe: "+path); return files.get(path); }
    void eliminar(String path){ if(!existe(path)) throw new IllegalStateException("No existe: "+path); files.remove(path); }
    void mover(String src,String dst){
        if(!existe(src)) throw new IllegalStateException("No existe: "+src);
        if(existe(dst)) throw new IllegalStateException("Ya existe destino: "+dst);
        files.put(dst, files.get(src)); files.remove(src);
    }
    Map<String,String> listar(){ return new TreeMap<>(files); }
}
// ===== Command =====
interface Comando { String ejecutar(); String deshacer(); }
// ===== Commands concretos =====
class ComandoCrear implements Comando {
    private final SistemaArchivos fs; private final String path; private final String contenido; private boolean ejecutado=false;
    ComandoCrear(SistemaArchivos fs,String path,String contenido){ this.fs=fs; this.path=path; this.contenido=contenido; }
    public String ejecutar(){ if(fs.existe(path)) throw new IllegalStateException("Create: ya existe "+path); fs.escribir(path, contenido); ejecutado=true; return "Create: "+path; }
    public String deshacer(){ if(ejecutado && fs.existe(path)){ fs.eliminar(path); return "Undo Create: "+path+" eliminado"; } return "Undo Create: nada que deshacer"; }
}
class ComandoEliminar implements Comando {
    private final SistemaArchivos fs; private final String path; private String respaldo=null; private boolean tenia=false;
    ComandoEliminar(SistemaArchivos fs,String path){ this.fs=fs; this.path=path; }
    public String ejecutar(){ if(!fs.existe(path)) throw new IllegalStateException("Delete: no existe "+path); respaldo=fs.leer(path); tenia=true; fs.eliminar(path); return "Delete: "+path; }
    public String deshacer(){ if(tenia && !fs.existe(path)){ fs.escribir(path, respaldo); return "Undo Delete: "+path+" restaurado"; } return "Undo Delete: nada que deshacer"; }
}
class ComandoMover implements Comando {
    private final SistemaArchivos fs; private final String src; private final String dst; private boolean movido=false; private String snap=null;
    ComandoMover(SistemaArchivos fs,String src,String dst){ this.fs=fs; this.src=src; this.dst=dst; }
    public String ejecutar(){ if(!fs.existe(src)) throw new IllegalStateException("Move: no existe "+src); if(fs.existe(dst)) throw new IllegalStateException("Move: ya existe "+dst); snap=fs.leer(src); fs.mover(src,dst); movido=true; return "Move: "+src+" -> "+dst; }
    public String deshacer(){
        if(!movido) return "Undo Move: nada que deshacer";
        if(fs.existe(dst)){ fs.mover(dst, src); }
        else { if(fs.existe(src)) return "Undo Move: src ocupado, no se pudo revertir"; fs.escribir(src, snap); }
        return "Undo Move: "+dst+" -> "+src;
    }
}
// ===== Invoker =====
class Invocador {
    private final Deque<Comando> undo = new ArrayDeque<>();
    private final Deque<Comando> redo = new ArrayDeque<>();
    String run(Comando c){ String out = c.ejecutar(); undo.push(c); redo.clear(); return out; }
    String undo(){ if(undo.isEmpty()) return "Nada que deshacer"; Comando c = undo.pop(); String out = c.deshacer(); redo.push(c); return out; }
    String redo(){ if(redo.isEmpty()) return "Nada que rehacer"; Comando c = redo.pop(); String out = c.ejecutar(); undo.push(c); return out; }
}
// ===== Helpers =====
class Console {
    static void estado(String titulo, SistemaArchivos fs){
        System.out.println("================================================================");
        System.out.println(titulo);
        System.out.println("================================================================");
        Map<String,String> m = fs.listar();
        if(m.isEmpty()) System.out.println("(sin archivos)");
        else {
            System.out.printf("%-40s %8s%n", "Ruta", "Tam");
            System.out.println("---------------------------------------------------------------");
            for(Map.Entry<String,String> e : m.entrySet())
                System.out.printf("%-40s %8d%n", e.getKey(), e.getValue().getBytes().length);
        }
        System.out.println("================================================================\n");
    }
}
// ===== Demo =====
public class CommandFileSystemSimulado {
    public static void main(String[] args) {
        SistemaArchivos fs = new SistemaArchivos();
        Invocador inv = new Invocador();

        Console.estado("Estado inicial", fs);
        try {
            System.out.println(inv.run(new ComandoCrear(fs, "/notas.txt", "Hola mundo\nLínea 2")));
            System.out.println(inv.run(new ComandoCrear(fs, "/readme.md", "# Readme\nContenido")));
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        Console.estado("Tras crear", fs);

        try { System.out.println(inv.run(new ComandoMover(fs, "/readme.md", "/README.txt"))); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        Console.estado("Tras mover", fs);

        try { System.out.println(inv.run(new ComandoEliminar(fs, "/notas.txt"))); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        Console.estado("Tras borrar", fs);

        System.out.println(inv.undo());
        System.out.println(inv.undo());
        Console.estado("Tras UNDO x2", fs);

        System.out.println(inv.redo());
        System.out.println(inv.redo());
        Console.estado("Tras REDO x2", fs);
    }
}
