import React, { useEffect, useState, useRef } from "react";
import axios from 'axios';

/**
 * Single-file React + Tailwind CSS app that integrates a session-based auth system
 * with an E-Book store and download functionality.
 *
 * Endpoints Used:
 * - /api/users/* (for authentication)
 * - /api/books/* (for book data)
 * - /api/downloads/* (for capturing leads and downloading files)
 */

// --- API Helpers ---

// Auth API helper using fetch with credentials
const AUTH_API_BASE = "/api/users";
async function authApiFetch(path, options = {}) {
    const res = await fetch(`${AUTH_API_BASE}${path}`, {
        credentials: "include",
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options,
    });
    const payload = await res.json().catch(() => null);
    if (!res.ok) {
        throw new Error(payload?.message || `HTTP ${res.status}`);
    }
    return payload;
}

// Book API functions using Axios
const BOOK_API_BASE = '/api/books';
const getAllBooks = () => axios.get(BOOK_API_BASE);
const searchBooksByName = (name) => {
    if (!name || !name.trim()) return getAllBooks();
    return axios.get(`${BOOK_API_BASE}/name/${encodeURIComponent(name)}`);
};

// Download API functions using Axios
const DOWNLOAD_API_BASE = '/api/downloads';
const captureDownload = (downloadData) => axios.post(`${DOWNLOAD_API_BASE}/capture`, downloadData);

// Special handler for file downloads
const downloadFile = async (downloadId, filename) => {
    const response = await axios.get(`${DOWNLOAD_API_BASE}/file/${downloadId}`, {
        responseType: 'blob', // Important: tells Axios to expect binary data
    });
    // Create a URL for the blob data
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename); // Use the filename from backend
    document.body.appendChild(link);
    link.click();
    // Clean up by revoking the object URL and removing the link
    link.parentNode.removeChild(link);
    window.URL.revokeObjectURL(url);
};


// --- Utility & Hooks ---
function cx(...classes) { return classes.filter(Boolean).join(" "); }
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

const BlueButton = ({ className, ...props }) => (
    <button className={cx("w-full px-4 py-2.5 font-medium rounded-2xl bg-blue-600 text-white shadow-lg shadow-blue-600/20 enabled:hover:bg-blue-700 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400 disabled:opacity-50", className)} {...props} />
);
const OutlineButton = ({ className, ...props }) => (
    <button className={cx("w-full px-4 py-2.5 rounded-2xl border border-gray-300 text-gray-700 hover:bg-gray-100 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400", className)} {...props} />
);

