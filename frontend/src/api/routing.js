const cache = new Map();   // "from|to" -> Promise of a [[lat, lon], ...] path
const R = 6371000;

function haversine(a, b) {
    const rad = Math.PI / 180;
    const dLat = (b[0] - a[0]) * rad;
    const dLon = (b[1] - a[1]) * rad;
    const h = Math.sin(dLat / 2) ** 2 +
        Math.cos(a[0] * rad) * Math.cos(b[0] * rad) * Math.sin(dLon / 2) ** 2;
    return 2 * R * Math.asin(Math.sqrt(h));
}

/** Street route between two [lat, lon] points (free OSRM server). Falls back to a straight line. */
export function fetchRoute(from, to) {
    const key = [from, to].map((p) => p.map((v) => v.toFixed(5)).join(",")).join("|");
    if (!cache.has(key)) {
        const url = `https://router.project-osrm.org/route/v1/driving/${from[1]},${from[0]};${to[1]},${to[0]}?overview=full&geometries=geojson`;
        const straight = [from, to];
        cache.set(key, fetch(url)
            .then((r) => (r.ok ? r.json() : Promise.reject(new Error("routing " + r.status))))
            .then((d) => {
                const coords = d.routes && d.routes[0] && d.routes[0].geometry.coordinates;
                if (!coords || coords.length < 2) return straight;
                return [from, ...coords.map(([lon, lat]) => [lat, lon]), to];
            })
            .catch(() => straight));
    }
    return cache.get(key);
}

/** The point `meters` along the path, plus the part of the path still ahead of it. */
export function positionAlong(path, meters) {
    let left = Math.max(0, meters);
    for (let i = 1; i < path.length; i++) {
        const seg = haversine(path[i - 1], path[i]);
        if (left <= seg) {
            const t = seg === 0 ? 0 : left / seg;
            const point = [
                path[i - 1][0] + (path[i][0] - path[i - 1][0]) * t,
                path[i - 1][1] + (path[i][1] - path[i - 1][1]) * t,
            ];
            return { point, rest: [point, ...path.slice(i)] };
        }
        left -= seg;
    }
    const end = path[path.length - 1];
    return { point: end, rest: [end, end] };
}