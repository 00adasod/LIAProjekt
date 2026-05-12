import type { IPublicClientApplication } from "@azure/msal-browser";
import LoginButton from "./components/LoginButton";
import HealthCheck from "./components/HealthCheck";

export default function App({ msalInstance }: { msalInstance: IPublicClientApplication }) {

    const account = msalInstance.getActiveAccount();

    if (!account) {
        console.log("No active account - user likely not redirected properly");
    }

    return (
        <div>
            {!account && <LoginButton instance={msalInstance} />}

            {account && (
                <>
                    <h2>Logged in as {account.username}</h2>
                    <HealthCheck instance={msalInstance} />
                </>
            )}
        </div>
    );
}