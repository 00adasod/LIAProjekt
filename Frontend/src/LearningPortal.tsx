import {type JSX, useState} from "react";
import "./LearningPortal.css";

// ── Types ─────────────────────────────────────────────────────────────────────

type Status = "in-progress" | "completed" | "not-started";
type ViewKey = "courses" | "quizzes" | "stats" | "admin";
type FilterKey = "all" | Status;
type UserRole = "admin" | "student" | "courseAdmin";

interface Course {
    id: number;
    title: string;
    sections: number;
    progress: number;
    status: Status;
}

interface Quiz {
    title: string;
    course: string;
    done: boolean;
    score: string | null;
}

interface User {
    id: number;
    name: string;
    email: string;
    role: UserRole;
    coursesEnrolled: number;
}
// ── Data ──────────────────────────────────────────────────────────────────────

const COURSES: Course[] = [
    { id: 1, title: "Introduction to astronomy",    sections: 12, progress: 75,  status: "in-progress"  },
    { id: 2, title: "Python for beginners",         sections: 20, progress: 40,  status: "in-progress"  },
    { id: 3, title: "Watercolor painting basics",   sections: 8,  progress: 100, status: "completed"    },
    { id: 4, title: "Startup fundamentals",         sections: 15, progress: 0,   status: "not-started"  },
    { id: 5, title: "World history: 1900–2000",     sections: 18, progress: 100, status: "completed"    },
    { id: 6, title: "Mindfulness & stress relief",  sections: 10, progress: 20,  status: "in-progress"  },
];

const QUIZZES: Quiz[] = [
    { title: "Astronomy: solar system",    course: "Astronomy", done: true,  score: "88%" },
    { title: "Python: variables & loops",  course: "Python",    done: true,  score: "74%" },
    { title: "Painting: color theory",     course: "Painting",  done: true,  score: "91%" },
    { title: "Python: functions",          course: "Python",    done: false, score: null  },
    { title: "Astronomy: deep space",      course: "Astronomy", done: false, score: null  },
];

const USERS: User[] = [
    { id: 1, name: "Erik Lindström",   email: "erik.l@email.se",   role: "admin",       coursesEnrolled: 6 },
    { id: 2, name: "Maja Svensson",    email: "maja.s@email.se",   role: "student",     coursesEnrolled: 4 },
    { id: 3, name: "Lars Bergström",   email: "lars.b@email.se",   role: "student",     coursesEnrolled: 2 },
    { id: 4, name: "Ingrid Karlsson",  email: "ingrid.k@email.se", role: "courseAdmin", coursesEnrolled: 1 },
    { id: 5, name: "Anders Nilsson",   email: "anders.n@email.se", role: "student",     coursesEnrolled: 3 },
    { id: 6, name: "Karin Johansson",  email: "karin.j@email.se",  role: "student",     coursesEnrolled: 5 },
    { id: 7, name: "Björn Eriksson",   email: "bjorn.e@email.se",  role: "courseAdmin", coursesEnrolled: 2 },
    { id: 8, name: "Sofia Pettersson", email: "sofia.p@email.se",  role: "student",     coursesEnrolled: 1 },
];

const FILTERS: { key: FilterKey; label: string }[] = [
    { key: "all",         label: "All"         },
    { key: "in-progress", label: "In progress" },
    { key: "completed",   label: "Completed"   },
    { key: "not-started", label: "Not started" },
];

const VIEWS: { key: ViewKey; label: string }[] = [
    { key: "courses",  label: "All modules"   },
    { key: "quizzes",  label: "Examinations"  },
    { key: "stats",    label: "Statistics"    },
    { key: "admin",    label: "Admin"         },
];

// ── Helpers ───────────────────────────────────────────────────────────────────

function pad(n: number): string {
    return n < 10 ? `0${n}` : `${n}`;
}

// ── Sub-components ────────────────────────────────────────────────────────────

function StatusBadge({ status }: { status: Status }) {
    const map: Record<Status, { cls: string; label: string }> = {
        "completed":   { cls: "vmv-status vmv-status--done", label: "✓ Completed"  },
        "in-progress": { cls: "vmv-status vmv-status--wip",  label: "In progress"  },
        "not-started": { cls: "vmv-status vmv-status--todo", label: "Not started"  },
    };
    const { cls, label } = map[status];
    return <span className={cls}>{label}</span>;
}

