import React, { useEffect, useState } from "react";
import { api } from "../api/client";

const FINISHED = ["READY", "REJECTED", "CANCELLED"];

export default function OrderPanel({ user, version, onChanged }) {
    const [orders, setOrders] = useState([]);
    const [summaries, setSummaries] = useState({});

    const load = () => {
    if (user.role === "COURIER") return;
    return api("GET", "/api/orders").then(setOrders).catch(() => {});
    };

    async function act(id, action, body) {
        try { await api("POST", `/api/orders/${id}/${action}`, body); } catch {}
        onChanged();
    }

    function reject(id) {
        const reason = window.prompt("Reason for rejection?");
        if (reason) act(id, "reject", { reason });
    }

async function viewFinalSummary(id) {
    try {
        const summary = await api(
            "GET",
            `/api/orders/${id}/final-summary`
        );

        setSummaries((prev) => ({
            ...prev,
            [id]: typeof summary === "string"
                ? JSON.parse(summary)
                : summary
        }));
    } catch (error) {
        alert("Unable to load final order summary.");
    }
}

    return (
        <div>
            <h3>Orders <button onClick={load}>Refresh</button></h3>
            {user.role === "COURIER" && <p>Deliveries are not part of this phase.</p>}
            {user.role !== "COURIER" && orders.length === 0 && <p>No orders yet.</p>}
            {orders.map((o) => (
                <div key={o.orderId} style={{ border: "1px solid #ccc", padding: 6, marginBottom: 6 }}>
                    <b>{o.orderId}</b> at {o.bakeryId} - <b>{o.status}</b> - ${o.total.toFixed(2)}
                    <div>
                        {o.lines.map((l) => `${l.quantity} x ${l.name}`).join(", ")}
                        {o.rejectionReason && ` (rejected: ${o.rejectionReason})`}
                    </div>
                    {user.role === "CUSTOMER" && !FINISHED.includes(o.status) &&
                        <button onClick={() => act(o.orderId, "cancel")}>Cancel</button>}
                        
{user.role === "CUSTOMER" && o.status === "DELIVERED" && (
    <>
        <button onClick={() => viewFinalSummary(o.orderId)}>
            View Final Summary
        </button>

        {summaries[o.orderId] && (
            <div style={{
                border: "1px solid #aaa",
                padding: 10,
                marginTop: 8
            }}>
                <h4>Final Order Summary</h4>
                <pre style={{ whiteSpace: "pre-wrap" }}>
                    {JSON.stringify(summaries[o.orderId], null, 2)}
                </pre>
            </div>
        )}
    </>
)}

                    {user.role === "BAKERY_STAFF" && !FINISHED.includes(o.status) && (
                        <>
                            <button onClick={() => act(o.orderId, "accept")}>Accept</button>
                            <button onClick={() => reject(o.orderId)}>Reject</button>
                            <button onClick={() => act(o.orderId, "prepare")}>Prepare</button>
                            <button onClick={() => act(o.orderId, "ready")}>Mark ready</button>
                        </>
                    )}
                </div>
            ))}
        </div>
    );
}