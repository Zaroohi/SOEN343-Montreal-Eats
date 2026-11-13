export function queueBand(n) {
    if (n >= 6) return { color: "#d33", label: "busy" };
    if (n >= 3) return { color: "#e0a100", label: "moderate" };
    return { color: "#2e9e5b", label: "light" };
}