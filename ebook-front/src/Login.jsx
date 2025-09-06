import React, { useEffect, useState } from "react";

/**
 * Single-file React + Tailwind CSS app that integrates with Spring Boot endpoints:
 * POST   /api/users/register
 * POST   /api/users/login
 * GET    /api/users/home
 * POST   /api/users/logout
 * PUT    /api/users/update/{userId}
 * DELETE /api/users/delete/{userId}
 *
 * Notes
 * - All components are in this single file.
 * - Uses session cookies; all requests send `credentials: 'include'`.
 * - Modern design: glassy card, subtle gradients, focus rings, micro-interactions.
 * - Replaced native confirm() with a custom modal for deleting the user.
 */

// --- API Helper ---
const API_BASE = "/api/users"; // Adjust if your backend is hosted elsewhere

async function apiFetch(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        credentials: "include",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options,
    });

    const contentType = res.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
        ? await res.json().catch(() => null)
        : await res.text().catch(() => null);

    if (!res.ok) {
        const message = Array.isArray(payload)
            ? payload.join("\n")
            : typeof payload === "string"
                ? payload
                : payload?.message || `HTTP ${res.status}`;
        throw new Error(message);
    }
    return payload;
}

// --- Utility Functions ---
function cx(...classes) {
    return classes.filter(Boolean).join(" ");
}

// --- Custom Hooks ---
function useFlash(timeout = 3000) {
    const [msg, setMsg] = useState(null);
    useEffect(() => {
        if (!msg) return;
        const id = setTimeout(() => setMsg(null), timeout);
        return () => clearTimeout(id);
    }, [msg, timeout]);
    return [msg, setMsg];
}

// --- Reusable UI Components ---

const BlueButton = ({ className, disabled, children, ...props }) => (
    <button
        className={cx(
            "relative inline-flex items-center justify-center w-full px-4 py-2.5 font-medium rounded-2xl",
            "bg-blue-600 text-white shadow-lg shadow-blue-600/20",
            "enabled:hover:bg-blue-700 enabled:active:scale-[.99] transition",
            "focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400 focus-visible:ring-offset-2",
            "disabled:opacity-50 disabled:cursor-not-allowed",
            className
        )}
        disabled={disabled}
        {...props}
    >
        {children}
    </button>
);

const OutlineButton = ({ className, children, ...props }) => (
    <button
        className={cx(
            "w-full px-4 py-2.5 rounded-2xl border border-blue-200 text-blue-700",
            "hover:bg-blue-50 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400",
            className
        )}
        {...props}
    >
        {children}
    </button>
);

const RedButton = ({ className, ...props }) => (
    <button
        className={cx(
            "bg-red-500 text-white hover:bg-red-600",
            "px-4 py-2.5 font-medium rounded-2xl transition focus:outline-none focus-visible:ring-2 focus-visible:ring-red-400 focus-visible:ring-offset-2",
            className
        )}
        {...props}
    />
)

function Field({ label, htmlFor, children, error }) {
    return (
        <div className="space-y-1">
            <label htmlFor={htmlFor} className="block text-sm font-medium text-blue-900/80">
                {label}
            </label>
            {children}
            {error ? (
                <p className="text-xs text-red-600 mt-1">{error}</p>
            ) : null}
        </div>
    );
}

function PasswordInput({ id, value, onChange, placeholder }) {
    const [show, setShow] = useState(false);
    return (
        <div className="relative">
            <input
                id={id}
                type={show ? "text" : "password"}
                className="w-full rounded-2xl border border-blue-200 bg-white/80 backdrop-blur px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400"
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                autoComplete="current-password"
            />
            <button
                type="button"
                onClick={() => setShow((s) => !s)}
                className="absolute inset-y-0 right-2 my-auto text-blue-700/70 text-sm px-2 rounded-md hover:bg-blue-50"
                aria-label={show ? "Hide password" : "Show password"}
            >
                {show ? "Hide" : "Show"}
            </button>
        </div>
    );
}