function ProgressBar({ value }: { value: number }) {
    return (
        <div className="vmv-progress-bg">
            <div className="vmv-progress-fill" style={{ width: `${value}%` }} />
        </div>
    );
}

function CourseCard({ course }: { course: Course }) {
    return (
        <div className={`vmv-course ${course.status === "in-progress" ? "highlight" : ""}`}>
            <div className="vmv-course-num">{pad(course.id)}</div>
            <div className="vmv-course-title">{course.title}</div>
            <div className="vmv-course-meta">{course.sections} avsnitt</div>
            <ProgressBar value={course.progress} />
            <div className="vmv-course-footer">
                <span className="vmv-course-pct">{course.progress}%</span>
                <StatusBadge status={course.status} />
            </div>
        </div>
    );
}

function QuizRow({ quiz, index }: { quiz: Quiz; index: number }) {
    return (
        <div className="vmv-quiz">
            <div className="vmv-quiz-num">{pad(index + 1)}</div>
            <div className="vmv-quiz-body">
                <div className="vmv-quiz-title">{quiz.title}</div>
                <div className="vmv-quiz-course">{quiz.course}</div>
            </div>
            {quiz.done ? (
                <div className="vmv-quiz-score">{quiz.score}</div>
            ) : (
                <button className="vmv-quiz-start">Start ↗</button>
            )}
        </div>
    );
}

function StatCard({ value, label }: { value: string | number; label: string }) {
    return (
        <div className="vmv-stat">
            <div className="vmv-stat-n">{value}</div>
            <div className="vmv-stat-l">{label}</div>
        </div>
    );
}

// ── Views ─────────────────────────────────────────────────────────────────────

function CoursesView() {
    const [filter, setFilter] = useState<FilterKey>("all");
    const [search, setSearch] = useState("");

    const filtered = COURSES.filter((c) => {
        const matchFilter = filter === "all" || c.status === filter;
        const matchSearch =
            c.title.toLowerCase().includes(search.toLowerCase())
        return matchFilter && matchSearch;
    });

    return (
        <>
            {/*<div className="vmv-stats">*/}
            {/*    <StatCard value={6}    label="Enrolled"        />*/}
            {/*    <StatCard value={24}   label="Hours this month" />*/}
            {/*    <StatCard value={8}    label="Quizzes passed"   />*/}
            {/*    <StatCard value={5}    label="Day streak"       />*/}
            {/*</div>*/}

            <div className="vmv-search">
                <span className="vmv-search-icon">⌕</span>
                <input
                    type="text"
                    placeholder="Search modules..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className="vmv-filters">
                {FILTERS.map((f) => (
                    <button
                        key={f.key}
                        className={`vmv-filter ${filter === f.key ? "active" : ""}`}
                        onClick={() => setFilter(f.key)}
                    >
                        {f.label}
                    </button>
                ))}
            </div>

            <div className="vmv-section-head">Alla Kurser</div>

            <div className="vmv-courses">
                {filtered.length === 0 ? (
                    <div className="vmv-empty">No modules match your search.</div>
                ) : (
                    filtered.map((c) => <CourseCard key={c.id} course={c} />)
                )}
            </div>

            <div className="vmv-bottom">
                <div>
                    <div className="vmv-panel-title">Examinations</div>
                    {QUIZZES.slice(0, 3).map((q, i) => (
                        <QuizRow key={i} quiz={q} index={i} />
                    ))}
                </div>
            </div>
        </>
    );
}

function QuizzesView() {
    return (
        <>
            <div className="vmv-section-head">All examinations</div>
            {QUIZZES.map((q, i) => (
                <QuizRow key={i} quiz={q} index={i} />
            ))}
        </>
    );
}

function StatsView() {
    return (
        <>
            <div className="vmv-section-head">Your statistics</div>
            <div className="vmv-stats">
                <StatCard value={6}     label="Total courses"   />
                <StatCard value={2}     label="Completed"       />
                <StatCard value="82%"   label="Avg quiz score"  />
                <StatCard value="24h"   label="Learned"         />
            </div>
            <p className="vmv-stats-note">
                Detailed statistics coming in the next module release.
            </p>
        </>
    );
}

