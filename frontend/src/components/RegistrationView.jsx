import React, { useState } from "react";

function RegistrationView({ onLogin }) {
    const [id, setId] = useState("");
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleRegister(event) {
        event.preventDefault();
        setError("");

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch("/api/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ id, name, password })
            });

            if (!response.ok) {
                throw new Error("Registration failed");
            }

            alert("Account created successfully!");
            onLogin();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{ maxWidth: "350px", margin: "100px auto", fontFamily: "Arial" }}>
            <h1>Montreal Eats</h1>
            <h2>Create an Account</h2>

            <form onSubmit={handleRegister}>
                <label>User ID</label>
                <input
                    type="text"
                    value={id}
                    onChange={(e) => setId(e.target.value)}
                    required
                    style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                />

                <label>Name</label>
                <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                    style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                />

                <label>Password</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                />

                <label>Confirm Password</label>
                <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    style={{ display: "block", width: "100%", marginBottom: "15px", padding: "8px" }}
                />

                {error && <p style={{ color: "red" }}>{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading ? "Creating account..." : "Register"}
                </button>
            </form>

            <p>
                Already have an account?{" "}
                <button type="button" onClick={onLogin}>
                    Login
                </button>
            </p>
        </div>
    );
}

export default RegistrationView;
