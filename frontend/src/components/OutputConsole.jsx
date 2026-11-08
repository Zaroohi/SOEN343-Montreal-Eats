import React, { useSyncExternalStore } from "react";
import { subscribe, getLines } from "../api/client";

const colors = { cmd: "#9cdcfe", ok: "#6a9955", error: "#f48771" };

export default function OutputConsole() {
    const lines = useSyncExternalStore(subscribe, getLines);
    return (
        <div style={{ background: "#1e1e1e", padding: 10, height: 220, overflowY: "auto",
                      fontFamily: "monospace", fontSize: 14 }}>
            {lines.map((l, i) => (
                <div key={i} style={{ color: colors[l.kind] }}>
                    [{l.time}] {l.kind === "cmd" ? "> " : ""}{l.text}
                </div>
            ))}
        </div>
    );
}