function AdminView() {
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState<"all" | UserRole>("all");
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    const filtered = USERS.filter((u) => {
        const matchRole = roleFilter === "all" || u.role === roleFilter;
        const matchSearch =
            u.name.toLowerCase().includes(search.toLowerCase()) ||
            u.email.toLowerCase().includes(search.toLowerCase());
        return matchRole && matchSearch;
    });

    const roleLabels: Record<UserRole, string> = {
        admin: "Admin",
        student: "Student",
        courseAdmin: "Kursledare",
    };

    const roleCls: Record<UserRole, string> = {
        admin:      "vmv-role vmv-role--admin",
        student:    "vmv-role vmv-role--student",
        courseAdmin: "vmv-role vmv-role--courseAdmin",
    };

    return (
        <>
            {/*<div className="vmv-stats">*/}
            {/*    <StatCard value={USERS.length}                                        label="Total users"    />*/}
            {/*    <StatCard value={USERS.filter(u => u.status === "active").length}     label="Active"         />*/}
            {/*    <StatCard value={USERS.filter(u => u.role === "student").length}      label="Students"       />*/}
            {/*    <StatCard value={USERS.filter(u => u.role === "instructor").length}   label="Instructors"    />*/}
            {/*</div>*/}

            <div className="vmv-admin-toolbar">
                <div className="vmv-search" style={{ flex: 1, margin: 0 }}>
                    <span className="vmv-search-icon">⌕</span>
                    <input
                        type="text"
                        placeholder="Search users..."
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
                            {r === "all" ? "All roles" : roleLabels[r]}
                        </button>
                    ))}
                </div>
            </div>

            <div className="vmv-section-head" style={{ marginTop: "1.25rem" }}>
                Registered users — {filtered.length} of {USERS.length}
            </div>

            <div className="vmv-user-table">
                <div className="vmv-user-thead">
                    <span>#</span>
                    <span>Name</span>
                    <span>Email</span>
                    <span>Role</span>
                    <span>Courses</span>
                </div>
                {filtered.length === 0 ? (
                    <div className="vmv-empty" style={{ gridColumn: "1 / -1" }}>No users match your search.</div>
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
                            <span><span className={roleCls[u.role]}>{roleLabels[u.role]}</span></span>
                            <span className="vmv-user-meta">{u.coursesEnrolled}</span>
                        </div>
                    ))
                )}
            </div>

            {selectedUser && (
                <div className="vmv-user-detail">
                    <div className="vmv-user-detail-header">
                        <div className="vmv-user-avatar">
                            {selectedUser.name.split(" ").map(n => n[0]).join("").slice(0, 2)}
                        </div>
                        <div>
                            <div className="vmv-user-detail-name">{selectedUser.name}</div>
                            <div className="vmv-user-detail-email">{selectedUser.email}</div>
                        </div>
                        <button
                            className="vmv-user-detail-close"
                            onClick={() => setSelectedUser(null)}
                            aria-label="Close"
                        >✕</button>
                    </div>
                    <div className="vmv-user-detail-grid">
                        <div><div className="vmv-user-detail-label">Role</div><div className="vmv-user-detail-val">{selectedUser.role}</div></div>
                        <div><div className="vmv-user-detail-label">Courses enrolled</div><div className="vmv-user-detail-val">{selectedUser.coursesEnrolled}</div></div>
                    </div>
                    <div className="vmv-user-detail-actions">
                        <button className="vmv-quiz-start">Edit user ↗</button>
                        <button className="vmv-quiz-start vmv-action--danger">Remove user ↗</button>
                    </div>
                </div>
            )}
        </>
    );
}

// ── Root component ────────────────────────────────────────────────────────────

export default function LearningPortal() {
    const [view, setView] = useState<ViewKey>("courses");

    const viewMap: Record<ViewKey, JSX.Element> = {
        courses:  <CoursesView  />,
        quizzes:  <QuizzesView  />,
        stats:    <StatsView    />,
        admin:    <AdminView    />
    };

    return (
        <div className="vmv">
            <header className="vmv-header">
                <h1 className="vmv-title">Lärportal</h1>
                <p className="vmv-subtitle">Här står det mer text om man vill ha en undertitel</p>
            </header>

            <nav className="vmv-nav">
                {VIEWS.map((v) => (
                    <button
                        key={v.key}
                        className={view === v.key ? "active" : ""}
                        onClick={() => setView(v.key)}
                    >
                        {v.label}
                    </button>
                ))}
            </nav>

            <main>{viewMap[view]}</main>
        </div>
    );
}