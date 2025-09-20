import { flexRender, getCoreRowModel, useReactTable, type ColumnDef } from "@tanstack/react-table";

type Props<T> = {
    data: T[];
    columns: ColumnDef<T, any>[];
    loading?: boolean;
    emptyText?: string;
};

export function DataTable<T>({ data, columns, loading, emptyText = "No data" }: Props<T>) {
    const table = useReactTable({ data, columns, getCoreRowModel: getCoreRowModel() });
    if (loading) return <div>Loading...</div>;
    if (!loading && data.length === 0 ) return <div>{emptyText}</div>;
    return (
        <table className="tbl">
            <thead>
                {table.getHeaderGroups().map((hg) => (
                    <tr key={hg.id}>
                        {hg.headers.map((h) => (
                            <th key={h.id}>{h.isPlaceholder ? null : flexRender(h.column.columnDef.header, h.getContext())}</th>
                        ))}
                    </tr>
                ))}
            </thead>
            <tbody>
                {table.getRowModel().rows.map((r) => (
                    <tr key={r.id}>
                        {r.getVisibleCells().map((c) => (
                            <td key={c.id}>{flexRender(c.column.columnDef.cell, c.getContext())}</td>
                        ))}
                    </tr>
                ))}
            </tbody>
        </table>
    )
}