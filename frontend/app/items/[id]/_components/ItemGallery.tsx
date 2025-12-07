// app/items/[id]/_components/ItemGallery.tsx
"use client";

import { useState } from "react";

interface ItemGalleryProps {
  images: string[];
  name: string;
}

export default function ItemGallery({ images, name }: ItemGalleryProps) {
  const [selectedImage, setSelectedImage] = useState(images[0] || "");

  if (!images || images.length === 0) {
    return (
      <div className="w-full h-64 bg-gray-200 rounded-lg flex items-center justify-center text-gray-500">
        Sem imagens marafadas
      </div>
    );
  }

  return (
    <div className="space-y-4 max-w-full">
      {/* Imagem Grande */}
      <div className="aspect-square w-full max-h-[400px] overflow-hidden rounded-lg bg-gray-100 relative border border-gray-200">
        <img
          src={selectedImage}
          alt={name}
          className="h-full w-full object-cover object-center"
        />
      </div>

      {/* Miniaturas (se houver mais que uma) */}
      {images.length > 1 && (
        <div className="flex gap-3 overflow-x-auto pb-2">
          {images.map((img, idx) => (
            <button
              key={idx}
              onClick={() => setSelectedImage(img)}
              className={`relative h-16 w-16 flex-shrink-0 overflow-hidden rounded-md border-2 ${
                selectedImage === img ? "border-blue-600" : "border-transparent"
              }`}
            >
              <img
                src={img}
                alt={`Thumb ${idx}`}
                className="h-full w-full object-cover"
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
