"use client";

import { use } from "react"; // <--- 1. IMPORTA ISTO
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import ItemGallery from "./_components/ItemGallery";
import ItemInfo from "./_components/ItemInfo";
import { Item } from "@/app/items/types";

interface PageProps {
  // 2. AGORA É UMA PROMISE!
  params: Promise<{
    id: string;
  }>;
}

export default function ItemPage({ params }: PageProps) {
  // 3. DESEMBRULHA A PROMESSA AQUI
  const { id } = use(params);

  // Daqui para baixo é tudo igual, mas usas o 'id' que acabaste de sacar
  const {
    data: item,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["item", id], // <--- Usas o id desembrulhado
    queryFn: async () => {
      const res = await fetch(`http://localhost:8080/api/items/${id}`);
      if (!res.ok) throw new Error("Deu barraca no fetch!");
      return res.json() as Promise<Item>;
    },
  });

  if (isLoading) return <div className="p-10 text-center">A carregar...</div>;
  if (isError || !item)
    return (
      <div className="p-10 text-center text-red-600">
        Erro: {error?.message}
      </div>
    );

  return (
    <div className="px-4 py-6 sm:px-6 lg:px-8  overflow-y-hidden">
      <Link
        href="/items"
        className="inline-flex items-center gap-2 text-sm font-medium text-blue-600 hover:text-blue-800 mb-4"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M15 19l-7-7 7-7"
          />
        </svg>
        Back to Items
      </Link>
      <div className="grid gap-6 lg:gap-8 lg:grid-cols-2 items-start">
        <ItemGallery images={item.photoUrls} name={item.name} />
        <ItemInfo item={item} />
      </div>
    </div>
  );
}
