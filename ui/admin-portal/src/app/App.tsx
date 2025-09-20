import { ErrorBoundary } from "./ErrorBoundary";
import { Providers } from "./providers";
import { Router } from "./routes";

export default function App() {
    return (
        <Providers>
            <ErrorBoundary>
                <Router />
            </ErrorBoundary>
        </Providers>
    );
}