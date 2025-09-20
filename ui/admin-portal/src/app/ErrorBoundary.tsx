import { Component, type ReactNode } from "react";

export class ErrorBoundary extends Component<{ children: ReactNode }, { hasError: boolean; msg?: string }> {
    constructor(props: { children: ReactNode }) {
        super(props);
        this.state = { hasError: false };
    }
    static getDerivedStateFromError(error: unknown) {
        return { hasError: true, msg: (error as Error).message };
    }
    render() {
        if (this.state.hasError) {
            return <div style={{ padding: 24 }}>
                <h2>Something went wrong</h2>
                <p>{this.state.msg}</p>
                <button onClick={() => location.reload()}>Relad</button>
            </div>
        }
        return this.props.children;
    }
}