function Field({ label, htmlFor, children, error }) {
    return (
        <div className="space-y-1">
            <label htmlFor={htmlFor} className="block text-sm font-medium text-gray-700">{label}</label>
            {children}
            {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
        </div>
    );
}

function PasswordInput({ id, ...props }) {
    const [show, setShow] = useState(false);
    return (
        <div className="relative">
            <input id={id} type={show ? "text" : "password"} className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400" {...props} />
            <button type="button" onClick={() => setShow(s => !s)} className="absolute inset-y-0 right-2 my-auto text-blue-700/70 text-sm px-2 rounded-md hover:bg-blue-50">{show ? "Hide" : "Show"}</button>
        </div>
    );
}

// --- Download Modal Component ---

function DownloadModal({ book, onClose, onDownloadSuccess }) {
    const [formData, setFormData] = useState({ userName: '', contactNumber: '', email: '' });
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [flash, setFlash] = useFlash(5000);
    const modalRef = useRef();

    // Close modal if clicked outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (modalRef.current && !modalRef.current.contains(event.target)) {
                onClose();
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [onClose]);

    const validate = () => {
        const errs = {};
        if (!formData.userName.trim()) errs.userName = "Name is required";
        if (!/^\d{10}$/.test(formData.contactNumber)) errs.contactNumber = "Enter a valid 10-digit contact number";
        if (!/.+@.+\..+/.test(formData.email)) errs.email = "Enter a valid email address";
        setErrors(errs);
        return Object.keys(errs).length === 0;
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        try {
            const capturePayload = { ...formData, ebookId: book.id };
            const captureResponse = await captureDownload(capturePayload);
            const downloadId = captureResponse.data.id;
            const filename = `${captureResponse.data.ebookName}.pdf`; // Assuming PDF, as per backend code

            // Trigger the actual file download
            await downloadFile(downloadId, filename);

            onDownloadSuccess(`Downloading '${book.name}'...`);
            onClose(); // Close modal on success
        } catch (err) {
            setFlash(err.response?.data?.message || "An error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div ref={modalRef} className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
                <h3 className="text-lg font-bold text-blue-900">Download "{book.name}"</h3>
                <p className="mt-1 text-sm text-gray-600">Please provide your details to start the download.</p>

                <form onSubmit={handleSubmit} className="mt-6 space-y-4">
                    <Field label="Full Name" htmlFor="userName" error={errors.userName}>
                        <input id="userName" name="userName" type="text" value={formData.userName} onChange={handleChange} className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400" />
                    </Field>
                    <Field label="Contact Number" htmlFor="contactNumber" error={errors.contactNumber}>
                        <input id="contactNumber" name="contactNumber" type="tel" value={formData.contactNumber} onChange={handleChange} className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400" />
                    </Field>
                    <Field label="Email Address" htmlFor="email" error={errors.email}>
                        <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-400" />
                    </Field>

                    {flash && <div className="text-sm rounded-lg border border-red-200 bg-red-50 text-red-900 p-3">{flash}</div>}

                    <div className="mt-6 flex justify-end gap-3">
                        <OutlineButton type="button" className="w-auto" onClick={onClose}>Cancel</OutlineButton>
                        <BlueButton type="submit" className="w-auto" disabled={loading}>{loading ? "Processing..." : "Submit & Download"}</BlueButton>
                    </div>
                </form>
            </div>
        </div>
    );
}


// --- Auth Component ---
// ... (AuthCard component remains unchanged)
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
            const user = await authApiFetch("/login", { method: "POST", body: JSON.stringify(login) });
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
        if (!reg.password || reg.password.length < 6) errs.password = "Password must be at least 6 characters";
        setRegErr(errs);
        if (Object.keys(errs).length) return;

        try {
            setLoading(true);
            const user = await authApiFetch("/register", { method: "POST", body: JSON.stringify(reg) });
            setFlash("Registration successful! You can now log in.");
            setTab("login");
            setLogin({ email: user.email, password: "" });
        } catch (err) {
            setFlash(err.message || "Registration failed");
        } finally {
            setLoading(false);
        }
    };

    function TabSwitch({ tab, setTab }) {
        const tabs = [{ id: "login", label: "Login" }, { id: "register", label: "Register" }];
        return (
            <div className="grid grid-cols-2 p-1 rounded-2xl bg-blue-100 gap-1">
                {tabs.map((t) => (
                    <button
                        key={t.id}
                        onClick={() => setTab(t.id)}
                        className={cx("py-2 rounded-2xl text-sm font-medium transition", tab === t.id ? "bg-white text-blue-700 shadow" : "text-blue-800/70 hover:bg-white/60")}
                    >
                        {t.label}
                    </button>
                ))}
            </div>
        );
    }

    return (
        <div className="w-full max-w-lg mx-auto">
            <div className="bg-white/70 backdrop-blur-xl rounded-3xl border border-white/60 shadow-2xl p-6 md:p-8">
                <div className="flex flex-col items-center gap-4 mb-6">
                    <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-blue-500 to-blue-700 shadow-lg" />
                    <h1 className="text-2xl font-semibold text-blue-900">Welcome to the Store</h1>
                    <p className="text-sm text-blue-900/70 text-center">Sign in to continue or create a new account.</p>
                    <TabSwitch tab={tab} setTab={setTab} />
                </div>

                {tab === "login" ? (
                    <form className="space-y-4" onSubmit={handleLogin}>
                        <Field label="Email" htmlFor="login-email" error={loginErr.email}>
                            <input id="login-email" type="email" className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400" value={login.email} onChange={(e) => setLogin({ ...login, email: e.target.value })} placeholder="you@example.com" autoComplete="email"/>
                        </Field>
                        <Field label="Password" htmlFor="login-password" error={loginErr.password}>
                            <PasswordInput id="login-password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} placeholder="••••••••"/>
                        </Field>
                        <BlueButton disabled={loading}>{loading ? "Signing in…" : "Sign in"}</BlueButton>
                    </form>
                ) : (
                    <form className="space-y-4" onSubmit={handleRegister}>
                        <Field label="Name" htmlFor="reg-name" error={regErr.name}>
                            <input id="reg-name" type="text" className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400" value={reg.name} onChange={(e) => setReg({ ...reg, name: e.target.value })} placeholder="Your full name" autoComplete="name"/>
                        </Field>
                        <Field label="Email" htmlFor="reg-email" error={regErr.email}>
                            <input id="reg-email" type="email" className="w-full rounded-2xl border border-blue-200 bg-white/80 px-4 py-2.5 outline-none focus:ring-2 focus:ring-blue-400" value={reg.email} onChange={(e) => setReg({ ...reg, email: e.target.value })} placeholder="you@example.com" autoComplete="email"/>
                        </Field>
                        <Field label="Password" htmlFor="reg-password" error={regErr.password}>
                            <PasswordInput id="reg-password" value={reg.password} onChange={(e) => setReg({ ...reg, password: e.target.value })} placeholder="At least 6 characters"/>
                        </Field>
                        <BlueButton disabled={loading}>{loading ? "Creating account…" : "Create account"}</BlueButton>
                    </form>
                )}
                {flash && <div className="mt-4 text-sm rounded-xl border border-blue-200 bg-blue-50 text-blue-900 p-3">{flash}</div>}
            </div>
        </div>
    );
}

