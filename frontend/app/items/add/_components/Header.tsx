"use client";

export function Header() {
    return (
        <div className="mb-8 border-b border-gray-200 dark:border-gray-700 pb-6">
            <div className="flex items-center gap-4 mb-3">
                <div className="flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 shadow-lg shadow-blue-500/30">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-6 w-6 text-white"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M12 4v16m8-8H4"
                        />
                    </svg>
                </div>

                <div>
                    <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
                        Add New Item
                    </h1>
                </div>
            </div>

            <p className="text-gray-600 dark:text-gray-400 ml-16 text-sm">
                Fill in the details below to add a new fishing equipment item to your inventory
            </p>
        </div>
    );
}
