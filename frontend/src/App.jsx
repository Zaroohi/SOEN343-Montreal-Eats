import React, { useState } from "react";
import LoginView from "./components/LoginView";
import RegistrationView from "./components/RegistrationView";

function App() {
    const [page, setPage] = useState("login");

    return (
        <div>
            {page === "login" ? (
                <LoginView
                    onRegister={() => setPage("register")}
                />
            ) : (
                <RegistrationView
                    onLogin={() => setPage("login")}
                />
            )}
        </div>
    );
}

export default App;
