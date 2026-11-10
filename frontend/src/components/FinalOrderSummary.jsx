import React from "react";

export default function FinalOrderSummary({ summary }) {
    const money = (value) => `$${Number(value ?? 0).toFixed(2)}`;

    return (
        <div style={{
            maxWidth: 400,
            padding: 20,
            marginTop: 12,
            border: "1px solid #ddd",
            borderRadius: 12,
            backgroundColor: "#fff",
            boxShadow: "0 3px 12px rgba(0,0,0,0.08)"
        }}>
            <h3 style={{ marginTop: 0 }}>BakedToGo</h3>
            <h4>Final Order Summary</h4>

            <p><strong>Order:</strong> {summary.order}</p>
            <p><strong>Bakery:</strong> {summary.bakery}</p>
            <p><strong>Status:</strong> {summary.status}</p>

            <hr />

            <h4>Items</h4>
            {summary.items?.map((item, index) => (
                <p key={index}>{item}</p>
            ))}

            <hr />

            <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 10 }}>
                <span>Subtotal</span>
                <span>{money(summary.subtotal)}</span>

                <span>Discount</span>
                <span>-{money(summary.discount)}</span>

                <span>Delivery fee</span>
                <span>{money(summary.deliveryFee)}</span>

                <span>Service fee</span>
                <span>{money(summary.serviceFee)}</span>

                <span>Small-order surcharge</span>
                <span>{money(summary.smallOrderSurcharge)}</span>
            </div>

            <hr />

            <div style={{
                display: "flex",
                justifyContent: "space-between",
                fontSize: 20,
                fontWeight: "bold"
            }}>
                <span>Total</span>
                <span>{money(summary.total)}</span>
            </div>

            <p style={{
                marginTop: 20,
                padding: 10,
                textAlign: "center",
                backgroundColor: "#e8f5e9",
                borderRadius: 8,
                color: "#bcb5e7"
            }}>
                ✓ Your order has been delivered!
            </p>
        </div>
    );
}