function TabSwitch({ tab, setTab }) {
    const tabs = [
        { id: "login", label: "Login" },
        { id: "register", label: "Register" },
    ];
    return (
        <div className="grid grid-cols-2 p-1 rounded-2xl bg-blue-100 gap-1">
            {tabs.map((t) => (
                <button
                    key={t.id}
                    onClick={() => setTab(t.id)}
                    className={cx(
                        "py-2 rounded-2xl text-sm font-medium transition",
                        tab === t.id ? "bg-white text-blue-700 shadow" : "text-blue-800/70 hover:bg-white/60"
                    )}
                >
                    {t.label}
                </button>
            ))}
        </div>
    );
}

function ConfirmModal({ isOpen, onClose, onConfirm, title, children }) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
                <h3 className="text-lg font-bold text-blue-900">{title}</h3>
                <div className="mt-2 text-sm text-blue-800/80">{children}</div>
                <div className="mt-6 flex justify-end gap-3">
                    <OutlineButton className="w-auto" onClick={onClose}>Cancel</OutlineButton>
                    <RedButton className="w-auto" onClick={onConfirm}>Delete</RedButton>
                </div>
            </div>
        </div>
    );
}


// --- Main Application Components ---

function AuthCard({ onAuth }) {
    const [tab, setTab] = useState("login");
    const [loading, setLoading] = useState(false);
    const [flash, setFlash] = useFlash();

    const [login, setLogin] = useState({ email: "", password: "" });
    const [loginErr, setLoginErr] = useState({});
    const [reg, setReg] = useState({ name: "", email: "", password: "" });
    const [regErr, setRegErr] = useState({});

    const validateEmail = (email) => /.+@.+\..+/.test(email);

    const handleLogin = async (e) => {
        e.preventDefault();
        const errs = {};
        if (!validateEmail(login.email)) errs.email = "Enter a valid email";
        if (!login.password) errs.password = "Password is required";
        setLoginErr(errs);
        if (Object.keys(errs).length) return;

        try {
            setLoading(true);
            const user = await apiFetch("/login", {
                method: "POST",
                body: JSON.stringify(login),
            });
            setFlash("Logged in successfully");
            onAuth(user);
        } catch (err) {
            setFlash(err.message || "Login failed");
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        const errs = {};
        if (!reg.name.trim()) errs.name = "Name is required";
        if (!validateEmail(reg.email)) errs.email = "Enter a valid email";
        if (!reg.password || reg.password.length < 6)
            errs.password = "Password must be at least 6 characters";
        setRegErr(errs);
        if (Object.keys(errs).length) return;

        try {
            setLoading(true);
            const user = await apiFetch("/register", {
                method: "POST",
                body: JSON.stringify(reg),
            });
            setFlash("Registration successful. You can now log in.");
            setTab("login");
            setLogin((l) => ({ ...l, email: user.email, password: "" }));
        } catch (err) {
            setFlash(err.message || "Registration failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="w-full max-w-lg mx-auto">
            <div className="bg-white/70 backdrop-blur-xl rounded-3xl border border-white/60 shadow-2xl p-6 md:p-8">
                <div className="flex flex-col items-center gap-4 mb-6">
                    <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-blue-500 to-blue-700 shadow-lg" />
                    <h1 className="text-2xl font-semibold text-blue-900">Welcome</h1>
                    <p className="text-sm text-blue-900/70 text-center">
                        Sign in to continue or create a new account.
                    </p>
                    <TabSwitch tab={tab} setTab={setTab} />
                </div>

                {tab === "login" ? (
                    <form className="space-y-4" onSubmit={handleLogin}>
                        <Field label="Email" htmlFor="login-email" error={loginErr.email}>
                            <input
                                id="login-email"
                                type="email"
                                className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400"
                                value={login.email}
                                onChange={(e) => setLogin({ ...login, email: e.target.value })}
                                placeholder="you@example.com"
                                autoComplete="email"
                            />
                        </Field>
                        <Field label="Password" htmlFor="login-password" error={loginErr.password}>
                            <PasswordInput
                                id="login-password"
                                value={login.password}
                                onChange={(e) => setLogin({ ...login, password: e.target.value })}
                                placeholder="••••••••"
                            />
                        </Field>
                        <BlueButton disabled={loading}>{loading ? "Signing in…" : "Sign in"}</BlueButton>
                    </form>
                ) : (
                    <form className="space-y-4" onSubmit={handleRegister}>
                        <Field label="Name" htmlFor="reg-name" error={regErr.name}>
                            <input
                                id="reg-name"
                                type="text"
                                className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400"
                                value={reg.name}
                                onChange={(e) => setReg({ ...reg, name: e.target.value })}
                                placeholder="Your full name"
                                autoComplete="name"
                            />
                        </Field>
                        <Field label="Email" htmlFor="reg-email" error={regErr.email}>
                            <input
                                id="reg-email"
                                type="email"
                                className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400"
                                value={reg.email}
                                onChange={(e) => setReg({ ...reg, email: e.target.value })}
                                placeholder="you@example.com"
                                autoComplete="email"
                            />
                        </Field>
                        <Field label="Password" htmlFor="reg-password" error={regErr.password}>
                            <PasswordInput
                                id="reg-password"
                                value={reg.password}
                                onChange={(e) => setReg({ ...reg, password: e.target.value })}
                                placeholder="At least 6 characters"
                            />
                        </Field>
                        <BlueButton disabled={loading}>{loading ? "Creating account…" : "Create account"}</BlueButton>
                    </form>
                )}

                {flash ? (
                    <div className="mt-4 text-sm rounded-xl border border-blue-200 bg-blue-50 text-blue-900 p-3">
                        {flash}
                    </div>
                ) : null}
            </div>
            <p className="text-center text-xs text-blue-900/60 mt-6">
                By continuing you agree to our Terms & Privacy Policy.
            </p>
        </div>
    );
}


function Dashboard({ user, onLogout, onUserUpdate, onUserDelete }) {
    const [name, setName] = useState(user?.name || "");
    const [email, setEmail] = useState(user?.email || "");
    const [password, setPassword] = useState("");
    const [welcome, setWelcome] = useState("");
    const [loading, setLoading] = useState(false);
    const [flash, setFlash] = useFlash();
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    useEffect(() => {
        (async () => {
            try {
                const text = await apiFetch("/home", { method: "GET" });
                setWelcome(typeof text === "string" ? text : JSON.stringify(text));
            } catch (e) {
                setWelcome("");
                setFlash(e.message || "Failed to fetch welcome message.");
            }
        })();
    }, []);

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            const payload = { id: user.id, name, email };
            if (password) payload.password = password;
            const updated = await apiFetch(`/update/${user.id}`, {
                method: "PUT",
                body: JSON.stringify(payload),
            });
            setFlash("Profile updated successfully.");
            onUserUpdate(updated);
            setPassword("");
        } catch (e) {
            setFlash(e.message || "Update failed.");
        } finally {
            setLoading(false);
        }
    };

    const confirmDelete = async () => {
        setIsDeleteModalOpen(false);
        try {
            setLoading(true);
            await apiFetch(`/delete/${user.id}`, { method: "DELETE" });
            setFlash("Account deleted.");
            onUserDelete();
        } catch (e) {
            setFlash(e.message || "Delete failed.");
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = async () => {
        try {
            await apiFetch(`/logout`, { method: "POST" });
        } catch (e) {
            // Ignore errors on logout, just log out the user on the client
        } finally {
            onLogout();
        }
    };

    return (
        <div className="w-full max-w-2xl mx-auto">
            <ConfirmModal
                isOpen={isDeleteModalOpen}
                onClose={() => setIsDeleteModalOpen(false)}
                onConfirm={confirmDelete}
                title="Delete Account"
            >
                <p>This will permanently delete your account and all associated data. This action cannot be undone. Are you sure you want to continue?</p>
            </ConfirmModal>

            <div className="bg-white/70 backdrop-blur-xl rounded-3xl border border-white/60 shadow-2xl p-6 md:p-8">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <p className="text-sm text-blue-900/70">Signed in as</p>
                        <h2 className="text-xl font-semibold text-blue-900">{user.name}</h2>
                    </div>
                    <OutlineButton onClick={handleLogout} className="w-full sm:w-auto">Log out</OutlineButton>
                </div>

                <div className="mt-6 p-4 rounded-2xl bg-gradient-to-br from-blue-50 to-blue-100 text-blue-900 border border-blue-200">
                    <p className="text-sm">{welcome || "Welcome to your dashboard!"}</p>
                </div>

                <form className="mt-6 space-y-4" onSubmit={handleUpdate}>
                    <h3 className="text-lg font-semibold text-blue-900">Edit Profile</h3>
                    <div className="grid md:grid-cols-2 gap-4">
                        <Field label="Name" htmlFor="upd-name">
                            <input
                                id="upd-name"
                                type="text"
                                className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                        </Field>
                        <Field label="Email" htmlFor="upd-email">
                            <input
                                id="upd-email"
                                type="email"
                                className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </Field>
                    </div>
                    <Field label="New Password (optional)" htmlFor="upd-password">
                        <PasswordInput
                            id="upd-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Leave blank to keep current password"
                        />
                    </Field>
                    <div className="flex flex-col sm:flex-row gap-3">
                        <BlueButton className="sm:w-48" disabled={loading}>
                            {loading ? "Saving…" : "Save changes"}
                        </BlueButton>
                        <OutlineButton type="button" className="sm:w-48 text-red-600 border-red-200 hover:bg-red-50" onClick={() => setIsDeleteModalOpen(true)}>
                            Delete account
                        </OutlineButton>
                    </div>
                    {flash ? (
                        <div className="text-sm rounded-xl border border-blue-200 bg-blue-50 text-blue-900 p-3">
                            {flash}
                        </div>
                    ) : null}
                </form>
            </div>
        </div>
    );
}

// --- Root App Component ---
export default function App() {
    const [authedUser, setAuthedUser] = useState(null);

    return (
        <main className="min-h-screen w-full flex items-center justify-center p-4 bg-gradient-to-br from-blue-100 to-white relative overflow-hidden">
            {/* Background decorative blobs */}
            <div className="pointer-events-none absolute -top-32 -left-32 h-96 w-96 rounded-full bg-blue-300/20 blur-3xl" />
            <div className="pointer-events-none absolute -bottom-32 -right-32 h-96 w-96 rounded-full bg-sky-300/20 blur-3xl" />

            <div className="w-full max-w-4xl z-10">
                <div className="text-center mb-8">
                    <h1 className="text-3xl md:text-4xl font-bold tracking-tight text-blue-900">
                        E-Book Store
                    </h1>
                    <p className="mt-2 text-blue-900/70">
                        Secure session-based auth with Spring Boot & React
                    </p>
                </div>

                {!authedUser ? (
                    <AuthCard onAuth={setAuthedUser} />
                ) : (
                    <Dashboard
                        user={authedUser}
                        onLogout={() => setAuthedUser(null)}
                        onUserUpdate={(u) => setAuthedUser(u)}
                        onUserDelete={() => setAuthedUser(null)}
                    />
                )}

                <footer className="mt-10 text-center text-xs text-blue-900/60">
                    <p>
                        Tip: Ensure your backend enables CORS and same-site cookies if the frontend runs on a different origin.
                    </p>
                </footer>
            </div>
        </main>
    );
}
