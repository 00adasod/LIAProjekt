import type { IPublicClientApplication } from "@azure/msal-browser";
import { getAccessToken } from "../auth/getAccessToken";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

if (!BASE_URL) {
    console.error("Missing VITE_API_BASE_URL in environment variables");
}

async function safeFetch(url: string, token: string, errorMessage: string) {
    try {
        const res = await fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (!res.ok) {
            const text = await res.text().catch(() => "");
            console.error("API error:", res.status, text);
            throw new Error(errorMessage);
        }

        return await res.json();
    } catch (err) {
        console.error("Network/API failure:", err);
        throw err;
    }
}

export async function getHealth(instance: IPublicClientApplication) {
    if (!BASE_URL) return null;

    const token = await getAccessToken(instance);

    return safeFetch(
        `${BASE_URL}/health`,
        token,
        "Failed health request"
    );
}

export async function getGraphUsers(instance: IPublicClientApplication) {
    if (!BASE_URL) return null;

    const token = await getAccessToken(instance);

    return safeFetch(
        `${BASE_URL}/graph/users`,
        token,
        "Failed graph users request"
    );
}