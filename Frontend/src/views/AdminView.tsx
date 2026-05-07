import { useState, useEffect } from "react";
import type { User, UserRole, UserResponse } from "../types";
import { pad } from "../components/Shared";

// ── Constants ─────────────────────────────────────────────────────────────────

const ROLE_LABELS: Record<UserRole, string> = {
    admin:       "Admin",
    student:     "Student",
    courseAdmin: "Kursledare",
};

const ROLE_CLS: Record<UserRole, string> = {
    admin:       "vmv-role vmv-role--admin",
    student:     "vmv-role vmv-role--student",
    courseAdmin: "vmv-role vmv-role--courseAdmin",
};

// The API may send role strings that don't exactly match UserRole.
// This normalises them so unknown values fall back to "student".
function normaliseRole(raw: string): UserRole {
    const map: Record<string, UserRole> = {
        admin:       "admin",
        Admin:       "admin",
        student:     "student",
        Student:     "student",
        courseadmin: "courseAdmin",
        courseAdmin: "courseAdmin",
        CourseAdmin: "courseAdmin",
    };
    return map[raw] ?? "student";
}

// Transforms the raw API shape into the User shape our components expect
function mapUser(u: UserResponse): User {
    return {
        id:              u.id,
        name:            u.displayName,
        email:           u.mail,
        role:            normaliseRole(u.role),
        coursesEnrolled: 0,   // not provided by the API; update when available
    };
}

// ── Hook ──────────────────────────────────────────────────────────────────────

function useUsers() {
    const [users, setUsers]     = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]     = useState<string | null>(null);

    useEffect(() => {
        // Replace this URL with your real API endpoint
        fetch("http://localhost:8080/api/users/all")
            .then((res) => {
                if (!res.ok) throw new Error(`Server error: ${res.status}`);
                return res.json() as Promise<UserResponse[]>;
            })
            .then((data) => setUsers(data.map(mapUser)))
            .catch((err: Error) => setError(err.message))
            .finally(() => setLoading(false));
    }, []); // runs once on mount

    return { users, loading, error };
}

// ── Component ─────────────────────────────────────────────────────────────────

export function AdminView() {
    const { users, loading, error }             = useUsers();
    const [search, setSearch]                   = useState("");
    const [roleFilter, setRoleFilter]           = useState<"all" | UserRole>("all");
    const [selectedUser, setSelectedUser]       = useState<User | null>(null);

    const filtered = users.filter((u) => {
        const matchRole   = roleFilter === "all" || u.role === roleFilter;
        const matchSearch =
            u.name.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase());
        return matchRole && matchSearch;
    });

    // ── Loading state ─────────────────────────────────────────────────────────

    if (loading) {
        return (
            <div className="vmv-fetch-state">
                <div className="vmv-fetch-spinner" />
                <span>Hämtar användare...</span>
            </div>
        );
    }

    // ── Error state ───────────────────────────────────────────────────────────

    if (error) {
        return (
            <div className="vmv-fetch-state vmv-fetch-state--error">
                <span>Kunde inte hämta användare: {error}</span>
                <button
                    className="vmv-quiz-start"
                    onClick={() => window.location.reload()}
                >
                    Försök igen ↗
                </button>
            </div>
        );
    }

    // ── Main render ───────────────────────────────────────────────────────────

    return (
        <>
            <div className="vmv-admin-toolbar">
                <div className="vmv-search" style={{ flex: 1, margin: 0 }}>
                    <span className="vmv-search-icon">⌕</span>
                    <input
                        type="text"
                        placeholder="Sök användare..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
                <div className="vmv-filters" style={{ margin: 0 }}>
                    {(["all", "admin", "student", "courseAdmin"] as const).map((r) => (
                        <button
                            key={r}
                            className={`vmv-filter ${roleFilter === r ? "active" : ""}`}
                            onClick={() => setRoleFilter(r)}
                        >
                            {r === "all" ? "Alla roller" : ROLE_LABELS[r]}
                        </button>
                    ))}
                </div>
            </div>

            <div className="vmv-section-head" style={{ marginTop: "1.25rem" }}>
                Registrerade användare — {filtered.length} av {users.length}
            </div>

            <div className="vmv-user-table">
                <div className="vmv-user-thead">
                    <span>#</span>
                    <span>Namn</span>
                    <span>Email</span>
                    <span>Roll</span>
                    <span>Kurser</span>
                </div>

                {filtered.length === 0 ? (
                    <div className="vmv-empty">Inga användare matchar din sökning.</div>
                ) : (
                    filtered.map((u) => (
                        <div
                            key={u.id}
                            className={`vmv-user-row ${selectedUser?.id === u.id ? "selected" : ""}`}
                            onClick={() => setSelectedUser(selectedUser?.id === u.id ? null : u)}
                        >
                            <span className="vmv-user-id">{pad(u.id)}</span>
                            <span className="vmv-user-name">{u.name}</span>
                            <span className="vmv-user-email">{u.email}</span>
                            <span><span className={ROLE_CLS[u.role]}>{ROLE_LABELS[u.role]}</span></span>
                            <span className="vmv-user-meta">{u.coursesEnrolled}</span>
                        </div>
                    ))
                )}
            </div>

            {selectedUser && (
                <div className="vmv-user-detail">
                    <div className="vmv-user-detail-header">
                        <div className="vmv-user-avatar">
                            {selectedUser.name.split(" ").map((n) => n[0]).join("").slice(0, 2)}
                        </div>
                        <div>
                            <div className="vmv-user-detail-name">{selectedUser.name}</div>
                            <div className="vmv-user-detail-email">{selectedUser.email}</div>
                        </div>
                        <button
                            className="vmv-user-detail-close"
                            onClick={() => setSelectedUser(null)}
                            aria-label="Stäng"
                        >
                            ✕
                        </button>
                    </div>

                    <div className="vmv-user-detail-grid">
                        <div>
                            <div className="vmv-user-detail-label">Roll</div>
                            <div className="vmv-user-detail-val">{ROLE_LABELS[selectedUser.role]}</div>
                        </div>
                        <div>
                            <div className="vmv-user-detail-label">Antal Kurser</div>
                            <div className="vmv-user-detail-val">{selectedUser.coursesEnrolled}</div>
                        </div>
                    </div>

                    <div className="vmv-user-detail-actions">
                        <button className="vmv-quiz-start">Lägg till i kurs ↗</button>
                        <button className="vmv-quiz-start vmv-action--danger">Ta bort från kurs ↗</button>
                    </div>
                </div>
            )}
        </>
    );
}
