// app/items/[id]/_components/ItemGallery.tsx
"use client";

import { useState } from "react";

interface ItemGalleryProps {
  images: string[];
  name: string;
}

export default function ItemGallery({ images, name }: ItemGalleryProps) {
  const [selectedImage, setSelectedImage] = useState(images[0] || "");
  const [imageError, setImageError] = useState(false);

  console.log("ItemGallery - Received images:", images);
  console.log("ItemGallery - Selected image:", selectedImage);

  if (!images || images.length === 0) {
    return (
      <div className="w-full h-64 bg-gray-200 rounded-lg flex items-center justify-center text-gray-500">
        Sem imagens disponíveis
      </div>
    );
  }

  return (
    <div className="space-y-4 max-w-full">
      {/* Main Image */}
      <div className="w-full max-h-[400px] overflow-hidden rounded-lg bg-gray-100 relative border border-gray-200 aspect-square">
        {!imageError ? (
          <img
            src={selectedImage}
            alt={name}
            width={400}
            height={400}
            className="h-full w-full object-cover object-center"
            onError={(e) => {
              console.error("Failed to load image:", selectedImage);
              setImageError(true);
            }}
          />
        ) : (
          <div className="h-full w-full flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-100">
            <div className="text-center p-6 max-w-md">
              <svg
                className="w-20 h-20 text-blue-300 mx-auto mb-3"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <p className="text-sm font-medium text-gray-700 mb-2">
                Erro ao carregar imagem
              </p>
              <p className="text-xs text-gray-500 mb-3">
                Use URLs diretos de imagens (.jpg, .png, etc.)
                <br />
                Evite links do Google Search
              </p>
              <details className="text-left">
                <summary className="text-xs text-blue-600 cursor-pointer hover:text-blue-800">
                  Ver URL
                </summary>
                <p className="text-xs text-gray-600 mt-2 break-all bg-white p-2 rounded border">
                  {selectedImage}
                </p>
              </details>
            </div>
          </div>
        )}
      </div>

      {/* Thumbnails (if more than one) */}
      {images.length > 1 && (
        <div className="flex gap-3 overflow-x-auto pb-2">
          {images.map((img, idx) => (
            <button
              key={idx}
              onClick={() => {
                setSelectedImage(img);
                setImageError(false);
              }}
              className={`relative h-16 w-16 flex-shrink-0 overflow-hidden rounded-md border-2 ${
                selectedImage === img ? "border-blue-600" : "border-transparent"
              }`}
            >
              <img
                src={img}
                alt={`Thumb ${idx}`}
                width={64}
                height={64}
                className="h-full w-full object-cover"
                onError={(e) => {
                  const target = e.target as HTMLImageElement;
                  target.style.display = "none";
                }}
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
