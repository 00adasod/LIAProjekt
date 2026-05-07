import {useState} from "react";
import type { User, UserRole } from "../types";
import { USERS } from "../data";
import { pad } from "../components/Shared";

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

// async function fetchUsers(): Promise<User[]> {
//     const response = await fetch('/api/users/all');
//     const data = await response.json();
//     return data;
// }

// function UserList() {
//     const [users, setUsers] = useState<User[]>([]);
//     const [error, setError] = useState<string | null>(null);
//     useEffect(() => {
//         async function fetchData() {
//             try {
//                 const data = await fetchUsers();
//                 setUsers(data);
//             } catch (error) {
//                 setError(error.message);
//             }
//         }
//         fetchData();
//     }, []);
//     if (error) {
//         return <div>Error: {error}</div>;
//     }
//     return (
//         <div>
//             {users.map((user) => (
//                 <div key={user.id}>
//                     <h2>{user.name}</h2>
//                     <p>{user.email}</p>
//                 </div>
//             ))}
//         </div>
//     );
// }

export function AdminView() {
    const [search, setSearch]           = useState("");
    const [roleFilter, setRoleFilter]   = useState<"all" | UserRole>("all");
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    const filtered = USERS.filter((u) => {
        const matchRole   = roleFilter === "all" || u.role === roleFilter;
        const matchSearch =
            u.name.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase());
        return matchRole && matchSearch;
    });

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
                Registrerade användare — {filtered.length} av {USERS.length}
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
