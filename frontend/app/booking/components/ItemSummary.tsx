import { Item } from "@/app/items/types";

interface ItemSummaryProps {
    item: Item;
}

export default function ItemSummary({ item }: ItemSummaryProps) {
    return (
        <div className="flex-1 flex flex-col">
            {/* Large Hero Image */}
            <div className="relative h-80 rounded-2xl overflow-hidden mb-6 group">
                {item.photoUrls && item.photoUrls.length > 0 ? (
                    <img
                        src={item.photoUrls[0]}
                        alt={item.name}
                        className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                    />
                ) : (
                    <div className="w-full h-full bg-gradient-to-br from-blue-100 via-indigo-100 to-purple-100 flex items-center justify-center">
                        <svg className="w-20 h-20 text-blue-300/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                )}

                {/* Glass overlay with price */}
                {item.price !== undefined && (
                    <div className="absolute bottom-4 right-4 backdrop-blur-xl bg-white/30 border border-white/40 px-4 py-2 rounded-2xl shadow-lg">
                        <span className="text-2xl font-bold text-white drop-shadow-lg">
                            ${item.price.toFixed(2)}
                            <span className="text-sm font-normal opacity-80"> / day</span>
                        </span>
                    </div>
                )}

                {/* Material badge */}
                {item.material && (
                    <div className="absolute top-4 left-4 backdrop-blur-xl bg-white/30 border border-white/40 px-3 py-1.5 rounded-full">
                        <span className="text-sm font-medium text-white drop-shadow">
                            {item.material.replace(/_/g, " ")}
                        </span>
                    </div>
                )}
            </div>

            {/* Item Info - Glass Card */}
            <div className="backdrop-blur-xl bg-white/60 border border-white/40 rounded-2xl p-6 shadow-xl">
                <h2 className="text-2xl font-bold text-gray-800 mb-3">{item.name}</h2>
                <p className="text-gray-600 leading-relaxed">{item.description}</p>

                {item.category && (
                    <div className="mt-4 pt-4 border-t border-gray-200/50">
                        <span className="text-xs uppercase tracking-wide text-gray-500">Category</span>
                        <p className="text-gray-700 font-medium mt-1">{String(item.category).replace(/_/g, " ")}</p>
                    </div>
                )}
            </div>
        </div>
    );
}
