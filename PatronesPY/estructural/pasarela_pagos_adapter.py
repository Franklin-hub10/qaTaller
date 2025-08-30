"""
Adapter — Pasarela de Pagos (simple, consola)

Qué muestra:
- "Clientes" de terceros con APIs distintas (simulados)
- Adaptadores que los hacen compatibles con una interfaz común: PaymentGateway.pay(monto, moneda)
- Un procesador que los usa sin saber qué proveedor hay detrás

Ejecución:
    python adapter_pasarela_pagos_simple.py
"""

from __future__ import annotations
from abc import ABC, abstractmethod

# ---------------- Simulación de SDKs de terceros (APIs diferentes) ----------------

class StripeClient:
    # Cobra en CENTAVOS y pide currency en MAYÚSCULA
    def create_charge(self, amount_cents: int, currency: str, metadata: dict | None = None) -> str:
        if amount_cents <= 0:
            raise ValueError("Stripe: amount must be > 0")
        return f"stripe_tx_{amount_cents}_{currency}"

class PaypalClient:
    # Cobra con enteros (redondea) y moneda en minúsculas
    def make_payment(self, total: float, curr: str) -> str:
        if total <= 0:
            raise ValueError("PayPal: total inválido")
        return f"paypal_tx_{int(round(total))}_{curr}"

class BankClient:
    # Solo USD, método diferente y sin currency explícita
    def transfer_usd(self, amount_usd: float) -> str:
        if amount_usd <= 0:
            raise ValueError("Bank: monto inválido")
        return f"bank_ref_{int(amount_usd*100)}"


# ---------------- Interfaz común (lo que usa tu app) ----------------

class PaymentGateway(ABC):
    @abstractmethod
    def pay(self, amount: float, currency: str) -> str:
        """Procesa el pago y retorna una referencia/recibo."""


# ---------------- Adaptadores concretos ----------------

class StripeAdapter(PaymentGateway):
    def __init__(self, client: StripeClient | None = None):
        self.client = client or StripeClient()

    def pay(self, amount: float, currency: str) -> str:
        cents = int(round(amount * 100))
        curr = currency.upper()
        return self.client.create_charge(cents, curr, metadata=None)

class PaypalAdapter(PaymentGateway):
    SUPPORTED = {"usd", "eur", "gbp"}
    def __init__(self, client: PaypalClient | None = None):
        self.client = client or PaypalClient()

    def pay(self, amount: float, currency: str) -> str:
        curr = currency.lower()
        if curr not in self.SUPPORTED:
            raise ValueError(f"PayPal no soporta {currency}")
        return self.client.make_payment(amount, curr)

class BankAdapter(PaymentGateway):
    def __init__(self, client: BankClient | None = None):
        self.client = client or BankClient()

    def pay(self, amount: float, currency: str) -> str:
        if currency.upper() != "USD":
            raise ValueError("Banco: solo USD")
        return self.client.transfer_usd(amount)


# ---------------- Fábrica mínima (para el demo) ----------------

def get_gateway(name: str) -> PaymentGateway:
    name = (name or "").lower()
    if name == "stripe": return StripeAdapter()
    if name == "paypal": return PaypalAdapter()
    if name == "bank":   return BankAdapter()
    raise ValueError(f"Proveedor desconocido: {name}")


# ---------------- Helper de impresión ----------------

def print_result(title: str, ok: bool, msg: str):
    line = "─" * 60
    print(line)
    print(f"{title}")
    print(line)
    print(("✅ " if ok else "❌ ") + msg)
    print()


# ---------------- Demo de consola ----------------

def demo():
    casos = [
        ("stripe", 25.00, "USD"),
        ("paypal", 15.75, "EUR"),
        ("bank",   10.00, "USD"),
        # caso de error (moneda no soportada por bank)
        ("bank",   10.00, "EUR"),
    ]
    for proveedor, monto, moneda in casos:
        try:
            gw = get_gateway(proveedor)
            ref = gw.pay(monto, moneda)
            print_result(f"{proveedor.upper()} — {monto} {moneda}", True, f"Pago exitoso. Ref: {ref}")
        except Exception as e:
            print_result(f"{proveedor.upper()} — {monto} {moneda}", False, f"{e}")

if __name__ == "__main__":
    demo()
