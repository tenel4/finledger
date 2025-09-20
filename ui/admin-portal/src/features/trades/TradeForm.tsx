import { useForm } from "react-hook-form";
import type { SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "../../components/Input";
import { Select } from "../../components/Select";
import { Button } from "../../components/Button";
import { createTrade } from "../../api/trades";
import { useToast } from "../../components/Toast";

// ✅ Define schema with correct coercion and output type
const schema = z.object({
  symbol: z.string().min(1),
  side: z.enum(["BUY", "SELL"], { error: "Side is required" }),
  quantity: z.coerce.number().int().positive(),
  price: z.coerce.number().positive(),
  currency: z.string().min(3),
  buyerAccountId: z.string().uuid({ version: "v4", message: "Must be a valid UUID" }),
  sellerAccountId: z.string().uuid({ version: "v4" }),
});

// ✅ Infer the *output* type from schema
type FormValues = z.infer<typeof schema>;

export default function TradeForm() {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } =
    useForm<FormValues>({
      resolver: zodResolver(schema) as any, // 👈 cast to any to satisfy RHF's generic constraint
      defaultValues: {
        symbol: "",
        side: "BUY",
        quantity: 0,
        price: 0,
        currency: "USD",
        buyerAccountId: "",
        sellerAccountId: "",
      },
    });

  const toast = useToast();

  // ✅ Explicitly type the submit handler
  const onSubmit: SubmitHandler<FormValues> = async (values) => {
      console.log("Form values:", values); // 👈 should match CreateTradeRequest
    try {
      const res = await createTrade(values);
      toast.success(`Trade created: ${res.id.slice(0, 8)}…`);
      reset();
    } catch (e: any) {
      toast.error(e.message ?? "Failed to create trade");
    }
  };

 return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="card">
      <h3>Create trade</h3>
      <div className="grid">
        <label>
          Symbol
          <Input placeholder="AAPL" {...register("symbol")} />
          {errors.symbol && <span className="error">{errors.symbol.message}</span>}
        </label>
        <label>
          Side
          <Select {...register("side")}>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </Select>
          {errors.side && <span className="error">{errors.side.message}</span>}
        </label>
        <label>
          Quantity
          <Input type="number" {...register("quantity", { valueAsNumber: true })} />
          {errors.quantity && <span className="error">{errors.quantity.message}</span>}
        </label>
        <label>
          Price
          <Input type="number" step="0.01" {...register("price", { valueAsNumber: true })} />
          {errors.price && <span className="error">{errors.price.message}</span>}
        </label>
        <label>
          Currency
          <Input placeholder="USD" {...register("currency")} />
          {errors.currency && <span className="error">{errors.currency.message}</span>}
        </label>
        <label>
          Buyer Account ID
          <Input placeholder="uuid" {...register("buyerAccountId")} />
          {errors.buyerAccountId && <span className="error">{errors.buyerAccountId.message}</span>}
        </label>
        <label>
          Seller Account ID
          <Input placeholder="uuid" {...register("sellerAccountId")} />
          {errors.sellerAccountId && <span className="error">{errors.sellerAccountId.message}</span>}
        </label>
      </div>
      <Button disabled={isSubmitting} type="submit">
        {isSubmitting ? "Submitting…" : "Create trade"}
      </Button>
    </form>
  );
}