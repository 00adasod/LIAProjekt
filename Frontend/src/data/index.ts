import type { Course, Quiz, User, FilterKey, ViewKey } from "../types";

export const COURSES: Course[] = [
    { id: 1, title: "Introduction to astronomy",   sections: 12, progress: 75,  status: "in-progress"  },
    { id: 2, title: "Python for beginners",        sections: 20, progress: 40,  status: "in-progress"  },
    { id: 3, title: "Watercolor painting basics",  sections: 8,  progress: 100, status: "completed"    },
    { id: 4, title: "Startup fundamentals",        sections: 15, progress: 0,   status: "not-started"  },
    { id: 5, title: "World history: 1900–2000",    sections: 18, progress: 100, status: "completed"    },
    { id: 6, title: "Mindfulness & stress relief", sections: 10, progress: 20,  status: "in-progress"  },
];

export const QUIZZES: Quiz[] = [
    { title: "Astronomy: solar system",   course: "Astronomy", done: true,  score: "88%" },
    { title: "Python: variables & loops", course: "Python",    done: true,  score: "74%" },
    { title: "Painting: color theory",    course: "Painting",  done: true,  score: "91%" },
    { title: "Python: functions",         course: "Python",    done: false, score: null  },
    { title: "Astronomy: deep space",     course: "Astronomy", done: false, score: null  },
];

export const USERS: User[] = [
    { id: 1, name: "Erik Lindström",   email: "erik.l@email.se",   role: "admin",       coursesEnrolled: 6 },
    { id: 2, name: "Maja Svensson",    email: "maja.s@email.se",   role: "student",     coursesEnrolled: 4 },
    { id: 3, name: "Lars Bergström",   email: "lars.b@email.se",   role: "student",     coursesEnrolled: 2 },
    { id: 4, name: "Ingrid Karlsson",  email: "ingrid.k@email.se", role: "courseAdmin", coursesEnrolled: 1 },
    { id: 5, name: "Anders Nilsson",   email: "anders.n@email.se", role: "student",     coursesEnrolled: 3 },
    { id: 6, name: "Karin Johansson",  email: "karin.j@email.se",  role: "student",     coursesEnrolled: 5 },
    { id: 7, name: "Björn Eriksson",   email: "bjorn.e@email.se",  role: "courseAdmin", coursesEnrolled: 2 },
    { id: 8, name: "Sofia Pettersson", email: "sofia.p@email.se",  role: "student",     coursesEnrolled: 1 },
];

export const FILTERS: { key: FilterKey; label: string }[] = [
    { key: "all",         label: "All"          },
    { key: "in-progress", label: "Pågående"     },
    { key: "completed",   label: "Klar"         },
    { key: "not-started", label: "Inte startad" },
];

export const VIEWS: { key: ViewKey; label: string }[] = [
    { key: "courses", label: "Alla Kurser" },
    { key: "admin",   label: "Admin"       },
];