// --- BookStore Component ---
const BookStore = ({ user, onLogout }) => {
    const [books, setBooks] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [flash, setFlash] = useFlash();

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedBook, setSelectedBook] = useState(null);

    useEffect(() => {
        const fetchBooks = async () => {
            setIsLoading(true);
            try {
                const { data } = await getAllBooks();
                setBooks(data);
            } catch (err) {
                setError(err.response?.data?.message || 'Failed to fetch books.');
            } finally {
                setIsLoading(false);
            }
        };
        fetchBooks();
    }, []);

    const handleSearch = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            const { data } = await searchBooksByName(searchTerm);
            setBooks(data);
        } catch (err) {
            setError(err.response?.data?.message || 'Search failed.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDownloadClick = (book) => {
        setSelectedBook(book);
        setIsModalOpen(true);
    };

    return (
        <div className="bg-slate-100 min-h-screen font-sans w-full">
            {isModalOpen && (
                <DownloadModal
                    book={selectedBook}
                    onClose={() => setIsModalOpen(false)}
                    onDownloadSuccess={(msg) => setFlash(msg)}
                />
            )}

            <header className="bg-blue-600 text-white p-4 shadow-lg flex justify-between items-center sticky top-0 z-10">
                <h1 className="text-xl md:text-3xl font-bold">📘 E-Book Store</h1>
                <form onSubmit={handleSearch} className="flex-grow flex justify-center items-center gap-2 px-4">
                    <input type="text" placeholder="Search by book name..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} className="w-full max-w-md p-2 rounded-full text-white text-bold" />
                    <button type="submit" className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded-full transition">Search</button>
                </form>
                <div className="flex items-center gap-4">
                    <span className="hidden sm:block text-sm">Welcome, {user.name}</span>
                    <button onClick={onLogout} className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-5 rounded-full transition">Logout</button>
                </div>
            </header>

            {flash && <div className="z-20 sticky top-20 mx-auto max-w-md p-3 text-center text-sm text-white bg-green-500 rounded-lg shadow-lg">{flash}</div>}

            <main className="p-8">
                {isLoading ? <p className="text-center">Loading...</p> : error ? <p className="text-center text-red-500">{error}</p> : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-8">
                        {books.map((book) => (
                            <div key={book.id} className="bg-white rounded-lg shadow-md overflow-hidden flex flex-col justify-between transform hover:-translate-y-2 transition-transform">
                                <div className="p-5">
                                    <h3 className="text-xl font-semibold text-gray-800 mb-2 truncate" title={book.name}>{book.name}</h3>
                                    <p className="text-gray-500 mb-4">by {book.author}</p>
                                    <p className="text-sm bg-blue-100 text-blue-800 rounded-full px-3 py-1 inline-block">{book.category}</p>
                                </div>
                                <div className="p-5 bg-gray-50 flex items-center justify-between">
                                    <div className="text-2xl font-bold text-blue-600">${book.price ? book.price.toFixed(2) : '0.00'}</div>
                                    <button onClick={() => handleDownloadClick(book)} className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-lg transition">Download</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

// --- Root App Component ---
export default function App() {
    const [authedUser, setAuthedUser] = useState(null);

    useEffect(() => {
        authApiFetch("/home").then(user => setAuthedUser(user)).catch(() => setAuthedUser(null));
    }, []);

    const handleLogout = async () => {
        try {
            await authApiFetch(`/logout`, { method: "POST" });
        } finally {
            setAuthedUser(null);
        }
    };

    if (authedUser) {
        return <BookStore user={authedUser} onLogout={handleLogout} />;
    }

    return (
        <main className="min-h-screen w-full flex items-center justify-center p-4 bg-gradient-to-br from-blue-100 to-white relative">
            <div className="pointer-events-none absolute -top-32 -left-32 h-96 w-96 rounded-full bg-blue-300/20 blur-3xl" />
            <div className="pointer-events-none absolute -bottom-32 -right-32 h-96 w-96 rounded-full bg-sky-300/20 blur-3xl" />
            <div className="w-full max-w-4xl z-10">
                <AuthCard onAuth={setAuthedUser} />
            </div>
        </main>
    );
}

