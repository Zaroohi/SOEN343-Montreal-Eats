
import React, { useEffect, useState } from "react";
import { api } from "../api/client";

export default function OrderHistory({ user, version }) {
    const [history, setHistory] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let active = true;
        setLoading(true);

        api("GET", "/api/orders/history", undefined, true)
            .then((data) => {
                if (!active) return;
                setHistory(data);
                setError("");
            })
            .catch(() => {
                if (active) setError("Unable to load order history.");
            })
            .finally(() => {
                if (active) setLoading(false);
            });

        return () => { active = false; };
    }, [user.id, version]);

    const formatDate = (date) =>
        date ? new Date(date).toLocaleString("en-CA", {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }) : "—";

    const styles = {
        section: {
            marginTop: 30,
            padding: 24,
            background: "#f8fafc",
            borderRadius: 12,
            border: "1px solid #e2e8f0"
        },
        card: {
            background: "white",
            border: "1px solid #e2e8f0",
            borderRadius: 10,
            padding: 20,
            marginTop: 14,
            boxShadow: "0 2px 6px rgba(0,0,0,0.04)"
        },
        badge: {
            background: "#dcfce7",
            color: "#166534",
            borderRadius: 20,
            padding: "5px 12px",
            fontSize: 12,
            fontWeight: 600
        },
        label: {
            color: "#64748b",
            fontSize: 13,
            marginBottom: 5
        }
    };

    return (
        <section style={styles.section}>
            <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
            }}>
                <div>
                    <h2 style={{ margin: 0 }}>Order History</h2>
                    <p style={{ color: "#64748b", marginTop: 6 }}>
                        View your completed orders and delivery details
                    </p>
                </div>
                <span style={{ fontSize: 28 }}>📦</span>
            </div>

            {loading && <p>Loading order history...</p>}
            {error && <p style={{ color: "#dc2626" }}>{error}</p>}

            {!loading && !error && history.length === 0 && (
                <p style={{ color: "#64748b" }}>
                    No completed orders yet.
                </p>
            )}

            {!loading && !error && history.map((entry) => (
                <div key={entry.orderId} style={styles.card}>
                    <div style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: 12,
                        flexWrap: "wrap"
                    }}>
                        <strong style={{ fontSize: 17 }}>
                            {entry.orderId}
                        </strong>
                        <span style={styles.badge}>
                            ✓ {entry.finalState}
                        </span>
                    </div>

                    <div style={{
                        display: "grid",
                        gridTemplateColumns:
                            "repeat(auto-fit, minmax(170px, 1fr))",
                        gap: 20,
                        marginTop: 22,
                        marginBottom: 18
                    }}>
                        <div>
                            <div style={styles.label}>Placed on</div>
                            <strong>{formatDate(entry.createdAt)}</strong>
                        </div>
                        <div>
                            <div style={styles.label}>Delivered on</div>
                            <strong>{formatDate(entry.deliveredAt)}</strong>
                        </div>
                    </div>

                    {user.role !== "COURIER" &&
                        entry.totalCharge != null && (
                            <div style={{
                                background: "#f1f5f9",
                                borderRadius: 8,
                                padding: "14px 16px",
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center"
                            }}>
                                <span style={{ color: "#475569" }}>
                                    Total Charge
                                </span>
                                <strong style={{ fontSize: 20 }}>
                                    ${entry.totalCharge.toFixed(2)}
                                </strong>
                            </div>
                        )}
                </div>
            ))}
        </section>
    );
}
