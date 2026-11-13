import React, { useState } from "react";
import BakeryPanel from "./BakeryPanel";
import OrderPanel from "./OrderPanel";
import OutputConsole from "./OutputConsole";
import CourierPanel from "./CourierPanel";
import OperatorDeliveryPanel from "./OperatorDeliveryPanel";
<<<<<<< Updated upstream
=======
import OrderHistory from "./OrderHistory";
import CityMap from "./CityMap";
>>>>>>> Stashed changes

export default function DashboardView({ user, onLogout }) {
    const [version, setVersion] = useState(0);
    const refresh = () => setVersion((v) => v + 1);

    return (
        <>
            <header className="topbar">
                <div className="brand">🥐 Baked To Go</div>
                <div className="userchip">
                    <span>{user.name}</span>
                    <span className="role">
                        {user.role.replace("_", " ")}{user.bakeryId ? ` · ${user.bakeryId}` : ""}
                    </span>
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
                        <BakeryPanel user={user} version={version} onChanged={refresh} />
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
<<<<<<< Updated upstream
            </div>
            <h3>Output console</h3>
            <OutputConsole />
        </div>
=======

                <OrderHistory user={user} version={version} />

                <section className="card" style={{ marginTop: 20 }}>
                    <h3 className="section-title">Output console</h3>
                    <OutputConsole />
                </section>
            </main>
        </>
>>>>>>> Stashed changes
    );
}