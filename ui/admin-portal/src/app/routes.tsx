import { Navigate, useRoutes } from "react-router-dom";
import Shell from "./Shell";
import TradesPage from "../features/trades/TradesPage";
import SettlementsPage from "../features/settlements/SettlementsPage";
import LedgerPage from "../features/ledger/LedgerPage";
import EodPage from "../features/eod/EodPage";
import OutboxAdminPage from "../features/outbox/OutboxAdminPage";

export function Router() {
    const element = useRoutes([
        {
            path: "/",
            element: <Shell />,
            children: [
                { index: true, element: <Navigate to="trades" replace /> },
                { path: "trades", element: <TradesPage /> },
                { path: "settlements", element: <SettlementsPage /> },
                { path: "ledger", element: <LedgerPage /> },
                { path: "eod", element: <EodPage /> },
                { path: "admin/outbox", element: <OutboxAdminPage />},
            ],
        },
    ]);
    return element;
}