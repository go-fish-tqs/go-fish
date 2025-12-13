import Link from "next/link";

interface BookingErrorProps {
    message: string;
    linkHref?: string;
    linkLabel?: string;
}

export default function BookingError({
    message,
    linkHref = "/items",
    linkLabel = "Browse Items",
}: BookingErrorProps) {
    return (
        <div className="max-w-2xl mx-auto p-6">
            <div className="text-center py-12 bg-red-50 rounded-xl border border-red-100">
                <svg
                    className="w-12 h-12 text-red-400 mx-auto mb-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={1.5}
                        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                </svg>
                <p className="text-red-600 font-medium">{message}</p>
                <Link
                    href={linkHref}
                    className="text-blue-600 hover:underline mt-4 inline-block font-medium"
                >
                    {linkLabel}
                </Link>
            </div>
        </div>
    );
}
