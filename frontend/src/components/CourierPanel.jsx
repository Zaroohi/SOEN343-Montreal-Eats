import React, { useEffect, useState } from "react";
import { api } from "../api/client";

export default function CourierPanel({ onChanged }) {
    const [me, setMe] = useState(null);

    const load = (quiet = false) =>
        api("GET", "/api/delivery/me", undefined, quiet).then(setMe).catch(() => {});

    useEffect(() => {
        load();
        const t = setInterval(() => load(true), 4000);
        return () => clearInterval(t);
    }, []);

    async function act(path, body) {
        try { await api("POST", `/api/delivery/${path}`, body); } catch {}
        load(true);
        onChanged();
    }

    if (!me) return <p>Loading courier status...</p>;

    return (
        <div>
            <h3>Courier <button onClick={() => load()}>Refresh</button></h3>
            <p>Status: <b>{me.status}</b></p>

            {me.status === "OFFLINE"
                ? <button onClick={() => act("duty", { onDuty: true })}>Go on duty</button>
                : <button onClick={() => act("duty", { onDuty: false })}>Go off duty</button>}

            {me.offeredOrderId && (
                <div style={{ border: "1px solid #c90", padding: 6, marginTop: 10 }}>
                    <b>Offer:</b> order {me.offeredOrderId}, pickup at bakery {me.offeredBakeryId}{" "}
                    <button onClick={() => act("accept")}>Accept</button>
                    <button onClick={() => act("decline")}>Decline</button>
                </div>
            )}

            {me.currentOrderId && (
                <div style={{ border: "1px solid #3a3", padding: 6, marginTop: 10 }}>
                    <b>Your delivery:</b> order {me.currentOrderId}, pickup at bakery {me.currentBakeryId}{" "}
                    <button onClick={() => act("pickup")}>Confirm pickup</button>
                    <button onClick={() => act("deliver")}>Confirm delivery</button>
                </div>
            )}

            {!me.offeredOrderId && !me.currentOrderId && me.status !== "OFFLINE" &&
                <p>Waiting for an order...</p>}
        </div>
    );
}