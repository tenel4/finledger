// src/components/Input.tsx
export function Input(props: React.InputHTMLAttributes<HTMLInputElement>) {
    return <input {...props} className={`inp ${props.className ?? ""}`} />;
  }
  