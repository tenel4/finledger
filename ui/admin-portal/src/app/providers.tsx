import { StrictMode, type PropsWithChildren } from "react";
import { BrowserRouter } from "react-router-dom";
import { ToastProvider } from "../components/Toast";

export function Providers({ children }: PropsWithChildren) {
    return (
        <StrictMode>
            <BrowserRouter>
                <ToastProvider>{children}</ToastProvider>
            </BrowserRouter>
        </StrictMode>
    );
}