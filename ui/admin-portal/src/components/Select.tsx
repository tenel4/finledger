  // src/components/Select.tsx
  export function Select(props: React.SelectHTMLAttributes<HTMLSelectElement>) {
    return <select {...props} className={`sel ${props.className ?? ""}`} />;
  }