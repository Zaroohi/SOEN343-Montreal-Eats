let lines = [];
const subscribers = new Set();

export function log(kind, text) {
    lines = [...lines, { time: new Date().toLocaleTimeString(), kind, text }];
    subscribers.forEach((f) => f());
}
export function subscribe(f) { subscribers.add(f); return () => subscribers.delete(f); }
export function getLines() { return lines; }

export async function api(method, path, body) {
    const token = sessionStorage.getItem("token");
    log("cmd", `${method} ${path}${body ? " " + JSON.stringify(body) : ""}`);
    let res;
    try {
        res = await fetch(path, {
            method,
            headers: {
                "Content-Type": "application/json",
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            body: body ? JSON.stringify(body) : undefined,
        });
    } catch (e) {
        log("error", "Network error: " + e.message);
        throw e;
    }
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }
    if (!res.ok) {
        const msg = (data && data.error) || text || res.statusText;
        log("error", `${res.status} ${msg}`);
        throw new Error(msg);
    }
    if (data && data.eventId) {
        log("ok", `${data.orderId} -> ${data.status} (event ${data.eventId})`);
    } else {
        log("ok", "OK");
    }
    return data;
}