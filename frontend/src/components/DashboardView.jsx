import React, { useState } from "react";
import BakeryPanel from "./BakeryPanel";
import OrderPanel from "./OrderPanel";
import OutputConsole from "./OutputConsole";

export default function DashboardView({ user, onLogout }) {
    const [version, setVersion] = useState(0);
    const refresh = () => setVersion((v) => v + 1);

    return (
        <div style={{ fontFamily: "Arial", padding: 20 }}>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
                <h2>Baked To Go</h2>
                <div>
                    {user.name} ({user.role}{user.bakeryId ? `, ${user.bakeryId}` : ""}){" "}
                    <button onClick={onLogout}>Log out</button>
                </div>
            </div>
            <div style={{ display: "flex", gap: 30 }}>
                <div style={{ flex: 1 }}><BakeryPanel user={user} onChanged={refresh} /></div>
                <div style={{ flex: 1 }}><OrderPanel user={user} version={version} onChanged={refresh} /></div>
            </div>
            <h3>Output console</h3>
            <OutputConsole />
        </div>
    );
}