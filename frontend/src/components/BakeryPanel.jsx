import React, { useEffect, useState } from "react";
import { api } from "../api/client";
import { queueBand } from "../api/queueBand";

export default function BakeryPanel({ user, version, onChanged }) {
    const [bakeries, setBakeries] = useState([]);
    const [selected, setSelected] = useState(null);
    const [menu, setMenu] = useState([]);
    const [qty, setQty] = useState({});

    const loadBakeries = () => api("GET", "/api/bakeries").then(setBakeries).catch(() => {});
    useEffect(() => { loadBakeries(); }, [version]);

    async function loadMenu(id) {
        setSelected(id);
        try { setMenu(await api("GET", `/api/bakeries/${id}/menu`)); }
        catch { setMenu([]); }
    }

    async function place() {
        const lines = Object.entries(qty)
            .filter(([, q]) => q > 0)
            .map(([menuItemId, quantity]) => ({ menuItemId, quantity }));
        try {
            await api("POST", "/api/orders", { bakeryId: selected, lines });
            setQty({});
            onChanged();
        } catch {}
    }

    async function bakeryAction(id, action) {
        try { await api("POST", `/api/bakeries/${id}/${action}`); } catch {}
        loadBakeries();
    }

    async function toggleSoldOut(item) {
        const status = item.status === "AVAILABLE" ? "SOLD_OUT" : "AVAILABLE";
        try { await api("PUT", `/api/bakeries/${selected}/menu/${item.id}`, { status }); } catch {}
        loadMenu(selected);
    }

    const canManage = (b) =>
        user.role === "OPERATOR" || (user.role === "BAKERY_STAFF" && user.bakeryId === b.id);

    return (
        <div>
            <h3>Bakeries</h3>
            {bakeries.map((b) => (
                <div key={b.id} style={{ marginBottom: 6 }}>
                    <b>{b.name}</b> [{b.status}]{" "}
                    <span style={{ background: queueBand(b.queueLength).color, color: "#fff",
                        borderRadius: 10, padding: "1px 8px" }}>queue {b.queueLength}</span>{" "}
                    <button onClick={() => loadMenu(b.id)}>Menu</button>
                    {canManage(b) && b.status === "OPEN" &&
                        <button onClick={() => bakeryAction(b.id, "pause")}>Pause</button>}
                    {user.role === "OPERATOR" && b.status !== "OPEN" &&
                        <button onClick={() => bakeryAction(b.id, "reopen")}>Reopen</button>}
                </div>
            ))}

            {selected && (
                <div>
                    <h3>Menu ({selected})</h3>
                    {menu.map((m) => (
                        <div key={m.id}>
                            {m.name} ${m.price.toFixed(2)} [{m.status}]{" "}
                            {user.role === "CUSTOMER" && (
                                <input type="number" min="0" style={{ width: 50 }}
                                    value={qty[m.id] || ""}
                                    onChange={(e) => setQty({ ...qty, [m.id]: Number(e.target.value) })} />
                            )}
                            {user.role === "BAKERY_STAFF" && user.bakeryId === selected && (
                                <button onClick={() => toggleSoldOut(m)}>Toggle sold out</button>
                            )}
                        </div>
                    ))}
                    {user.role === "CUSTOMER" && menu.length > 0 &&
                        <button onClick={place}>Place order</button>}
                </div>
            )}
        </div>
    );
}