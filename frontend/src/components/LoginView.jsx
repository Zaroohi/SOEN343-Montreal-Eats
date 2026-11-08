import React, { useState } from "react";

function LoginView({ onRegister , onLoggedIn }) {
    const [id, setId] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleLogin(event) {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            const response = await fetch("/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ id, password })
            });

            if (!response.ok) {
                throw new Error("Invalid ID or password");
            }

            const data = await response.json();
            sessionStorage.setItem("token", data.token);
            if (onLoggedIn) onLoggedIn();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{ maxWidth: "350px", margin: "100px auto", fontFamily: "Arial" }}>
            <h1>Baked To Go</h1>
            <h2>Login</h2>

            <form onSubmit={handleLogin}>
                <div>
                    <label>User ID</label>
                    <input
                        type="text"
                        value={id}
                        onChange={(e) => setId(e.target.value)}
                        required
                        style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                    />
                </div>

                <div>
                    <label>Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                    />
                </div>

                {error && <p style={{ color: "red" }}>{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>

            <p>
                Don't have an account?{" "}
                <button type="button" onClick={onRegister}>
                    Register
                </button>
            </p>
        </div>
    );
}

export default LoginView;
