# command_filesystem_simulado.py
# Ejecuta:  python command_filesystem_simulado.py

# ========== Receiver: FileSystem (en memoria) ==========

class FileSystem:
    def __init__(self):
        # Representación simple: {ruta: contenido}
        self._files = {}

    def exists(self, path: str) -> bool:
        return path in self._files

    def write(self, path: str, content: str):
        self._files[path] = content

    def read(self, path: str) -> str:
        if path not in self._files:
            raise ValueError(f"No existe: {path}")
        return self._files[path]

    def delete(self, path: str):
        if path not in self._files:
            raise ValueError(f"No existe: {path}")
        del self._files[path]

    def move(self, src: str, dst: str):
        if src not in self._files:
            raise ValueError(f"No existe: {src}")
        if dst in self._files:
            raise ValueError(f"Ya existe destino: {dst}")
        self._files[dst] = self._files[src]
        del self._files[src]

    def list_files(self):
        return sorted(self._files.items())


# ========== Interfaz Command ==========

class Command:
    def execute(self) -> str:
        raise NotImplementedError
    def undo(self) -> str:
        raise NotImplementedError


# ========== Commands concretos ==========

class CreateCommand(Command):
    def __init__(self, fs: FileSystem, path: str, content: str):
        self.fs = fs
        self.path = path
        self.content = content
        self._executed = False

    def execute(self) -> str:
        if self.fs.exists(self.path):
            raise ValueError(f"Create: ya existe {self.path}")
        self.fs.write(self.path, self.content)
        self._executed = True
        return f"Create: {self.path}"

    def undo(self) -> str:
        if self._executed and self.fs.exists(self.path):
            self.fs.delete(self.path)
            return f"Undo Create: {self.path} eliminado"
        return "Undo Create: nada que deshacer"


class DeleteCommand(Command):
    def __init__(self, fs: FileSystem, path: str):
        self.fs = fs
        self.path = path
        self._backup = None
        self._had = False

    def execute(self) -> str:
        if not self.fs.exists(self.path):
            raise ValueError(f"Delete: no existe {self.path}")
        self._backup = self.fs.read(self.path)
        self._had = True
        self.fs.delete(self.path)
        return f"Delete: {self.path}"

    def undo(self) -> str:
        if self._had and not self.fs.exists(self.path):
            self.fs.write(self.path, self._backup)
            return f"Undo Delete: {self.path} restaurado"
        return "Undo Delete: nada que deshacer"


class MoveCommand(Command):
    def __init__(self, fs: FileSystem, src: str, dst: str):
        self.fs = fs
        self.src = src
        self.dst = dst
        self._moved = False
        self._snapshot = None  # contenido al momento de mover

    def execute(self) -> str:
        if not self.fs.exists(self.src):
            raise ValueError(f"Move: no existe {self.src}")
        if self.fs.exists(self.dst):
            raise ValueError(f"Move: ya existe {self.dst}")
        self._snapshot = self.fs.read(self.src)
        self.fs.move(self.src, self.dst)
        self._moved = True
        return f"Move: {self.src} → {self.dst}"

    def undo(self) -> str:
        if self._moved:
            # Si el destino desapareció por otra operación, restauramos igual
            if self.fs.exists(self.dst):
                # mover de vuelta
                self.fs.move(self.dst, self.src)
            else:
                # restaurar a src con el contenido original
                if self.fs.exists(self.src):
                    # si algo ocupa src, no sobrescribimos (simple para demo)
                    return "Undo Move: src ocupado, no se pudo revertir"
                self.fs.write(self.src, self._snapshot)
            return f"Undo Move: {self.dst} → {self.src}"
        return "Undo Move: nada que deshacer"


# ========== Invoker con pilas undo/redo ==========

class Invoker:
    def __init__(self):
        self._undo_stack = []
        self._redo_stack = []

    def run(self, cmd: Command) -> str:
        out = cmd.execute()
        self._undo_stack.append(cmd)
        self._redo_stack.clear()
        return out

    def undo(self) -> str:
        if not self._undo_stack:
            return "Nada que deshacer"
        cmd = self._undo_stack.pop()
        out = cmd.undo()
        self._redo_stack.append(cmd)
        return out

    def redo(self) -> str:
        if not self._redo_stack:
            return "Nada que rehacer"
        cmd = self._redo_stack.pop()
        out = cmd.execute()
        self._undo_stack.append(cmd)
        return out


# ========== Helpers de impresión ==========

WIDTH = 72
def line(ch="-"):
    print(ch * WIDTH)

def print_state(title: str, fs: FileSystem):
    line("=")
    print(title)
    line("=")
    files = fs.list_files()
    if not files:
        print("(sin archivos)")
    else:
        print("Ruta".ljust(40), "Tamaño".rjust(8))
        print("-" * 50)
        for path, content in files:
            size = len(content.encode("utf-8"))
            print(path.ljust(40), str(size).rjust(8))
    line("=")
    print()


# ========== Demo de consola ==========

def demo():
    fs = FileSystem()
    inv = Invoker()

    print_state("Estado inicial", fs)

    # 1) Crear archivos
    try:
        print(inv.run(CreateCommand(fs, "/notas.txt", "Hola mundo\nLínea 2")))
        print(inv.run(CreateCommand(fs, "/readme.md", "# Readme\nContenido")))
    except Exception as e:
        print("Error:", e)
    print_state("Tras crear /notas.txt y /readme.md", fs)

    # 2) Mover archivo
    try:
        print(inv.run(MoveCommand(fs, "/readme.md", "/README.txt")))
    except Exception as e:
        print("Error:", e)
    print_state("Tras mover /readme.md → /README.txt", fs)

    # 3) Borrar archivo
    try:
        print(inv.run(DeleteCommand(fs, "/notas.txt")))
    except Exception as e:
        print("Error:", e)
    print_state("Tras borrar /notas.txt", fs)

    # 4) Undo dos veces (revierte delete y move)
    print(inv.undo())
    print(inv.undo())
    print_state("Tras UNDO (x2)", fs)

    # 5) Redo dos veces (reaplica move y delete)
    print(inv.redo())
    print(inv.redo())
    print_state("Tras REDO (x2)", fs)

    # 6) Errores controlados (ejemplos)
    try:
        print(inv.run(CreateCommand(fs, "/README.txt", "otro")))  # ya existe
    except Exception as e:
        print("Error:", e)
    try:
        print(inv.run(MoveCommand(fs, "/no_existe.txt", "/nuevo.txt")))
    except Exception as e:
        print("Error:", e)

if __name__ == "__main__":
    demo()
