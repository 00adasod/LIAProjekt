export type Status = "in-progress" | "completed" | "not-started";
export type ViewKey = "courses" | "quizzes" | "admin";
export type FilterKey = "all" | Status;
export type UserRole = "admin" | "student" | "courseAdmin";

export interface Course {
    id: number;
    title: string;
    sections: number;
    progress: number;
    status: Status;
}

export interface Quiz {
    title: string;
    course: string;
    done: boolean;
    score: string | null;
}

export interface User {
    id: number;
    name: string;
    email: string;
    role: UserRole;
    coursesEnrolled: number;
}
