import React, { useState } from "react";
import { api } from "../api/client";
import BakeryPanel from "./BakeryPanel";
import OrderPanel from "./OrderPanel";
import OutputConsole from "./OutputConsole";
import CourierPanel from "./CourierPanel";
import OperatorDeliveryPanel from "./OperatorDeliveryPanel";
import OrderHistory from "./OrderHistory";
import CityMap from "./CityMap";

export default function DashboardView({ user, onLogout }) {
    const [version, setVersion] = useState(0);
    const [resetKey, setResetKey] = useState(0);
    const refresh = () => setVersion((v) => v + 1);

    async function resetSystem() {
        if (!window.confirm("Reset the whole system to its initial state? All orders, events and courier activity will be erased.")) return;
        try {
            await api("POST", "/api/reset");
            setResetKey((k) => k + 1);   // remounts the bakery panel so a stale open menu disappears
            refresh();
        } catch {}
    }

    return (
        <>
            <header className="topbar">
                <div className="brand">🥐 Baked To Go</div>
                <div className="userchip">
                    <span>{user.name}</span>
                    <span className="role">
                        {user.role.replace("_", " ")}{user.bakeryId ? ` · ${user.bakeryId}` : ""}
                    </span>
                    {user.role === "OPERATOR" && <button onClick={resetSystem}>Reset system</button>}
                    <button onClick={onLogout}>Log out</button>
                </div>
            </header>

            <main className="page">
                <section className="card">
                    <h3 className="section-title">City map</h3>
                    <CityMap user={user} version={version} />
                </section>

                <div className="grid">
                    <section className="card">
                        <BakeryPanel key={resetKey} user={user} version={version} onChanged={refresh} />
                    </section>
                    <section className="card">
                        {user.role !== "COURIER" && <OrderPanel user={user} version={version} onChanged={refresh} />}
                        {user.role === "COURIER" && <CourierPanel onChanged={refresh} />}
                        {user.role === "OPERATOR" && (
                            <div style={{ marginTop: 20 }}>
                                <OperatorDeliveryPanel version={version} onChanged={refresh} />
                            </div>
                        )}
                    </section>
                </div>

                <OrderHistory user={user} version={version} />

                <section className="card" style={{ marginTop: 20 }}>
                    <h3 className="section-title">Output console</h3>
                    <OutputConsole />
                </section>
            </main>
        </>
    );
}