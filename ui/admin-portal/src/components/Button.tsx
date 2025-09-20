 // src/components/Button.tsx
  export function Button(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
    return <button {...props} className={`btn ${props.className ?? ""}`} />;
  }