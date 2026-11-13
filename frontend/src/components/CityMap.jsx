import React, { useEffect, useRef, useState } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { api } from "../api/client";
import { queueBand } from "../api/queueBand";
import { fetchRoute, positionAlong } from "../api/routing";

const COURIER_COLORS = { OFFLINE: "#999", IDLE: "#2a7de1", ASSIGNED: "#e67e22", CARRYING: "#8e44ad" };
const ROUTE_STYLE = {
    pickup: { color: "#e67e22", dashArray: null },
    offer: { color: "#e67e22", dashArray: "8 8" },
    delivery: { color: "#8e44ad", dashArray: null },
};
const LABEL = {
    pickup: "heading to the bakery",
    delivery: "delivering to the customer",
    offer: "offer pending",
};

// Demo speed: 10 m/s (about 36 km/h) played back 10x faster, so a 3 km trip takes about 30 s.
const REAL_SPEED_MPS = 10;
const TIME_SCALE = 10;

const esc = (s) =>
    String(s ?? "").replace(/[&<>"']/g, (c) =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

const icon = (html, size) =>
    L.divIcon({ className: "bt-marker", html, iconSize: [size, size], iconAnchor: [size / 2, size / 2] });

const courierIcon = (status, mine) =>
    icon(`<div class="bt-courier${mine ? " mine" : ""}" style="background:${COURIER_COLORS[status] || "#999"}">🛵</div>`, 32);

export default function CityMap({ user, version }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState("");
    const boxRef = useRef(null);
    const mapRef = useRef(null);
    const staticLayerRef = useRef(null);
    const courierLayerRef = useRef(null);
    const couriersRef = useRef(new Map());
    const fittedRef = useRef(false);

    // Create the map once.
    useEffect(() => {
        const map = L.map(boxRef.current).setView([45.5155, -73.59], 13);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution:
                '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 19,
        }).addTo(map);
        staticLayerRef.current = L.layerGroup().addTo(map);
        courierLayerRef.current = L.layerGroup().addTo(map);
        mapRef.current = map;
        const t = setTimeout(() => map.invalidateSize(), 100);
        return () => {
            clearTimeout(t);
            map.remove();
            mapRef.current = null;
            couriersRef.current = new Map();
            fittedRef.current = false;
        };
    }, []);

    // Fetch the role-scoped map data (quietly, so the console is not flooded).
    useEffect(() => {
        const load = () =>
            api("GET", "/api/map", undefined, true)
                .then((d) => { setData(d); setError(""); })
                .catch((e) => setError(e.message));
        load();
        const t = setInterval(load, 3000);
        return () => clearInterval(t);
    }, [version]);

    // Bakeries and customers (static markers), and the first fit of the view.
    useEffect(() => {
        const map = mapRef.current;
        const layer = staticLayerRef.current;
        if (!map || !layer || !data) return;
        layer.clearLayers();
        const pts = [];

        data.bakeries.forEach((b) => {
            const open = b.status === "OPEN";
            const color = open ? queueBand(b.queueLength).color : "#888";
            const mine = user.bakeryId === b.id ? " mine" : "";
            L.marker([b.lat, b.lon], {
                icon: icon(`<div class="bt-bakery${mine}" style="background:${color}"><span>🥐</span><b>${b.queueLength}</b></div>`, 36),
            })
                .bindTooltip(`<b>${esc(b.name)}</b><br>${esc(b.status)} · queue ${b.queueLength}`,
                    { direction: "top", offset: [0, -20] })
                .addTo(layer);
            pts.push([b.lat, b.lon]);
        });

        data.customers.forEach((c) => {
            const mine = c.id === user.id ? " mine" : "";
            L.marker([c.lat, c.lon], { icon: icon(`<div class="bt-customer${mine}">🏠</div>`, 30) })
                .bindTooltip(`<b>${esc(c.name)}</b><br>customer`, { direction: "top", offset: [0, -16] })
                .addTo(layer);
            pts.push([c.lat, c.lon]);
        });

        data.couriers.forEach((c) => pts.push([c.lat, c.lon]));

        // Fit once, so panning and zooming is not undone by the refresh.
        if (!fittedRef.current && pts.length > 0) {
            map.fitBounds(L.latLngBounds(pts).pad(0.3), { maxZoom: 15 });
            fittedRef.current = true;
        }
    }, [data, user]);

    // Couriers: keep one marker and one route line each, and fetch street routes when a trip changes.
    useEffect(() => {
        const layer = courierLayerRef.current;
        if (!layer || !data) return;
        const known = couriersRef.current;
        const seen = new Set();

        data.couriers.forEach((c) => {
            seen.add(c.id);
            const mine = c.id === user.id;
            let info = known.get(c.id);
            if (!info) {
                const marker = L.marker([c.lat, c.lon], { icon: courierIcon(c.status, mine) }).addTo(layer);
                marker.bindTooltip("", { direction: "top", offset: [0, -18] });
                const line = L.polyline([], { weight: 5, opacity: 0.85 }).addTo(layer);
                info = { marker, line, status: c.status, routeKey: null, route: null };
                known.set(c.id, info);
            } else if (info.status !== c.status) {
                info.marker.setIcon(courierIcon(c.status, mine));
                info.status = c.status;
            }

            info.anchor = [c.lat, c.lon];
            info.type = c.targetType;
            info.elapsed = c.legElapsedMillis;
            info.fetchedAt = Date.now();
            const label = c.targetType ? `<br>order ${esc(c.orderId)}: ${LABEL[c.targetType]}` : "";
            info.marker.setTooltipContent(`<b>${esc(c.id)}</b><br>${esc(c.status)}${label}`);

            if (c.targetType && c.targetLat != null) {
                const style = ROUTE_STYLE[c.targetType] || ROUTE_STYLE.pickup;
                info.line.setStyle({ color: style.color, dashArray: style.dashArray });
                const key = `${c.lat},${c.lon}>${c.targetLat},${c.targetLon}`;
                if (info.routeKey !== key) {
                    info.routeKey = key;
                    info.route = null;
                    fetchRoute([c.lat, c.lon], [c.targetLat, c.targetLon]).then((path) => {
                        if (info.routeKey === key) info.route = path;
                    });
                }
            } else {
                info.routeKey = null;
                info.route = null;
            }
        });

        for (const [id, info] of known) {
            if (!seen.has(id)) {
                layer.removeLayer(info.marker);
                layer.removeLayer(info.line);
                known.delete(id);
            }
        }
    }, [data, user]);

    // Animation: move each courier along its street route, ten times a second.
    useEffect(() => {
        const tick = () => {
            for (const info of couriersRef.current.values()) {
                if (info.route && info.type) {
                    const moving = info.type === "pickup" || info.type === "delivery";
                    const meters = moving
                        ? ((info.elapsed + Date.now() - info.fetchedAt) / 1000) * REAL_SPEED_MPS * TIME_SCALE
                        : 0;
                    const { point, rest } = positionAlong(info.route, meters);
                    info.marker.setLatLng(point);
                    info.line.setLatLngs(rest);      // the line shrinks as the courier gets closer
                } else if (info.anchor) {
                    info.marker.setLatLng(info.anchor);
                    info.line.setLatLngs([]);
                }
            }
        };
        const t = setInterval(tick, 100);
        return () => clearInterval(t);
    }, []);
    const noData = data && data.bakeries.length === 0 && data.couriers.length === 0 && data.customers.length === 0;
    const loading = !data && !error;

    return (
        <div>
            {error && <div className="notice error">The map could not be loaded ({error}). Retrying every few seconds...</div>}
            <div className="map-wrap">
                <div ref={boxRef} className="map-box" />
                {(noData || loading) && (
                    <div className="map-empty">
                        <div style={{ fontSize: 34 }}>🗺️</div>
                        <b>{loading ? "Loading map..." : "The map is empty"}</b>
                        {!loading && (
                            <span>
                                {user.role === "OPERATOR"
                                    ? "No bakeries, couriers or customers are loaded. Check city.json, or use Reset system to reload the initial state."
                                    : "There is nothing to show yet."}
                            </span>
                        )}
                    </div>
                )}
            </div>

            <div className="legend">
                <span><b>Bakery queue:</b>
                    <i className="sw" style={{ background: "#2e9e5b" }} />0-2
                    <i className="sw" style={{ background: "#e0a100" }} />3-5
                    <i className="sw" style={{ background: "#d33" }} />6+
                    <i className="sw" style={{ background: "#888" }} />paused/closed
                </span>
                <span><b>Courier:</b>
                    {Object.entries(COURIER_COLORS).map(([s, c]) => (
                        <span key={s}><i className="sw round" style={{ background: c }} />{s.toLowerCase()}</span>
                    ))}
                </span>
                <span><b>Routes:</b> dashed = offer preview, orange = to the bakery, purple = to the customer</span>
            </div>

            {user.role === "OPERATOR" && data && (
                <div style={{ fontSize: 13, marginTop: 8 }}>
                    <b>Dispatch queue:</b> {data.dispatchQueue.length === 0 ? "empty" : data.dispatchQueue.join(", ")}
                </div>
            )}
        </div>
    );
}