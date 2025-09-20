import { NavLink, Outlet } from "react-router-dom";

export default function Shell() {
    return (
        <div className="shell">
            <aside className="sidebar">
                <h1>FinLedger</h1>
                <nav>
                    <NavLink to="/trades">Trades</NavLink>
                    <NavLink to="/settlements">Settlements</NavLink>
                    <NavLink to="/ledger">Ledger</NavLink>
                    <NavLink to="/eod">EOD</NavLink>
                    <NavLink to="/admin/outbox">Outbox Admin</NavLink>
                </nav>
            </aside>
            <main className="content">
                <Outlet />
            </main>
        </div>
    );
}