import React, { useState, useEffect } from "react";
import "./styles.css";
import LoginView from "./components/LoginView";
import RegistrationView from "./components/RegistrationView";
import DashboardView from "./components/DashboardView";
import { api } from "./api/client";

function App() {
    const [page, setPage] = useState("login");
    const [user, setUser] = useState(null);

    async function loadUser() {
        try {
            setUser(await api("GET", "/api/me"));
        } catch {
            sessionStorage.removeItem("token");
            setUser(null);
        }
    }

    useEffect(() => {
        if (sessionStorage.getItem("token")) loadUser();
    }, []);

    function logout() {
        sessionStorage.removeItem("token");
        setUser(null);
        setPage("login");
    }

    if (user) return <DashboardView user={user} onLogout={logout} />;

    return (
        <div className="auth-page">
            {page === "login" ? (
                <LoginView onRegister={() => setPage("register")} onLoggedIn={loadUser} />
            ) : (
                <RegistrationView onLogin={() => setPage("login")} />
            )}
        </div>
    );
}

export default App;