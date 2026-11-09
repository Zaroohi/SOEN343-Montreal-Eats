import React, { useEffect, useState } from "react";
import { api } from "../api/client";

export default function OperatorDeliveryPanel({ version, onChanged }) {
    const [couriers, setCouriers] = useState([]);
    const [queue, setQueue] = useState([]);
    const [stalled, setStalled] = useState([]);

    const load = (quiet = false) => {
        api("GET", "/api/delivery/couriers", undefined, quiet).then(setCouriers).catch(() => {});
        api("GET", "/api/delivery/queue", undefined, quiet).then(setQueue).catch(() => {});
        api("GET", "/api/delivery/stalled", undefined, quiet).then(setStalled).catch(() => {});
    };

    useEffect(() => {
        load(true);
        const t = setInterval(() => load(true), 4000);
        return () => clearInterval(t);
    }, [version]);

    async function reassign(orderId) {
        try { await api("POST", `/api/delivery/orders/${orderId}/reassign`); } catch {}
        load(true);
        onChanged();
    }

    return (
        <div>
            <h3>Delivery <button onClick={() => load()}>Refresh</button></h3>

            <b>Couriers</b>
            {couriers.map((c) => (
                <div key={c.courierId}>
                    {c.courierId}: {c.status}
                    {c.offeredOrderId && ` (offered ${c.offeredOrderId})`}
                    {c.currentOrderId && ` (holds ${c.currentOrderId})`}
                </div>
            ))}

            <div style={{ marginTop: 8 }}><b>Dispatch queue</b></div>
            {queue.length === 0 ? <div>Empty</div> : queue.map((id) => <div key={id}>{id} waiting for a courier</div>)}

            <div style={{ marginTop: 8 }}><b>Stalled orders</b></div>
            {stalled.length === 0 ? <div>None</div> : stalled.map((o) => (
                <div key={o.orderId}>
                    {o.orderId} at {o.bakeryId}{" "}
                    <button onClick={() => reassign(o.orderId)}>Reassign</button>
                </div>
            ))}
        </div>
    );
}