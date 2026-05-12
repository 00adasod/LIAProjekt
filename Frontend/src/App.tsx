import type { IPublicClientApplication } from "@azure/msal-browser";
import LoginButton from "./components/LoginButton";
// import HealthCheck from "./components/HealthCheck";
import LearningPortal from "./LearningPortal.tsx";

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
                    <LearningPortal />
                </>
            )}
        </div>
    );
}