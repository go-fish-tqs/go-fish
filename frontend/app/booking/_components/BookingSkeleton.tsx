export default function BookingSkeleton() {
    return (
        <div className="max-w-2xl mx-auto p-6">
            <div className="animate-pulse space-y-4">
                <div className="h-6 bg-gray-200 rounded w-24"></div>
                <div className="bg-white rounded-2xl p-8 space-y-6">
                    <div className="h-8 bg-gray-200 rounded w-1/3"></div>
                    <div className="bg-gray-100 rounded-xl p-4">
                        <div className="flex gap-4">
                            <div className="w-24 h-24 bg-gray-200 rounded-lg"></div>
                            <div className="flex-1 space-y-2">
                                <div className="h-5 bg-gray-200 rounded w-2/3"></div>
                                <div className="h-4 bg-gray-200 rounded w-full"></div>
                                <div className="h-6 bg-gray-200 rounded w-1/4"></div>
                            </div>
                        </div>
                    </div>
                    <div className="h-12 bg-gray-200 rounded-xl"></div>
                    <div className="h-12 bg-gray-200 rounded-xl"></div>
                    <div className="flex gap-4 pt-4">
                        <div className="flex-1 h-12 bg-gray-200 rounded-xl"></div>
                        <div className="w-24 h-12 bg-gray-200 rounded-xl"></div>
                    </div>
                </div>
            </div>
        </div>
    );
}
