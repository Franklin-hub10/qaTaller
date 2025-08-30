package estructural;

/**
 * Adapter — Pasarela de pagos (Java)
 * Equivalente del Python "pasarela_pagos_adapter.py".
 * Ejecutar:
 *   javac estructural/AdapterPasarelaPagosSimple.java
 *   java estructural.AdapterPasarelaPagosSimple
 */
// --- SDKs simulados ---
class StripeClient {
    String createCharge(int amountCents, String currency) {
        if (amountCents <= 0) throw new IllegalArgumentException("Stripe: amount > 0");
        return "stripe_tx_" + amountCents + "_" + currency;
    }
}
class PaypalClient {
    String makePayment(double total, String curr) {
        if (total <= 0) throw new IllegalArgumentException("PayPal: total inválido");
        return "paypal_tx_" + Math.round(total) + "_" + curr;
    }
}
class BankClient {
    String transferUsd(double amountUsd) {
        if (amountUsd <= 0) throw new IllegalArgumentException("Bank: monto inválido");
        return "bank_ref_" + (int)(amountUsd * 100);
    }
}
// --- Interfaz común ---
interface PaymentGateway { String pay(double amount, String currency) throws Exception; }
// --- Adaptadores ---
class StripeAdapter implements PaymentGateway {
    public String pay(double amount, String currency) {
        return new StripeClient().createCharge((int)Math.round(amount*100), currency.toUpperCase());
    }
}
class PaypalAdapter implements PaymentGateway {
    public String pay(double amount, String currency) {
        String curr = currency.toLowerCase();
        if (!curr.equals("usd") && !curr.equals("eur") && !curr.equals("gbp"))
            throw new IllegalArgumentException("PayPal no soporta " + currency);
        return new PaypalClient().makePayment(amount, curr);
    }
}
class BankAdapter implements PaymentGateway {
    public String pay(double amount, String currency) {
        if (!"USD".equalsIgnoreCase(currency))
            throw new IllegalArgumentException("Banco: solo USD");
        return new BankClient().transferUsd(amount);
    }
}

public class AdapterPasarelaPagosSimple {
    private static void caso(String prov, double monto, String moneda, PaymentGateway gw) {
        System.out.println("------------------------------------------------------------");
        System.out.println(prov.toUpperCase() + " — " + monto + " " + moneda);
        try {
            String ref = gw.pay(monto, moneda);
            System.out.println("OK  Pago exitoso. Ref: " + ref);
        } catch (Exception e) {
            System.out.println("ERR " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        caso("stripe", 25.0, "USD", new StripeAdapter());
        caso("paypal", 15.75, "EUR", new PaypalAdapter());
        caso("bank",   10.0, "USD", new BankAdapter());
        caso("bank",   10.0, "EUR", new BankAdapter()); // error
    }
}
