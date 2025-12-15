import { use } from "react";
import ItemUpdateForm from "./ItemUpdateForm";

export default function EditItemPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-gray-50 px-4 py-8">
      <ItemUpdateForm itemId={id} />
    </div>
  );
}
